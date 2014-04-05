package com.softmotions.ncms.asm.render.httl;

import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlAsmMethods {

    private HttlAsmMethods() {
    }

    @SuppressWarnings("unchecked")
    public static String asm(String val) {
        String attrName;
        Map<String, String> opts;
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        int ind = val.indexOf(',');
        if (ind != -1) {
            attrName = val.substring(0, ind);
            opts = (ind < val.length() - 1) ? new AsmOptions(val.substring(ind + 1)) : null;
        } else {
            attrName = val;
            opts = null;
        }
        return ctx.renderAttribute(attrName, opts);
    }
}
