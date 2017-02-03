package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.events.EnsureResizedImageJobEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.jaxrs.NcmsNotificationException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.mediawiki.GMapTag;
import com.softmotions.ncms.mediawiki.MediaWikiRenderer;

/**
 * MediaWiki/Markdown attribute manager.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class AsmWikiAM extends AsmAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmWikiAM.class);

    public static final String MARKUP_MEDIAWIKI = "mediawiki";

    public static final String MARKUP_MARKDOWN = "markdown";

    public static final String[] TYPES = {"wiki"};

    // \[\[(image|media):(\s*)(/)?(\d+)/(.*)\]\]
    private static final Pattern MW_MEDIAREF_REGEXP =
            Pattern.compile("\\[\\[(image|media):(\\s*)(/)?(\\d+)/(.*)\\]\\]", Pattern.CASE_INSENSITIVE);

    // \[\[page:\s*([0-9a-f]{32})(\s*\|.*)?\]\]
    private static final Pattern MW_PAGEREF_REGEXP =
            Pattern.compile("\\[\\[page:\\s*([0-9a-f]{32})(\\s*\\|.*)?\\]\\]", Pattern.CASE_INSENSITIVE);


    // \[\[page:\s*([0-9a-f]{32})(\s*\|(.*))?\]\]
    private static final Pattern MW_PAGENAME_REGEXP =
            Pattern.compile("\\[\\[page:\\s*([0-9a-f]{32})(\\s*\\|(.*))?\\]\\]", Pattern.CASE_INSENSITIVE);


    private static final Pattern MW_WIKIFIX_REGEXP =
            Pattern.compile("(/rs/mw/[^\"\'>]+)|((/asm)?/([0-9a-f]{32}))", Pattern.CASE_INSENSITIVE);

    private static final Pattern MD_LINKS_REGEXP =
            Pattern.compile("([\\(<])((page:/?([0-9a-f]{32}))|((media|image):/?(\\d+)(/[^|\\]>)]+)?(\\|(\\d+)px)?))([>\\)])",
                            Pattern.CASE_INSENSITIVE);

    private static final Pattern MD_LINKS2_REGEXP =
            Pattern.compile("([\\(<])(media|image:/?)(\\d+)(/[^|\\]>)]+)?(\\|\\d+px)?([>\\)])",
                            Pattern.CASE_INSENSITIVE);


    private final MediaWikiRenderer mediaWikiRenderer;

    private final ObjectMapper mapper;

    private final PageService pageService;

    private final NcmsEnvironment env;

    private final NcmsEventBus ebus;

    @Inject
    public AsmWikiAM(ObjectMapper mapper,
                     MediaWikiRenderer mediaWikiRenderer,
                     NcmsEnvironment env,
                     PageService pageService,
                     NcmsEventBus ebus) {
        this.env = env;
        this.mapper = mapper;
        this.mediaWikiRenderer = mediaWikiRenderer;
        this.pageService = pageService;
        this.ebus = ebus;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        String effectiveValue = attr.getEffectiveValue();
        if (StringUtils.isBlank(effectiveValue)) {
            return null;
        }
        try {
            JsonNode value = mapper.readTree(effectiveValue);
            String markup = value.path("markup").asText();
            String text = null;
            if (MARKUP_MEDIAWIKI.equals(markup)) {
                text = mediaWikiRenderer.toText(value.path("value").asText());
                text = GMapTag.GMAP_FRAME_PATTERN.matcher(text).replaceAll("");
                text = MW_MEDIAREF_REGEXP.matcher(text).replaceAll("");
                text = MW_PAGENAME_REGEXP.matcher(text).replaceAll("$3");
            } else if (MARKUP_MARKDOWN.equals(markup)) {
                // todo
            }
            if (text != null) {
                return new String[]{text};
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, @Nonnull Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        String res = null;
        String markup = null;
        String value = attr.getEffectiveValue();
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return null;
            }
            JsonToken t;
            do {
                t = parser.nextValue();
                if ("html".equals(parser.getCurrentName())) {
                    res = parser.getValueAsString();
                } else if ("markup".equals(parser.getCurrentName())) {
                    markup = parser.getValueAsString();
                }
                if (res != null && markup != null) {
                    break;
                }
            } while (t != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return postProcessHtml(markup, res);
    }

    private String postProcessHtml(String markup, String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        StringBuffer res = new StringBuffer(html.length());
        // (/rs/mw/.*)|((/asm)?/([0-9a-f]{32}))
        //
        // 0:((/(12d5c7a0c3167d3d21d30f1c43368b32)4)2)0
        // 0:((/rs/mw/link/Image:300px-/421/header.jpg)1)0
        Matcher m = MW_WIKIFIX_REGEXP.matcher(html);
        while (m.find()) {
            final String guid = m.group(4);
            final String fref = m.group(1);
            if (!StringUtils.isBlank(guid)) {
                String link = pageService.resolvePageLink(guid);
                if (link != null) {
                    m.appendReplacement(res, link);
                } else {
                    m.appendReplacement(res, m.group());
                }
            } else if (!StringUtils.isBlank(fref)) {
                if (!fref.startsWith(env.getAppRoot())) {
                    m.appendReplacement(res, env.getAppRoot() + fref);
                } else {
                    m.appendReplacement(res, m.group());
                }
            } else {
                m.appendReplacement(res, m.group());
            }
        }
        m.appendTail(res);
        return res.toString();
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        if (attr.getOptions() != null) {
            asmOpts.loadOptions(attr.getOptions());
        }
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts, "markup");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String value = val.path("value").asText("");
        String markup = val.path("markup").asText(MARKUP_MEDIAWIKI);
        String html = null;
        if (!StringUtils.isBlank(value)) {

            checkAdminResource(ctx, value);

            if (MARKUP_MEDIAWIKI.equals(markup)) {

                html = mediaWikiRenderer.render(preSaveMediaWiki(ctx, attr, value), ctx.getLocale());
                html = "<div class=\"wiki\">" +
                       html +
                       "\n</div>";

            } else if (MARKUP_MARKDOWN.equals(markup)) {

                MutableDataHolder options = new MutableDataSet();
                options.setFrom(ParserEmulationProfile.KRAMDOWN.getOptions());
                options.set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        TocExtension.create(),
                        AutolinkExtension.create(),
                        WikiLinkExtension.create()
                ));
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                html = "<div class=\"markdown\">" +
                       renderer.render(parser.parse(preSaveMarkdown(ctx, attr, value))) +
                       "\n</div>";
            } else {
                log.warn("Unsupported markup language: {}", markup);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Rendered HTML={}", html);
        }
        attr.setEffectiveValue(mapper.writeValueAsString(
                mapper.createObjectNode()
                      .put("markup", markup)
                      .put("html", html)
                      .put("value", value)));
        return attr;
    }

    private void checkAdminResource(AsmAttributeManagerContext ctx, String value) {
        String admRoot = env.getNcmsAdminRoot();
        HttpServletRequest req = ctx.getRequest();
        Pattern adminResourcePattern =
                Pattern.compile("http(s)?://(www\\.)?" + req.getServerName() + "(:\\d+)?" + admRoot + "/.*");

        Matcher m = adminResourcePattern.matcher(value);
        if (m.find()) {
            throw new NcmsNotificationException("ncms.page.nosav.adm.link", true, req);
        }
    }

    private String preSaveMediaWiki(AsmAttributeManagerContext ctx, AsmAttribute attr, String value) {

        // \[\[page:\s*([0-9a-f]{32})(\s*\|.*)?\]\]
        Matcher m = MW_PAGEREF_REGEXP.matcher(value);
        while (m.find()) {
            String guid = m.group(1);
            if (guid != null) {
                ctx.registerPageDependency(attr, guid);
            }
        }
        // \[\[(image|media):(\s*)(/)?(\d+)/(.*)\]\]
        m = MW_MEDIAREF_REGEXP.matcher(value);
        while (m.find()) {
            String fileId = m.group(4);
            if (fileId == null) {
                continue;
            }
            try {
                ctx.registerFileDependency(attr, Long.parseLong(fileId));
            } catch (NumberFormatException e) {
                log.error("", e);
            }
        }
        return value;
    }

    private String preSaveMarkdown(AsmAttributeManagerContext ctx, AsmAttribute attr, String value) {

        // Links:
        // <media:893892>
        // [test](page:32826bfa40b52c8ccf3359e69501ac7b)
        // [test2](media:223)
        // ![gras](image:1001/ejdb.png|400px)

        StringBuffer res = new StringBuffer(value.length());
        // ([(<])((page:/?([0-9a-f]{32}))|((file|image):/?(\d+)(/[^|]+)?(\|(\d+)px)?))([>)])
        Matcher m = MD_LINKS_REGEXP.matcher(value);
        while (m.find()) {
            String open = m.group(1);
            String close = m.group(11);
            if (("(".equals(open) && !")".equals(close)) || ("<".equals(open) && !">".equals(close))) {
                m.appendReplacement(res, m.group(0));
                continue;
            }
            //  0:((()1((page:(32826bfa40b52c8ccf3359e69501ac7b)4)3)2())11)0    (page:32826bfa40b52c8ccf3359e69501ac7b)
            String guid = m.group(4);
            String mediaPart = m.group(5);
            if (guid != null) {
                m.appendReplacement(res, open + '/' + guid + close);
                ctx.registerPageDependency(attr, guid);
            } else if (mediaPart != null) {
                // 0:((()1(((image)6:(1001)7(/ejdb.png)8(|(400)10px)9)5)2())11)0        ![gras](image:1001/ejdb.png|400px)
                // 0:((()1(((media)6:(9329)7)5)2())11)0                                 (media:9329)
                // 0:((()1(((image)6:(1001)7(/ejdb.png)8)5)2())11)0                     (/image:1001/ejdb.png)
                String part = StringUtils.capitalize(m.group(6).toLowerCase());
                Long fid = Long.parseLong(m.group(7));              // file id
                String fname = StringUtils.trimToEmpty(m.group(8)); // file name
                if (m.group(10) == null) {
                    m.appendReplacement(res, open + "/rs/mw/link/" + part + ':' + fid + fname + close);
                } else {
                    // /rs/mw/link/image:400px-/1001/ejdb.png"
                    Integer px = Integer.parseInt(m.group(10));
                    m.appendReplacement(res, open + "/rs/mw/link/" + part + ':' + px + "px-/" + fid + fname + close);
                    ebus.fire(new EnsureResizedImageJobEvent(fid, px, null, MediaRepository.RESIZE_SKIP_SMALL));
                }
                ctx.registerFileDependency(attr, fid);
            }
        }
        m.appendTail(res);
        return res.toString();
    }

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr, Map<Long, Long> fmap) throws Exception {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            return attr;
        }
        ObjectNode node = (ObjectNode) mapper.readTree(attr.getEffectiveValue());
        String markup = node.path("markup").asText("");
        String value = node.path("value").asText("");
        StringBuffer nmarkup = new StringBuffer(value.length());

        if (MARKUP_MEDIAWIKI.equals(markup)) {

            // \[\[(image|media):(\s*)(/)?(\d+)/(.*)\]\]
            Matcher m = MW_MEDIAREF_REGEXP.matcher(value);
            while (m.find()) {
                if (m.group(4) == null) {
                    m.appendReplacement(nmarkup, m.group(0));
                    continue;
                }
                Long tfid = fmap.get(Long.parseLong(m.group(4)));
                if (tfid == null) {
                    m.appendReplacement(nmarkup, m.group(0));
                    continue;
                }
                // \[\[(image|media):(\s*)(/)?(\d+)/(.*)\]\]
                // 0:([[(Image)1:()2(/)3(421)4/(header.jpg|300px|center|frame|Тест)5]])0
                m.appendReplacement(nmarkup, "[[" +
                                             m.group(1) +
                                             ':' +
                                             StringUtils.defaultString(m.group(2)) +
                                             StringUtils.defaultString(m.group(3)) +
                                             tfid +
                                             '/' +
                                             StringUtils.defaultString(m.group(5)) +
                                             "]]");
            }
            m.appendTail(nmarkup);

        } else if (MARKUP_MARKDOWN.equals(markup)) {

            // ([\(<])(media|image:/?)(\d+)(/[^|\]>)]+)?(\|\d+px)?([>\)])
            Matcher m = MD_LINKS2_REGEXP.matcher(value);
            while (m.find()) {
                if (m.group(3) == null) {
                    m.appendReplacement(nmarkup, m.group(0));
                    continue;
                }
                Long tfid = fmap.get(Long.parseLong(m.group(3)));
                if (tfid == null) {
                    m.appendReplacement(nmarkup, m.group(0));
                    continue;
                }
                // (image:1001/ejdb.png|500px)
                // 0:((()1(image:)2(1001)3(/ejdb.png)4(|500px)5())6)0
                m.appendReplacement(
                        nmarkup,
                        m.group(1) +
                        m.group(2) +
                        tfid +
                        MoreObjects.firstNonNull(m.group(4), "") +
                        MoreObjects.firstNonNull(m.group(5), "") +
                        m.group(6));

            }
            m.appendTail(nmarkup);

        } else {
            return attr;
        }
        node.put("value", nmarkup.toString());
        return applyAttributeValue(ctx, attr, node);
    }
}
