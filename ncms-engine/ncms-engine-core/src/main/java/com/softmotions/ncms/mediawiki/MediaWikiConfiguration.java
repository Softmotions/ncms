package com.softmotions.ncms.mediawiki;

import info.bliki.htmlcleaner.TagToken;
import info.bliki.wiki.model.Configuration;
import com.softmotions.ncms.NcmsEnvironment;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Mediawiki configuration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MediaWikiConfiguration extends Configuration {

    private static final Logger log = LoggerFactory.getLogger(MediaWikiConfiguration.class);

    @Inject
    public MediaWikiConfiguration(Map<String, TagToken> tags, NcmsEnvironment env) {
        XMLConfiguration xcfg = env.xcfg();
        for (final Map.Entry<String, TagToken> e : tags.entrySet()) {
            log.info("Mediawiki custom tag: '" + e.getKey() +
                     "' class: '" + e.getValue().getClass().getName() +
                     "' registered");
            this.addTokenTag(e.getKey(), e.getValue());
        }
        List<HierarchicalConfiguration> cfgs = xcfg.configurationsAt("mediawiki.interwiki-links.link");
        for (final HierarchicalConfiguration c : cfgs) {
            String key = c.getString("[@key]");
            String value = c.getString("[@value]");
            if (StringUtils.isBlank(key)) {
                continue;
            }
            this.addInterwikiLink(key, value);
            log.info("Interwiki link [" + key + ", " + value + "] added");
        }
        if (!this.getInterwikiMap().containsKey("page") && !this.getInterwikiMap().containsKey("Page")) {
            String link = env.getServletContext().getContextPath() + env.getNcmsPrefix() + "/asm/{title}";
            this.addInterwikiLink("page", link);
            log.info("Interwiki link [page, " + link + "] added");
        }
    }
}
