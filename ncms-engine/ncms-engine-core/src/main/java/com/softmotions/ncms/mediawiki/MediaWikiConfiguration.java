package com.softmotions.ncms.mediawiki;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bliki.htmlcleaner.TagToken;
import info.bliki.wiki.model.Configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.xconfig.XConfig;

/**
 * Mediawiki configuration.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class MediaWikiConfiguration extends Configuration {

    private static final Logger log = LoggerFactory.getLogger(MediaWikiConfiguration.class);

    @Inject
    public MediaWikiConfiguration(Map<String, TagToken> tags, NcmsEnvironment env) {
        XConfig xcfg = env.xcfg();
        for (final Map.Entry<String, TagToken> e : tags.entrySet()) {
            log.info("Mediawiki custom tag: '{}' class: '{}' registered", e.getKey(), e.getValue().getClass().getName());
            this.addTokenTag(e.getKey(), e.getValue());
        }
        List<XConfig> cfgs = xcfg.subPattern("mediawiki.interwiki-links.link");
        for (final XConfig c : cfgs) {
            String key = c.textXPath("@key", "");
            String value = c.textXPath("@value", "");
            if (StringUtils.isBlank(key)) {
                continue;
            }
            this.addInterwikiLink(key, value);
            log.info("Interwiki link [{}, {}] added", key, value);
        }
        if (!this.getInterWikiMapping().containsKey("__global:page")
            && !this.getInterWikiMapping().containsKey("__global:Page")) {
            String link = "/$1";
            this.addInterwikiLink("page", link);
            log.info("Interwiki link [page, {}] added", link);
        }
    }
}
