package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mediawiki.MediaWikiRenderer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Markdown/Mediawiki attribute manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmWikiAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmWikiAM.class);

    public static final String[] TYPES = new String[]{"wiki"};

    private final ObjectMapper mapper;

    private final MediaWikiRenderer mediaWikiRenderer;

    @Inject
    public AsmWikiAM(ObjectMapper mapper,
                     MediaWikiRenderer mediaWikiRenderer) {
        this.mapper = mapper;
        this.mediaWikiRenderer = mediaWikiRenderer;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        return attr;
    }

    public String[] prepareFulltextSearchData(AsmAttribute attr) {
        if (!StringUtils.isBlank(attr.getValue())) {
            try {
                JsonNode value = mapper.readTree(attr.getValue());
                return new String[]{value.get("value").asText()};
            } catch (IOException ignored) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
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
        //todo apply page aliases
        return html;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        if (attr.getOptions() != null) {
            asmOpts.loadOptions(attr.getOptions());
        }
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts, "markup");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String value = val.has("value") ? val.get("value").asText() : null;
        String markup = val.has("markup") ? val.get("markup").asText() : "mediawiki";
        String html = null;
        if (!StringUtils.isBlank(value)) {
            if ("mediawiki".equals(markup)) {
                html = mediaWikiRenderer.render(value, ctx.getRequest());
                html = new StringBuilder(html.length() + 32)
                        .append("<div class=\"wiki\">")
                        .append(html)
                        .append("\n</div>")
                        .toString();
            } else {
                log.warn("Unsupported markup language: " + markup);
                html = null;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Rendered HTML=" + html);
        }
        ObjectNode root = mapper.createObjectNode();
        root.put("html", html);
        root.put("markup", markup);
        root.put("value", value);
        attr.setEffectiveValue(root.toString());
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }
}
