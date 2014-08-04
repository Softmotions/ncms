package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.filter.ITextConverter;
import com.softmotions.commons.ebus.EBus;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.mediawiki.events.MediaWikiHTMLRenderEvent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.XMLConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MediaWikiRenderer {

    private final MediaWikiConfiguration wikiCfg;

    private final ITextConverter converter;

    private final String imageBaseUrl;

    private final String linkBaseUrl;

    private final EBus ebus;

    private NcmsMessages messages;


    @Inject
    public MediaWikiRenderer(NcmsConfiguration cfg,
                             MediaWikiConfiguration wikiCfg,
                             ITextConverter converter,
                             NcmsEventBus ebus,
                             NcmsMessages messages) {
        this.wikiCfg = wikiCfg;
        this.converter = converter;
        this.ebus = ebus;
        this.messages = messages;
        XMLConfiguration xcfg = cfg.impl();
        this.imageBaseUrl = xcfg.getString("mediawiki.image-base-url",
                                           cfg.getNcmsPrefix() + "/rs/mw/res/" + "${image}");
        this.linkBaseUrl = xcfg.getString("mediawiki.link-base-url",
                                          cfg.getNcmsPrefix() + "/rs/mw/link/" + "${title}");
    }

    public String render(String markup, HttpServletRequest req) {
        WikiModel wiki = new WikiModel(wikiCfg, imageBaseUrl, linkBaseUrl, messages, req);
        String html = wiki.render(this.converter, markup);
        ebus.fire(new MediaWikiHTMLRenderEvent(markup, html));
        return html;
    }
}
