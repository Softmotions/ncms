package com.softmotions.ncms.mhttl;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Various template utils.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlUtilsMethods {

    private static final Logger log = LoggerFactory.getLogger(HttlUtilsMethods.class);

    private HttlUtilsMethods() {
    }

    public static <T> boolean notNull(T t) {
        return (t != null);
    }

    public static boolean blank(String str) {
        return StringUtils.isBlank(str);
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

    public static <T> Collection<Collection<T>> split(Iterable<T> coll, int size) {
        if (size < 1) {
            return Collections.EMPTY_LIST;
        }
        final List<Collection<T>> ret = new ArrayList<>();
        final Iterator<T> it = coll.iterator();
        Collection<T> box = null;
        for (int i = 0; it.hasNext(); ++i) {
            if (i % size == 0) {
                if (box != null) {
                    ret.add(box);
                }
                box = new ArrayList<>(size);
            }
            //noinspection ConstantConditions
            box.add(it.next());
        }
        if (box != null) {
            ret.add(box);
        }
        return ret;
    }

    public static String link(Asm asm) {
        return (asm != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(asm.getName()) : null;
    }

    public static String link(String alias) {
        return (alias != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(alias) : null;
    }

    public static String resolve(String link) {
        return (link != null) ? AsmRendererContext.getSafe().getPageService().resolvePageLink(link) : null;
    }

    public static String format2(Date date, String format) {
        if (date == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().format(date, format, ctx.getLocale());
    }

    public static String formatEng(Date date, String format) {
        if (date == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().format(date, format, Locale.ENGLISH);
    }

    public static String translate(String key) {
        if (key == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().get(key, ctx.getLocale());
    }

    public static String translate(String key, String v1) {
        if (key == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().get(key, ctx.getLocale(), v1);
    }

    public static String translate(String key, String v1, String v2) {
        if (key == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().get(key, ctx.getLocale(), v1, v2);
    }

    public static String translate(String key, String v1, String v2, String v3) {
        if (key == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().get(key, ctx.getLocale(), v1, v2, v3);
    }


    public static XMLConfiguration config() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        NcmsEnvironment env = ctx.getInjector().getInstance(NcmsEnvironment.class);

        return env.xcfg();
    }

    public static NcmsEnvironment env() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getInjector().getInstance(NcmsEnvironment.class);
    }

    public static String rootUrl() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        HttpServletRequest request = ctx.getServletRequest();
        StringBuffer requestURL = request.getRequestURL();
        String requestURI = request.getRequestURI();
        return requestURL.substring(0, requestURL.length() - requestURI.length());
    }
}
