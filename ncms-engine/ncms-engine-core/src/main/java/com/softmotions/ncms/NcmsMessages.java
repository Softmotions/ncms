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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private static final ThreadLocal<Map<String, SimpleDateFormat>> LOCAL_SDF_CACHE = new ThreadLocal<>();

    @SuppressWarnings("StaticCollection")
    private static final Map<String, String[]> LNG_MONTHS = new HashMap<>();

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
    public String get(String key, String... params) {
        return get(key, Locale.getDefault(), params);
    }

    @Nullable
    public String get(String key, Locale locale, String... params) {
        return String.format(locale, getResourceBundle(locale).getString(key), params);
    }

    @Nullable
    public String get(String key, HttpServletRequest req, String... params) {
        return get(key, getLocale(req), params);
    }

    @Nonnull
    public Locale getLocale(HttpServletRequest req) {
        if (req == null) {
            return Locale.getDefault();
        }
        Locale l = (Locale) req.getAttribute(NCMS_REQ_LOCALE_ATTR_NAME);
        if (l != null) {
            return l;
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie c : cookies) {
                if (NCMS_LNG_COOKIE_NAME.equals(c.getName())) {
                    String val = c.getValue();
                    if (!StringUtils.isBlank(val)) {
                        l = new Locale(val);
                        req.setAttribute(NCMS_REQ_LOCALE_ATTR_NAME, l);
                        return l;
                    }
                    break;
                }
            }
        }
        return req.getLocale();
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
