package com.softmotions.ncms.mediawiki;

import java.util.Locale;
import java.util.ResourceBundle;

import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.ImageFormat;
import net.jcip.annotations.NotThreadSafe;

import com.softmotions.ncms.NcmsMessages;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@NotThreadSafe

public class WikiModel extends info.bliki.wiki.model.WikiModel {

    private final Configuration cfg;

    private final NcmsMessages messages;

    private final Locale locale;

    private ResourceBundle bundle;


    @Override
    protected void setDefaultThumbWidth(ImageFormat imageFormat) {
        //noop
    }


    @Override
    public ResourceBundle getResourceBundle() {
        if (bundle != null) {
            return bundle;
        }
        bundle = messages.getResourceBundle(locale);
        return bundle;
    }

    public WikiModel(WikiModel src) {
        this(src.cfg, src.getImageBaseURL(), src.getWikiBaseURL(),
             src.messages, src.locale);
    }

    public WikiModel(Configuration cfg,
                     String imageBaseURL, String linkBaseURL,
                     NcmsMessages messages,
                     Locale locale) {
        super(cfg, imageBaseURL, linkBaseURL);
        this.cfg = cfg;
        this.messages = messages;
        this.locale = locale;
    }

    @Override
    public String encodeTitleToUrl(String wikiTitle, boolean firstCharacterAsUpperCase) {
        return Encoder.normaliseTitle(wikiTitle, false, ' ', firstCharacterAsUpperCase, true);
    }
}
