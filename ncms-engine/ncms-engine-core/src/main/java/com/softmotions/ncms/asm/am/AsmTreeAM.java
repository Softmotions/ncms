package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.Tree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
 * Tree strucrure attribute manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmTreeAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmTreeAM.class);

    private static final String[] TYPES = new String[]{"tree"};

    private final ObjectMapper mapper;

    private final AsmRichRefAM richRefAM;

    @Inject
    public AsmTreeAM(ObjectMapper mapper, AsmRichRefAM richRefAM) {
        this.mapper = mapper;
        this.richRefAM = richRefAM;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            try {
                attr.setEffectiveValue(mapper.writeValueAsString(new Tree(attr.getLabel() != null ? attr.getLabel() : "root")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            //todo sync?
        }
        return attr;
    }

    public Object[] prepareFulltextSearchData(AsmAttribute attr) {
        return null;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return new Tree("root");
        }
        try {
            return initTree((Tree) mapper.reader(Tree.class).readValue(attr.getEffectiveValue()), ctx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Tree initTree(Tree tree, AsmRendererContext ctx) throws IOException {
        if (!StringUtils.isBlank(tree.getNam())) {
            JsonNode namSpec = mapper.readTree(tree.getNam());
            if (!namSpec.hasNonNull("naClass")) {
                return tree;
            }
            String naClass = namSpec.get("naClass").asText();
            if (!"ncms.asm.am.RichRefAM".equals(naClass)) {
                return tree;
            }
            tree.setRichRef(richRefAM.renderAsmAttribute(ctx, (ObjectNode) namSpec));
        }
        if (tree.isHasChildren()) {
            for (Tree c : tree.getChildren()) {
                initTree(c, ctx);
            }
        }
        return tree;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts);
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        ObjectNode tree = (ObjectNode) val;
        if (tree != null) {
            try {
                saveTree(ctx, attr, tree);
            } catch (IOException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
            attr.setEffectiveValue(tree.toString());
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    private void saveTree(AsmAttributeManagerContext ctx, AsmAttribute attr, ObjectNode tree) throws IOException {
        String type = tree.hasNonNull("type") ? tree.get("type").asText() : null;
        Long id = tree.hasNonNull("id") ? tree.get("id").asLong() : null;
        if ("file".equals(type) && id != null) {
            ctx.registerMediaFileDependency(attr, id);
        }
        JsonNode val = tree.get("nam");
        if (val != null && val.isTextual()) {
            JsonNode naSpec = mapper.readTree(val.asText());
            String naClass = naSpec.hasNonNull("naClass") ? naSpec.get("naClass").asText() : null;
            if ("ncms.asm.am.RichRefAM".equals(naClass)) {
                richRefAM.applyJSONAttributeValue(ctx, attr, naSpec);
                tree.set("nam", tree.textNode(naSpec.toString()));
            }
        }
        val = tree.get("children");
        if (val instanceof ArrayNode) {
            for (JsonNode n : val) {
                if (n instanceof ObjectNode) {
                    saveTree(ctx, attr, (ObjectNode) n);
                }
            }
        }
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }
}
