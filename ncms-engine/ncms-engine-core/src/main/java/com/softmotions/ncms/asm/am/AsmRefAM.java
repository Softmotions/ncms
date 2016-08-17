package com.softmotions.ncms.asm.am;

import java.io.StringWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmRefAM extends AsmAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmRefAM.class);

    public static final String[] TYPES = new String[]{"asmref"};

    private final AsmDAO adao;

    @Inject
    public AsmRefAM(AsmDAO adao) {
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
        return null;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx,
                                     String attrname,
                                     Map<String, String> options) throws AsmRenderingException {

        StringWriter out;
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null) {
            return null;
        }
        String asmName = attr.getEffectiveValue();
        AsmRendererContext subcontext;
        HttpServletResponse resp;
        if (attr.getEffectiveValue() == null) {
            return null;
        }

        out = new StringWriter(4096);
        try {
            subcontext = ctx.createSubcontext(asmName, out);
            subcontext.render(null);
        } catch (Exception e) {
            log.warn("Exception {} during sub-assembly rendering: {} asm: {} attribute: {}",
                     e.getMessage(), asmName, asm.getName(), attr.getName(), e);
            return null;
        }
        resp = subcontext.getServletResponse();
        if (resp.getStatus() != HttpServletResponse.SC_OK) {
            log.warn("Unexpected status code: {} during sub-assembly rendering: {} asm: {} attribute: {}",
                     resp.getStatus(), asmName, asm.getName(), attr.getName());
            return null;
        }
        //Schedule skip escaping on this attribute
        //ctx.setNextEscapeSkipping(true);
        return out.toString();
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return applyAttributeValue(ctx, attr, val);
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        if (!val.get("value").canConvertToLong()) {
            attr.setEffectiveValue(null);
            return attr;
        }
        String name = adao.asmSelectNameById(val.get("value").asLong());
        attr.setEffectiveValue(name);
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
