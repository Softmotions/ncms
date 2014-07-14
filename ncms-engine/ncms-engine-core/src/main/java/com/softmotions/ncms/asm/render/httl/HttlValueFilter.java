package com.softmotions.ncms.asm.render.httl;

import httl.spi.filters.EscapeXmlFilter;
import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlValueFilter extends EscapeXmlFilter {

    public String filter(String key, String value) {
        if (escape()) {
            return super.filter(key, value);
        } else {
            return value;
        }
    }

    public char[] filter(String key, char[] value) {
        if (escape()) {
            return super.filter(key, value);
        } else {
            return value;
        }
    }

    public byte[] filter(String key, byte[] value) {
        if (escape()) {
            return super.filter(key, value);
        } else {
            return value;
        }
    }

    private static boolean escape() {
        AsmRendererContext ctx = AsmRendererContext.get();
        if (ctx == null) {
            return true;
        }
        if (ctx.isNextEscapeSkipping()) {
            ctx.setNextEscapeSkipping(false);
            return false;
        }
        return true;
    }
}
