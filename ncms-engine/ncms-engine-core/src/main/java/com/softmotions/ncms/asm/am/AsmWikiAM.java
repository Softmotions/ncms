package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.Collections;
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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationFamily;
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
import com.softmotions.ncms.jaxrs.NcmsNotificationException;
import com.softmotions.ncms.mediawiki.GMapTag;
import com.softmotions.ncms.mediawiki.MediaWikiRenderer;

/**
 * Mediawiki attribute manager.
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
    private static final Pattern MW_MEDIAREF_REGEXP = Pattern.compile("\\[\\[(image|media):(\\s*)(/)?(\\d+)/(.*)\\]\\]",
                                                                      Pattern.CASE_INSENSITIVE);

    // \[\[page:\s*([0-9a-f]{32})(\s*\|.*)?\]\]
    private static final Pattern MW_PAGEREF_REGEXP = Pattern.compile("\\[\\[page:\\s*([0-9a-f]{32})(\\s*\\|.*)?\\]\\]",
                                                                     Pattern.CASE_INSENSITIVE);


    // \[\[page:\s*([0-9a-f]{32})(\s*\|(.*))?\]\]
    private static final Pattern MW_PAGENAME_REGEXP = Pattern.compile("\\[\\[page:\\s*([0-9a-f]{32})(\\s*\\|(.*))?\\]\\]",
                                                                      Pattern.CASE_INSENSITIVE);


    private static final Pattern MW_WIKIFIX_REGEXP = Pattern.compile("(/rs/mw/[^\"\'>]+)|((/asm)?/([0-9a-f]{32}))",
                                                                     Pattern.CASE_INSENSITIVE);


    private final MediaWikiRenderer mediaWikiRenderer;

    private final ObjectMapper mapper;

    private final PageService pageService;

    private final NcmsEnvironment env;


    @Inject
    public AsmWikiAM(ObjectMapper mapper,
                     MediaWikiRenderer mediaWikiRenderer,
                     NcmsEnvironment env,
                     PageService pageService) {
        this.env = env;
        this.mapper = mapper;
        this.mediaWikiRenderer = mediaWikiRenderer;
        this.pageService = pageService;
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
        String value = attr.getEffectiveValue();
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return null;
            }
            JsonToken t;
            do {
                t = parser.nextValue();
                if ("html".equals(parser.getCurrentName())) {
                    res = postProcessHtml(parser.getValueAsString());
                    break;
                }
            } while (t != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private String postProcessHtml(String html) {
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
            if (MARKUP_MEDIAWIKI.equals(markup)) {
                html = mediaWikiRenderer.render(preSaveMediaWiki(ctx, attr, value), ctx.getLocale());
                html = "<div class=\"wiki\">" +
                       html +
                       "\n</div>";
            } else if (MARKUP_MARKDOWN.equals(markup)) {
                MutableDataHolder options = new MutableDataSet();
                options.setFrom(ParserEmulationFamily.KRAMDOWN.getOptions());
                options.set(Parser.EXTENSIONS, Collections.singleton(TablesExtension.create()));
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                html = "<div class=\"markdown\">" +
                       renderer.render(parser.parse(preSaveMarkdown(ctx, attr, value))) +
                       "\n</div>";
            } else {
                log.warn("Unsupported markup language: {}", markup);
            }
        }
//        if (log.isDebugEnabled()) {
//            log.debug("Rendered HTML={}", html);
//        }
//        log.info("Rendered HTML={}", html);
        ObjectNode root = mapper.createObjectNode();
        root.put("html", html);
        root.put("markup", markup);
        root.put("value", value);
        attr.setEffectiveValue(mapper.writeValueAsString(root));
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

        checkAdminResource(ctx, value);

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
                long fid = Long.parseLong(fileId);
                ctx.registerFileDependency(attr, fid);
            } catch (NumberFormatException e) {
                log.error("", e);
            }
        }
        return value;
    }

    private String preSaveMarkdown(AsmAttributeManagerContext ctx, AsmAttribute attr, String value) {

        // [link](page:32826bfa40b52c8ccf3359e69501ac7b)
        // ![smiley](smiley.png){:height="36px" width="36px"}
        Pattern p = Pattern.compile("(page:([0-9a-f]{32}))|(file:(\\d+))", Pattern.CASE_INSENSITIVE);


        // todo

        return value;
    }

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr, Map<Long, Long> fmap) throws Exception {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            return attr;
        }
        ObjectNode node = (ObjectNode) mapper.readTree(attr.getEffectiveValue());
        String markup = node.path("markup").asText("");
        if (MARKUP_MEDIAWIKI.equals(markup)) {
            String value = node.path("value").asText("");
            StringBuffer nmarkup = new StringBuffer(value.length());
            Matcher m = MW_MEDIAREF_REGEXP.matcher(value);
            while (m.find()) {
                String sfid = m.group(4);
                if (sfid == null) continue;
                Long fid;
                try {
                    fid = Long.parseLong(sfid);
                } catch (NumberFormatException ignored) {
                    continue;
                }
                Long tfid = fmap.get(fid);
                if (tfid == null) {
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
            node.put("value", nmarkup.toString());
            return applyAttributeValue(ctx, attr, node);
        } else if (MARKUP_MARKDOWN.equals(markup)) {
            // todo
            return attr;
        } else {
            return attr;
        }
    }
}
