package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
public class AsmBooleanAM extends AsmAttributeManagerSupport {

    public static final String[] TYPES = new String[]{"boolean"};

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
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            attr.setEffectiveValue("false");
        }
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return new Boolean[]{BooleanUtils.toBooleanObject(attr.getEffectiveValue())};
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        return attr == null ? Boolean.FALSE : BooleanUtils.toBooleanObject(attr.getValue());
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "display");
        attr.setOptions(asmOpts.toString());
        return applyAttributeValue(ctx, attr, val);
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        JsonNode n = val.get("value");
        if (n != null && BooleanUtils.toBoolean(n.asText())) {
            attr.setEffectiveValue("true");
        } else {
            attr.setEffectiveValue("false");
        }
        return attr;
    }
}
