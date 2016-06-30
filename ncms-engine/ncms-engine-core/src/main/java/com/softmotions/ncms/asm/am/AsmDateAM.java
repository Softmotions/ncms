package com.softmotions.ncms.asm.am;

import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmDateAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmDateAM.class);

    public static final String[] TYPES = new String[]{"date"};

    private final AsmDAO adao;

    @Inject
    public AsmDateAM(AsmDAO adao) {
        this.adao = adao;
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
        try {
            return new Long[]{Long.parseLong(attr.getEffectiveValue())};
        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        return asm.getEdate();
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts, "format");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? val.get("value").asText() : null);
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {
        if (val == null) {
            return;
        }
        val = val.get("value");
        adao.asmSetEdate(ctx.getAsmId(),
                         (val == null || val.asLong(0) == 0L) ? null :
                         new Date(val.asLong()));
    }
}
