package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.ncms.mediawiki.GMapTag;
import com.softmotions.ncms.mediawiki.MediaWikiRenderer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown/Mediawiki attribute manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmWikiAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmWikiAM.class);

    public static final String[] TYPES = {"wiki"};

    private static final Pattern MEDIAREF_REGEXP = Pattern.compile("\\[\\[(image|media):\\s*(/)?(\\d+)/.*\\]\\]",
                                                                   Pattern.CASE_INSENSITIVE);

    private static final Pattern PAGEREF_REGEXP = Pattern.compile("\\[\\[page:\\s*([0-9a-f]{32})(\\s*\\|.*)?\\]\\]",
                                                                  Pattern.CASE_INSENSITIVE);

    private static final Pattern PAGENAME_REGEXP = Pattern.compile("\\[\\[page:\\s*([0-9a-f]{32})(\\s*\\|(.*))?\\]\\]",
                                                                   Pattern.CASE_INSENSITIVE);

    private final ObjectMapper mapper;

    private final MediaWikiRenderer mediaWikiRenderer;

    private final Pattern pageRefsRE;

    private final PageService pageService;

    private final NcmsMessages messages;

    private final NcmsEnvironment env;


    @Inject
    public AsmWikiAM(ObjectMapper mapper,
                     MediaWikiRenderer mediaWikiRenderer,
                     NcmsEnvironment env,
                     PageService pageService,
                     NcmsMessages messages) {
        this.env = env;
        this.mapper = mapper;
        this.mediaWikiRenderer = mediaWikiRenderer;
        this.pageService = pageService;
        this.pageRefsRE = Pattern.compile(Pattern.quote(env.getAppRoot()) + "/(asm/)?" + "([0-9a-f]{32})");
        this.messages = messages;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        String effectiveValue = attr.getEffectiveValue();
        if (!StringUtils.isBlank(effectiveValue)) {
            try {
                JsonNode value = mapper.readTree(effectiveValue);
                String text = mediaWikiRenderer.toText(value.get("value").asText());
                text = GMapTag.GMAP_FRAME_PATTERN.matcher(text).replaceAll("");
                text = MEDIAREF_REGEXP.matcher(text).replaceAll("");
                text = PAGENAME_REGEXP.matcher(text).replaceAll("$3");
                return new String[]{text};
            } catch (IOException ignored) {
                return null;
            }
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
                    res = postProcessWikiHtml(parser.getValueAsString());
                    break;
                }
            } while (t != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private String postProcessWikiHtml(String html) {
        if (html == null) {
            return "";
        }
        StringBuffer res = new StringBuffer(html.length());
        Matcher m = pageRefsRE.matcher(html);
        while (m.find()) {
            String guid = m.group(2);
            if (guid != null) {
                String link = pageService.resolvePageLink(guid);
                if (link != null) {
                    m.appendReplacement(res, link);
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
        ctx.clearPageDeps(attr);
        ctx.clearFileDeps(attr);

        String value = val.hasNonNull("value") ? val.get("value").asText() : "";
        String markup = val.hasNonNull("markup") ? val.get("markup").asText() : "mediawiki";
        String html = null;
        if (!StringUtils.isBlank(value)) {
            if ("mediawiki".equals(markup)) {
                html = mediaWikiRenderer.render(preSaveWiki(ctx, attr, value), messages.getLocale(ctx.getRequest()));
                html = "<div class=\"wiki\">" +
                       html +
                       "\n</div>";
            } else {
                log.warn("Unsupported markup language: {}", markup);
                html = null;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Rendered HTML={}", html);
        }
        ObjectNode root = mapper.createObjectNode();
        root.put("html", html);
        root.put("markup", markup);
        root.put("value", value);
        attr.setEffectiveValue(root.toString());
        return attr;
    }

    private String preSaveWiki(AsmAttributeManagerContext ctx, AsmAttribute attr, String value) {

        String admRoot = env.getNcmsAdminRoot();
        HttpServletRequest req = ctx.getRequest();
        Pattern adminResourcePattern =
                Pattern.compile("http(s)?://(www\\.)?" + req.getServerName() + "(:\\d+)?" + admRoot + "/.*");

        Matcher m = adminResourcePattern.matcher(value);
        if (m.find()) {
            throw new NcmsMessageException(messages.get("ncms.page.nosav.adm.link"), true);
        }

        // \[\[page:\s*([0-9a-f]{32})(\s*\|.*)?\]\]
        m = PAGEREF_REGEXP.matcher(value);
        while (m.find()) {
            String guid = m.group(1);
            if (guid != null) {
                ctx.registerPageDependency(attr, guid);
            }
        }
        m = MEDIAREF_REGEXP.matcher(value);
        while (m.find()) {
            String fileId = m.group(3);
            if (fileId == null) {
                continue;
            }
            try {
                long fid = Long.parseLong(fileId);
                ctx.registerMediaFileDependency(attr, fid);
            } catch (NumberFormatException e) {
                log.error("", e);
            }
        }
        return value;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
