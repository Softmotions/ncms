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
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.commons.cont.ArrayUtils;
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

    @SuppressWarnings("StaticNonFinalField")
    private static volatile String sfRoot = null;

    private HttlUtilsMethods() {
    }

    public static boolean in(String el, String[] args) {
        return !(el == null || args == null) && (ArrayUtils.indexOf(args, el) != -1);
    }

    public static boolean inICase(String el, String[] args) {
        if (el == null || args == null) {
            return false;
        }
        el = el.toLowerCase();
        for (int i = 0; i < args.length; ++i) {
            args[i] = args[i].toLowerCase();
        }
        return in(el, args);
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

    public static String requestParameter(String param) {
        if (param == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        HttpServletRequest req = ctx.getServletRequest();
        return req.getParameter(param);
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

    public static String link(RichRef ref) {
        if (ref == null) {
            return null;
        }
        return ref.getLink();
    }

    public static String linkHtml(Object ref) {
        return linkHtml(ref, null);
    }

    public static String linkHtml(Object ref, Map<String, String> attrs) {
        if (ref == null) {
            return null;
        }
        //noinspection ChainOfInstanceofChecks
        if (ref instanceof Tree) {
            return ((Tree) ref).toHtmlLink(attrs);
        }
        if (ref instanceof String) {
            ref = new RichRef((String) ref, AsmRendererContext.getSafe().getPageService());
        }
        if (!(ref instanceof RichRef)) {
            return null;
        }
        return ((RichRef) ref).toHtmlLink(attrs);
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
            return spath.substring(sfRoot.length() - 1);
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

    public static String config(String key) {
        HierarchicalConfiguration<ImmutableNode> config = config();
        return config.getString(key);
    }

    public static String config(String key, String def) {
        HierarchicalConfiguration<ImmutableNode> config = config();
        return config.getString(key, def);
    }

    public static HierarchicalConfiguration<ImmutableNode> config() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getEnvironment().xcfg();
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
