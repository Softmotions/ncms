package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.filter.ITextConverter;
import com.softmotions.ncms.NcmsConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MediaWikiRenderer {

    final NcmsConfiguration cfg;

    final MediaWikiConfiguration wikiCfg;

    final ITextConverter converter;

    final String imageBaseUrl;

    final String linkBaseUrl;

    @Inject
    public MediaWikiRenderer(NcmsConfiguration cfg,
                             MediaWikiConfiguration wikiCfg,
                             ITextConverter converter) {
        this.cfg = cfg;
        this.wikiCfg = wikiCfg;
        this.converter = converter;
        XMLConfiguration xcfg = cfg.impl();
        this.imageBaseUrl = xcfg.getString("mediawiki.image-base-url", "${image}");
        this.linkBaseUrl = xcfg.getString("mediawiki.link-base-url", "${title}");
    }

    public String render(String markup) {
        WikiModel wiki = new WikiModel(wikiCfg, imageBaseUrl, linkBaseUrl);
        return wiki.render(this.converter, markup);
    }
}
