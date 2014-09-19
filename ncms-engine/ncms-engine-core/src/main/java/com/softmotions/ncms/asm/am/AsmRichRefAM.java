package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.RichRef;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmRichRefAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmRichRefAM.class);

    private static final String[] TYPES = new String[]{"richref"};

    private final AsmImageAM imageAM;

    private final ObjectMapper mapper;

    private final NcmsConfiguration cfg;

    @Inject
    public AsmRichRefAM(AsmImageAM imageAM,
                        ObjectMapper mapper,
                        NcmsConfiguration cfg) {
        this.imageAM = imageAM;
        this.mapper = mapper;
        this.cfg = cfg;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        return attr;
    }

    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    public RichRef renderAsmAttribute(AsmRendererContext ctx, ObjectNode node) throws AsmRenderingException {
        JsonNode n;
        String link = null;
        String name = null;
        String description = null;
        String style = null;
        String style2 = null;
        Image image = null;
        if (node.hasNonNull("image")) {
            image = imageAM.renderAsmAttribute(ctx, (ObjectNode) node.get("image"));
        }
        n = node.get("link");
        if (n != null && n.isTextual()) {
            link = n.asText().trim();
            int ind = link.indexOf('|');
            if (ind != -1) {
                if (ind < link.length() - 1) {
                    name = link.substring(ind + 1).trim();
                }
                link = link.substring(0, ind).trim();
            }
            link = cfg.getResourceLink(link);
        }
        if (node.hasNonNull("description")) {
            description = node.get("description").asText();
        }
        if (node.hasNonNull("style")) {
            style = node.get("style").asText();
        }
        if (node.hasNonNull("style2")) {
            style2 = node.get("style2").asText();
        }
        return new RichRef(name, link, description, image, style, style2);
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        // {
        // "image":{"id":561,"options":{"restrict":"false","width":"693","skipSmall":"false","resize":"true"}},
        // "link":"page:9036a644c27a6479490ba1fef54f86af | Контакты","description":"testf"
        // }
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr != null ? attr.getEffectiveValue() : null;
        if (StringUtils.isBlank(value)) {
            return null;
        }
        RichRef res;
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(value);
            res = renderAsmAttribute(ctx, node);
        } catch (IOException e) {
            throw new AsmRenderingException(e);
        }
        //log.info("res=" + res);
        return res;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts);
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        if (val == null) {
            attr.setEffectiveValue(null);
            return null;
        }
        attr.setEffectiveValue(applyJSONAttributeValue(ctx, attr, val).toString());
        return attr;
    }

    public JsonNode applyJSONAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) {
        JsonNode n = val.get("image");
        if (n != null) {
            imageAM.applyJSONAttributeValue(ctx, attr, n);
        }
        n = val.get("link");
        if (n != null && n.isTextual()) {
            Long fid = cfg.getFileIdByResourceSpec(n.asText());
            if (fid != null) {
                ctx.registerMediaFileDependency(attr, fid);
            }
        }
        return val;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }
}
