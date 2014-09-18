package com.softmotions.ncms.mhttl;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    private static final Logger log = LoggerFactory.getLogger(HttlUtilsMethods.class);

    private HttlUtilsMethods() {
    }

    public static String ifTrue(boolean res, String data) {
        return res ? data : null;
    }

    public static String ifTrue(boolean res, String data, String otherwise) {
        return res ? data : otherwise;
    }

    public static boolean requestParamMatched(String param, int value) {
        return requestParamMatched(param, Integer.toString(value));
    }

    public static boolean requestParamMatched(String param, String value) {
        if (param == null) {
            return false;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        HttpServletRequest req = ctx.getServletRequest();
        return Objects.equals(req.getParameter(param), value);
    }


    public static String encodeUriComponent(String s) {
        if (s == null) {
            return null;
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
        }
        return null;
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

    public static String alias(String alias) {
        return (alias != null) ? AsmRendererContext.getSafe().getCfg().getAsmLink(alias) : null;
    }
}
