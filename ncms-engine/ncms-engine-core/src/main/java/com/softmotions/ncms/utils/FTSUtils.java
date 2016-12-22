package com.softmotions.ncms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import javax.annotation.Nullable;

import org.apache.tika.language.LanguageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.GermanStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;
import org.tartarus.snowball.ext.RussianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;

import com.softmotions.commons.cont.CollectionUtils;

/**
 * Full text search utilities.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class FTSUtils {

    private static final Logger log = LoggerFactory.getLogger(FTSUtils.class);

    private FTSUtils() {
    }


    public static Locale identifyLanguageLocale(String sampleText, Locale fallback) {
        String lang = new LanguageIdentifier(sampleText).getLanguage();
        if (lang == null || lang.equalsIgnoreCase("unknown")) {
            return fallback;
        }
        return new Locale(lang);
    }

    public static String[] stemWordsLangAware(String words,
                                              Locale fallback,
                                              int minimalTermChars) {
        StringTokenizer st = new StringTokenizer(words,
                                                 " \t\n\r\f,;.!?()$%#@*&-+=*^'\"/\\<>|'`~_");
        Set<String> wset = new HashSet<>();
        while (st.hasMoreTokens()) {
            wset.add(st.nextToken());
        }
        return stemWords(
                identifyLanguageLocale(CollectionUtils.join(" ", wset), fallback),
                fallback, wset, minimalTermChars);
    }


    public static String[] stemWordsLangAware(Collection<String> words,
                                              Locale fallback,
                                              int minimalTermChars) {
        return stemWords(
                identifyLanguageLocale(CollectionUtils.join(" ", words), fallback),
                fallback, words, minimalTermChars);
    }

    public static String[] stemWords(Locale locale,
                                     Locale fallback,
                                     Collection<String> words,
                                     int minimalTermChars) {

        SnowballProgram s = selectStemmer(locale, fallback);
        if (s == null) {
            return words.toArray(new String[words.size()]);
        }
        List<String> rl = new ArrayList<>(words.size());
        for (final String w : words) {
            s.setCurrent(w);
            s.stem();
            String c = s.getCurrent();
            if (c.length() >= minimalTermChars) {
                rl.add(c);
            }
        }
        return rl.toArray(new String[rl.size()]);
    }

    @Nullable
    private static SnowballProgram selectStemmer(Locale locale, Locale fallback) {
        SnowballProgram stemmer = selectStemmer(locale);
        //noinspection ObjectEquality
        if (stemmer == null && fallback != null && fallback != locale) {
            stemmer = selectStemmer(fallback);
        }
        return stemmer;
    }

    @Nullable
    private static SnowballProgram selectStemmer(Locale locale) {
        if (locale == null) {
            return null;
        }
        String lng = locale.getLanguage();
        lng = lng.toLowerCase(Locale.ENGLISH);
        if ("ru".equals(lng) || "rus".equals(lng)) {
            return new RussianStemmer();
        } else if ("en".equals(lng) || "eng".equals(lng)) {
            return new EnglishStemmer();
        } else if ("fr".equals(lng) || "fra".equals(lng)) {
            return new FrenchStemmer();
        } else if ("de".equals(lng) || "deu".equals(lng)) {
            return new GermanStemmer();
        } else if ("it".equals(lng) || "ita".equals(lng)) {
            return new ItalianStemmer();
        } else if ("es".equals(lng) || "spa".equals(lng)) {
            return new SpanishStemmer();
        }
        return null;
    }
}
