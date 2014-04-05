package com.softmotions.ncms.asm.render.httl;

import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlAsmMethods {

    private HttlAsmMethods() {
    }

    public static String asm(String val) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        String attrName = val;
        Map<String, Object> opts = null;
        //todo opts
        return ctx.renderAsmAttribute(attrName, opts);
    }
}
