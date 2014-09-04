package com.softmotions.ncms.mhttl;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Various template utils.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlUtilsMethods {

    private HttlUtilsMethods() {
    }

    public static boolean requestParamMatched(String param, String value) {
        if (param == null) {
            return false;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        HttpServletRequest req = ctx.getServletRequest();
        return Objects.equals(req.getParameter(param), value);
    }

    public static <T> List<T> randomSublist(Collection<T> coll, int max) {
        List<T> cc = new ArrayList<>(coll);
        Collections.shuffle(cc);
        return (cc.size() == max) ? cc : cc.subList(0, max);
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
