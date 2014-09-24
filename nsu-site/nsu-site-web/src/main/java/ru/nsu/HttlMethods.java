package ru.nsu;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.util.Collections;
import java.util.Date;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlMethods {

    private HttlMethods() {
    }

    public static Date eventDate(Asm n) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        Date date = ctx.getAsm().getEdate();
        if (date != null) {
            return date;
        }
        if (n.isHasAttribute("event_date")) {
            date = (Date) ctx.renderAttribute(n, "event_date", Collections.EMPTY_MAP);
        }
        if (date == null) {
            date = n.getCdate();
        }
        return date;
    }
}
