package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.PlainTextConverter;
import com.softmotions.commons.ebus.EBus;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.mediawiki.events.MediaWikiHTMLRenderEvent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Locale;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MediaWikiRenderer {

    private final MediaWikiConfiguration wikiCfg;

    private final ITextConverter converter;

    private final ITextConverter plaintextConverter;

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
        this.plaintextConverter = new PlainTextConverter(this.converter.noLinks());
        this.ebus = ebus;
        this.messages = messages;
        HierarchicalConfiguration<ImmutableNode> xcfg = env.xcfg();
        this.imageBaseUrl = env.getAppPrefix() + xcfg.getString("mediawiki.image-base-url", "/rs/mw/res/" + "${image}");
        this.linkBaseUrl = env.getAppPrefix() + xcfg.getString("mediawiki.link-base-url", "/rs/mw/link/" + "${title}");
    }

    public String render(String markup, Locale locale) {
        WikiModel wiki = new WikiModel(wikiCfg, imageBaseUrl, linkBaseUrl, messages, locale);
        String html = wiki.render(this.converter, markup);
        ebus.fire(new MediaWikiHTMLRenderEvent(markup, html));
        return html;
    }

    public String toText(String markup) {
        WikiModel wiki = new WikiModel(wikiCfg, imageBaseUrl, linkBaseUrl, messages, null);
        return wiki.render(plaintextConverter, markup);
    }
}
