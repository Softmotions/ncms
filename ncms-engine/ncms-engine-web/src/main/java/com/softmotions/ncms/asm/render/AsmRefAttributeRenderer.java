package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRefAttributeRenderer implements AsmAttributeRenderer {

    private static final Logger log = LoggerFactory.getLogger(AsmRefAttributeRenderer.class);

    public static final String[] TYPES = new String[]{"asmref"};

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public String renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {

        StringWriter out = new StringWriter(4096);
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String asmName = attr.getEffectiveValue();
        AsmRendererContext subcontext = null;
        HttpServletResponse resp;

        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        try {
            subcontext = ctx.createSubcontext(asmName, out);
            subcontext.push();
            subcontext.render();
        } catch (Exception e) {
            log.warn("Exception " + e.getMessage() +
                     " during sub-assembly rendering: " + asmName +
                     " of assembly: " + asm.getName() +
                     " attribute: " + attr.getName(), e);
            return null;
        } finally {
            if (subcontext != null) {
                subcontext.pop();
            }
        }
        if (subcontext != null) {
            resp = subcontext.getServletResponse();
            if (resp.getStatus() != HttpServletResponse.SC_OK) {
                log.warn("Unexpected status code: " + resp.getStatus() +
                         " during sub-assembly rendering: " + asmName +
                         " of assembly: " + asm.getName() +
                         " attribute: " + attr.getName());
                return null;
            }
        }
        //Schedule skip escaping on this attribute
        ctx.setNextEscapeSkipping(true);
        return out.toString();
    }
}
