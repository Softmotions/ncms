package com.softmotions.ncms.mhttl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.CollectionUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.web.HttpUtils;

/**
 * Various template utils.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings({"unchecked"})
public final class HttlUtilsMethods {

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

    @Nullable
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

    @Nullable
    public static String requestParameter(String param) {
        if (param == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        HttpServletRequest req = ctx.getServletRequest();
        return req.getParameter(param);
    }

    public static boolean ifRequestParameter(String name, String value) {
        return Objects.equals(requestParameter(name), value);
    }

    @Nullable
    public static String cookie(String name) {
        if (name == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        HttpServletRequest req = ctx.getServletRequest();
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    public static boolean ifCookie(String name, String value) {
        return Objects.equals(cookie(name), value);
    }

    public static String requestLanguage() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getI18n().getLocale(ctx.getServletRequest()).getLanguage();
    }

    @Nullable
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
       return CollectionUtils.split(coll, size);
    }

    @Nullable
    public static String includeTemplate(Object path) {
        if (path == null) {
            return null;
        }
        String spath = path.toString();
        if (StringUtils.isBlank(spath)) {
            return spath;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        AsmRenderer renderer = ctx.getRenderer();
        StringWriter out = new StringWriter(1024);
        try {
            renderer.renderTemplate(spath, ctx, out);
        } catch (IOException e) {
            throw new AsmRenderingException("Failed to render template: '" + spath + '\'' +
                                            " asm: " + ctx.getAsm().getName() + " attribute: " + spath, e);
        }
        return out.toString();
    }

    @Nullable
    public static String siteFile(Object path) {
        if (path == null) {
            return null;
        }
        NcmsEnvironment env = env();
        String spath = path.toString();
        if (StringUtils.isBlank(spath)) {
            return spath;
        }
        if (sfRoot == null) {
            synchronized (HttlUtilsMethods.class) {
                if (sfRoot == null) {
                    String sfr = env.xcfg().getString("asm.site-files-root", "/site/");
                    if (!sfr.endsWith("/")) {
                        sfr += "/";
                    }
                    sfRoot = sfr;
                }
            }
        }
        if (spath.startsWith(sfRoot)) {
            spath = spath.substring(sfRoot.length() - 1);
        }
        if (!spath.startsWith(env.getAppRoot())) {
            spath = env.getAppRoot() + spath;
        }
        return spath;
    }

    @Nullable
    public static String format2(Date date, String format) {
        if (date == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getI18n().format(date, format, ctx.getLocale());
    }

    @Nullable
    public static String formatEng(Date date, String format) {
        if (date == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getI18n().format(date, format, Locale.ENGLISH);
    }

    @Nullable
    public static String translate(String key) {
        return translateImpl(key);
    }

    @Nullable
    public static String translate(String key, String v1) {
        return translateImpl(key, v1);
    }

    @Nullable
    public static String translate(String key, String v1, String v2) {
        return translateImpl(key, v1, v2);
    }

    @Nullable
    public static String translate(String key, String v1, String v2, String v3) {
        return translateImpl(key, v1, v2, v3);
    }

    @Nullable
    private static String translateImpl(String key, String... args) {
        if (key == null) {
            return null;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        try {
            return ctx.getI18n().get(key, ctx.getLocale(), (Object[]) args);
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
        HttpServletRequest req = ctx.getServletRequest();
        StringBuffer requestURL = req.getRequestURL();
        String requestURI = req.getRequestURI();
        return requestURL.substring(0, requestURL.length() - requestURI.length());
    }

    /**
     * Return true if we are in preview mode
     *
     * @return
     */
    public static boolean isPreview() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getPageService()
                  .getPageSecurityService()
                  .isPreviewPageRequest(ctx.getServletRequest());
    }

    ///////////////////////////////////////////////////////////
    //                     User agent detection              //
    ///////////////////////////////////////////////////////////

    public static boolean isAndroidMobile() {
        return HttpUtils.isAndroidMobile(AsmRendererContext.getSafe().getServletRequest());
    }

    public static boolean isAndroidTablet() {
        return HttpUtils.isAndroidTablet(AsmRendererContext.getSafe().getServletRequest());
    }

    public static boolean isIpad() {
        return HttpUtils.isIpad(AsmRendererContext.getSafe().getServletRequest());
    }

    public static boolean isIphone() {
        return HttpUtils.isIphone(AsmRendererContext.getSafe().getServletRequest());
    }

    public static boolean isMobile() {
        return HttpUtils.isMobile(AsmRendererContext.getSafe().getServletRequest());
    }

    public static boolean isTablet() {
        return HttpUtils.isTablet(AsmRendererContext.getSafe().getServletRequest());
    }

    ///////////////////////////////////////////////////////////
    //                Local date/time formatters             //
    ///////////////////////////////////////////////////////////

    @Nullable
    public static String format(LocalDate date) {
        return format(date, null);
    }

    @Nullable
    public static String format(LocalDate date, @Nullable String pattern) {
        if (date == null) {
            return null;
        }
        if (pattern == null) {
            AsmRendererContext ctx = AsmRendererContext.getSafe();
            pattern =
                    DateTimeFormatterBuilder
                            .getLocalizedDateTimePattern(FormatStyle.SHORT, null,
                                                         IsoChronology.INSTANCE,
                                                         ctx.getLocale());
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    @Nullable
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, null);
    }

    @Nullable
    public static String format(LocalDateTime dateTime, @Nullable String pattern) {
        if (dateTime == null) {
            return null;
        }
        if (pattern == null) {
            AsmRendererContext ctx = AsmRendererContext.getSafe();
            pattern =
                    DateTimeFormatterBuilder
                            .getLocalizedDateTimePattern(FormatStyle.SHORT, FormatStyle.SHORT,
                                                         IsoChronology.INSTANCE,
                                                         ctx.getLocale());
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
