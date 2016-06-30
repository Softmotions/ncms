package com.softmotions.ncms.mhttl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * Various template utils.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlUtilsMethods {

    private static final Logger log = LoggerFactory.getLogger(HttlUtilsMethods.class);

    private static volatile String sfRoot = null;

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

    public static String requestLanguage() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getMessages().getLocale(ctx.getServletRequest()).getLanguage();
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

    public static String siteFile(Object path) {
        if (path == null) {
            return null;
        }
        String spath = path.toString();
        if (StringUtils.isBlank(spath)) {
            return spath;
        }
        if (sfRoot == null) {
            synchronized (HttlUtilsMethods.class) {
                if (sfRoot == null) {
                    String sfr = env().xcfg().getString("asm.site-files-root", "");
                    if (!sfr.endsWith("/")) {
                        sfr += "/";
                    }
                    sfRoot = sfr;
                }
            }
        }
        if (spath.startsWith(sfRoot)) {
            return spath.substring(sfRoot.length());
        } else {
            return spath;
        }
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
        return translateImpl(key);
    }

    public static String translate(String key, String v1) {
        return translateImpl(key, v1);
    }

    public static String translate(String key, String v1, String v2) {
        return translateImpl(key, v1, v2);
    }

    public static String translate(String key, String v1, String v2, String v3) {
        return translateImpl(key, v1, v2, v3);
    }

    private static String translateImpl(String key, String... args) {
        if (key == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        try {
            return ctx.getMessages().get(key, ctx.getLocale(), args);
        } catch (MissingResourceException e) {
            log.error("", e);
            return null;
        }
    }

    public static HierarchicalConfiguration<ImmutableNode> config() {
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
