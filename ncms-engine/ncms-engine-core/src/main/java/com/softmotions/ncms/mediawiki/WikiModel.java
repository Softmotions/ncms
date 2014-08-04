package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.ImageFormat;
import com.softmotions.ncms.NcmsMessages;

import net.jcip.annotations.NotThreadSafe;

import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@NotThreadSafe

public class WikiModel extends info.bliki.wiki.model.WikiModel {

    private final Configuration cfg;

    private final NcmsMessages messages;

    private final HttpServletRequest req;

    private ResourceBundle bundle;


    protected void setDefaultThumbWidth(ImageFormat imageFormat) {
        //noop
    }


    public ResourceBundle getResourceBundle() {
        if (bundle != null) {
            return bundle;
        }
        bundle = messages.getResourceBundle(req);
        return bundle;
    }

    public WikiModel(WikiModel src) {
        this(src.cfg, src.getImageBaseURL(), src.getWikiBaseURL(),
             src.messages, src.req);
    }

    public WikiModel(Configuration cfg,
                     String imageBaseURL, String linkBaseURL,
                     NcmsMessages messages,
                     HttpServletRequest req) {
        super(cfg, imageBaseURL, linkBaseURL);
        this.cfg = cfg;
        this.messages = messages;
        this.req = req;
    }
}
