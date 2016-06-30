package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmStringAM implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"*", "string"};

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
        return new String[]{attr.getEffectiveValue()};
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        return attr.getEffectiveValue();
    }


    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "display", "placeholder", "maxLength");
        attr.setOptions(asmOpts.toString());
        attr.setEffectiveValue(val.hasNonNull("value") ? val.get("value").asText() : null);
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? val.get("value").asText() : null);
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
