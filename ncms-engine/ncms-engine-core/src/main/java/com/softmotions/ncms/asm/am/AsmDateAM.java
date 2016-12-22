package com.softmotions.ncms.asm.am;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmDateAM extends AsmAttributeManagerSupport {

    public static final String[] TYPES = new String[]{"date"};

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        String val = attr.getEffectiveValue();
        if (val != null) {
            try {
                return new Long[]{Long.parseLong(val)};
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx,
                                     String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        return LocalDateTime.parse(attr.getEffectiveValue());
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                              AsmAttribute attr,
                                              JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts, "format");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr,
                                            JsonNode val) throws Exception {
        attr.setEffectiveValue(val.path("value").asText(null));
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx,
                                   AsmAttribute attr,
                                   JsonNode val,
                                   JsonNode opts) throws Exception {

        // todo adao.asmSetEdate(ctx.getAsmId(),
    }
}
