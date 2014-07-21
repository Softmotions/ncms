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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
public class AsmTreeAttributeManager implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmTreeAttributeManager.class);

    private static final String[] TYPES = new String[]{"tree"};

    private final ObjectMapper mapper;

    @Inject
    public AsmTreeAttributeManager(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
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

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return new Tree("root");
        }
        try {
            return mapper.reader(Tree.class).readTree(attr.getEffectiveValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "allowPages", "allowFiles", "allowExternal",
                                        "nestingLevel");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        attr.setEffectiveValue(val != null ? val.toString() : null);
        return attr;
    }
}
