package com.softmotions.ncms;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@ThreadSafe
public class NcmsMessages {

    public static final String NCMS_LNG_COOKIE_NAME = "NCMSLNG";

    public static final String NCMS_REQ_LOCALE_ATTR_NAME = "NCMSREQLOCALE";

    public static final String NCMS_REQ_LOCALE_PARAM_NAME = "lang";

    private static final ThreadLocal<Map<String, SimpleDateFormat>> LOCAL_SDF_CACHE = new ThreadLocal<>();

    @SuppressWarnings("StaticCollection")
    private static final Map<String, String[]> LNG_MONTHS = new HashMap<>();

    private static final String[] ISO2_LANGUAGES;

    static {
        ISO2_LANGUAGES = Locale.getISOLanguages();
        Arrays.sort(ISO2_LANGUAGES);
    }

    static {

        LNG_MONTHS.put("ru", new String[]{
                "января", "февраля", "марта", "апреля", "мая",
                "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"
        });
    }

    private final AggregateResourceBundleLocator bundleLocator;

    private final Map<Locale, ResourceBundle> bundleCache;

    @Inject
    public NcmsMessages(NcmsEnvironment env) {
        List<Object> blist = env.xcfg().getList("messages.bundle");
        if (!blist.contains("com.softmotions.ncms.Messages")) {
            blist.add("com.softmotions.ncms.Messages");
        }
        ArrayList<String> rnames = new ArrayList<>();
        for (Object v : blist) {
            String sv = (v != null) ? v.toString() : null;
            if (StringUtils.isBlank(sv)) {
                continue;
            }
            rnames.add(sv);
        }
        bundleLocator = new AggregateResourceBundleLocator(rnames);
        bundleCache = new ConcurrentHashMap<>();
    }

    @Nonnull
    public ResourceBundle getResourceBundle(Locale locale) {
        ResourceBundle bundle = bundleCache.get(locale);
        if (bundle == null) {
            bundle = bundleLocator.getResourceBundle(locale);
            if (bundle == null) {
                bundle = bundleLocator.getResourceBundle(Locale.ENGLISH);
            }
            if (bundle == null) {
                throw new RuntimeException("Unable to locate any resource bundle for locale: " + locale);
            }
            bundleCache.put(locale, bundle);
        }
        return bundle;
    }

    @Nonnull
    public ResourceBundle getResourceBundle(HttpServletRequest req) {
        return getResourceBundle(getLocale(req));
    }

    @Nullable
    public String get(String key, String... params) throws MissingResourceException {
        return get(key, Locale.getDefault(), params);
    }

    @Nullable
    public String get(String key, Locale locale, String... params) throws MissingResourceException {
        return String.format(locale, getResourceBundle(locale).getString(key), params);
    }

    @Nullable
    public String get(String key, HttpServletRequest req, String... params) throws MissingResourceException {
        return get(key, getLocale(req), params);
    }

    @Nonnull
    public Locale getLocale(HttpServletRequest req) {
        if (req == null) {
            return Locale.getDefault();
        }
        Locale l = (Locale) req.getAttribute(NCMS_REQ_LOCALE_ATTR_NAME);
        if (l == null) {
            l = new Locale(fetchRequestLanguage(req));
            req.setAttribute(NCMS_REQ_LOCALE_ATTR_NAME, l);
        }
        return l;
    }

    public boolean isValidISO2Language(String lang) {
        return (lang != null && Arrays.binarySearch(ISO2_LANGUAGES, lang) >= 0);
    }

    @Nonnull
    private String fetchRequestLanguage(HttpServletRequest req) {
        String lang = req.getParameter(NCMS_REQ_LOCALE_PARAM_NAME);
        if (StringUtils.isBlank(lang)) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (final Cookie c : cookies) {
                    if (NCMS_LNG_COOKIE_NAME.equals(c.getName())) {
                        lang = c.getValue();
                        break;
                    }
                }
            }
        }
        if (lang != null) {
            lang = lang.toLowerCase();
        }
        if (!isValidISO2Language(lang)) {
            lang = req.getLocale().getLanguage();
        }
        return isValidISO2Language(lang) ? lang : Locale.getDefault().getLanguage();
    }

    public void initRequestI18N(HttpServletRequest req, HttpServletResponse resp) {
        if (req == null) {
            return;
        }
        String lang = req.getParameter(NCMS_REQ_LOCALE_PARAM_NAME);
        if (StringUtils.isBlank(lang)) {
            return;
        }
        lang = fetchRequestLanguage(req);
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie c : cookies) {
                if (NCMS_LNG_COOKIE_NAME.equals(c.getName())) {
                    String clang = c.getValue();
                    if (Objects.equals(lang, clang)) {
                        return;
                    }
                    break;
                }
            }
        }
        Cookie c = new Cookie(NCMS_LNG_COOKIE_NAME, lang);
        c.setMaxAge(60 * 60 * 24 * 7); //1 week todo configurable
        resp.addCookie(c);
    }

    @Nonnull
    public String format(@Nonnull Date date,
                         @Nonnull String format,
                         @Nullable Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        if ("LMMMMM".equals(format)) {
            return getLocaleAwareMonth(date, locale);
        }
        Map<String, SimpleDateFormat> formatters = LOCAL_SDF_CACHE.get();
        if (formatters == null) {
            formatters = new HashMap<>();
            LOCAL_SDF_CACHE.set(formatters);
        }
        String key = locale.toString() + '@' + format;
        SimpleDateFormat sdf = formatters.get(key);
        if (sdf == null) {
            sdf = new SimpleDateFormat(format, locale);
            formatters.put(key, sdf);
        }
        return sdf.format(date);
    }

    @Nonnull
    private String getLocaleAwareMonth(@Nonnull Date date,
                                       @Nonnull Locale locale) {
        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(date);
        String lng = locale.getLanguage();
        String[] months = LNG_MONTHS.get(lng);
        if (months != null) {
            return months[cal.get(Calendar.MONTH)];
        }
        return format(cal.getTime(), "MMMMM", locale);
    }
}
