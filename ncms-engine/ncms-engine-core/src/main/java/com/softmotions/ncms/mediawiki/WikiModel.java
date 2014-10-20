package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.ImageFormat;
import com.softmotions.ncms.NcmsMessages;

import net.jcip.annotations.NotThreadSafe;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@NotThreadSafe

public class WikiModel extends info.bliki.wiki.model.WikiModel {

    private final Configuration cfg;

    private final NcmsMessages messages;

    private final Locale locale;

    private ResourceBundle bundle;


    protected void setDefaultThumbWidth(ImageFormat imageFormat) {
        //noop
    }


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
}
