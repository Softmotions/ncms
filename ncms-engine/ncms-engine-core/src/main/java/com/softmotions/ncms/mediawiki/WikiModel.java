package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.ImageFormat;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class WikiModel extends info.bliki.wiki.model.WikiModel {

    private Configuration cfg;

    protected void setDefaultThumbWidth(ImageFormat imageFormat) {
        //noop
    }

    public WikiModel(WikiModel src) {
        this(src.cfg, src.getImageBaseURL(), src.getWikiBaseURL());
    }

    public WikiModel(Configuration cfg, String imageBaseURL, String linkBaseURL) {
        super(cfg, imageBaseURL, linkBaseURL);
        this.cfg = cfg;
    }
}
