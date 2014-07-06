package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.model.Configuration;

import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class WikiModel extends info.bliki.wiki.model.WikiModel {

    public WikiModel(WikiModel src) {
        this(src.getImageBaseURL(), src.getWikiBaseURL());
    }

    public WikiModel(String imageBaseURL, String linkBaseURL) {
        super(Configuration.DEFAULT_CONFIGURATION, Locale.getDefault(), imageBaseURL, linkBaseURL);
    }

}
