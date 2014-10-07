package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.filter.ITextConverter;
import com.softmotions.commons.ebus.EBus;
import com.softmotions.ncms.NcmsEnvironment;
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
    public MediaWikiRenderer(NcmsEnvironment env,
                             MediaWikiConfiguration wikiCfg,
                             ITextConverter converter,
                             NcmsEventBus ebus,
                             NcmsMessages messages) {
        this.wikiCfg = wikiCfg;
        this.converter = converter;
        this.ebus = ebus;
        this.messages = messages;
        XMLConfiguration xcfg = env.xcfg();
        this.imageBaseUrl = env.getNcmsPrefix() + xcfg.getString("mediawiki.image-base-url", "/rs/mw/res/" + "${image}");
        this.linkBaseUrl = env.getNcmsPrefix() + xcfg.getString("mediawiki.link-base-url", "/rs/mw/link/" + "${title}");
    }

    public String render(String markup, HttpServletRequest req) {
        WikiModel wiki = new WikiModel(wikiCfg, imageBaseUrl, linkBaseUrl, messages, req);
        String html = wiki.render(this.converter, markup);
        ebus.fire(new MediaWikiHTMLRenderEvent(markup, html));
        return html;
    }
}
