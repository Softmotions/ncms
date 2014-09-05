package com.softmotions.ncms;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class NcmsMessages {

    private static final Logger log = LoggerFactory.getLogger(NcmsMessages.class);

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
    public NcmsMessages(NcmsConfiguration cfg) {
        List<Object> blist = cfg.impl().getList("messages.bundle");
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
        log.info("BLIST=" + blist);
        bundleLocator = new AggregateResourceBundleLocator(rnames);
        bundleCache = new ConcurrentHashMap<>();
    }


    public ResourceBundle getResourceBundle(Locale locale) {
        ResourceBundle bundle = bundleCache.get(locale);
        if (bundle == null) {
            bundle = bundleLocator.getResourceBundle(locale);
            if (bundle != null) {
                bundleCache.put(locale, bundle);
            }
        }
        return bundle;
    }

    public ResourceBundle getResourceBundle(HttpServletRequest req) {
        return getResourceBundle(getLocale(req));
    }

    public String get(String key, String... params) {
        return get(key, Locale.getDefault(), params);
    }

    public String get(String key, Locale locale, String... params) {
        ResourceBundle bundle = getResourceBundle(locale);
        if (bundle == null) {
            return null;
        }
        String msg = bundle.getString(key);
        return (msg != null) ? String.format(locale, msg, params) : null;
    }

    public String get(String key, HttpServletRequest req, String... params) {
        return get(key, getLocale(req), params);
    }

    public Locale getLocale(HttpServletRequest req) {
        if (req == null) {
            return Locale.getDefault();
        }
        //todo locale selection
        return Locale.getDefault();
    }

    public String format(Date date, String format, Locale locale) {
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


    private String getLocaleAwareMonth(Date date, Locale locale) {
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
