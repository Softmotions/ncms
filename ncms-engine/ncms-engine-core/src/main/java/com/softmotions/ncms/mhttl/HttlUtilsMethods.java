package com.softmotions.ncms.mhttl;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.util.Date;

/**
 * Various template utils.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlUtilsMethods {

    private HttlUtilsMethods() {
    }

    public static String link(Asm asm) {
        return (asm != null) ? AsmRendererContext.getSafe().getCfg().getAsmLink(asm.getName()) : null;
    }

    public static String format2(Date date, String format) {
        if (date == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().format(date, format, ctx.getLocale());
    }
}
