package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.model.Configuration;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class WikiModel extends info.bliki.wiki.model.WikiModel {

    private Configuration cfg;

    public WikiModel(WikiModel src) {
        this(src.cfg, src.getImageBaseURL(), src.getWikiBaseURL());
    }

    public WikiModel(Configuration cfg, String imageBaseURL, String linkBaseURL) {
        super(cfg, imageBaseURL, linkBaseURL);
        this.cfg = cfg;
    }
}
