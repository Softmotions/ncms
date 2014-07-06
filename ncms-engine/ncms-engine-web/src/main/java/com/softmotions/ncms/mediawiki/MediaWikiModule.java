package com.softmotions.ncms.mediawiki;

import info.bliki.htmlcleaner.TagToken;
import com.softmotions.ncms.NcmsConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MediaWiki integration module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaWikiModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(MediaWikiModule.class);

    private final NcmsConfiguration cfg;

    public MediaWikiModule(NcmsConfiguration cfg) {
        this.cfg = cfg;
    }

    protected void configure() {

        ClassLoader cl = ObjectUtils.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader()
        );

        XMLConfiguration xcfg = cfg.impl();
        Multibinder<TagToken> tagsBinder =
                Multibinder.newSetBinder(binder(), TagToken.class);

        List<HierarchicalConfiguration> tcfgs = xcfg.configurationsAt("mediawiki.tags.tag");
        for (final HierarchicalConfiguration tcfg : tcfgs) {
            String className = tcfg.getString("[@class]");
            if (StringUtils.isBlank(className)) {
                continue;
            }
            try {
                Class<?> clazz = cl.loadClass(className);
                if (!TagToken.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Tag class: " + className +
                                               " does not implement: " + TagToken.class.getName());
                }
                //noinspection unchecked
                tagsBinder.addBinding().to((Class<? extends TagToken>) clazz).in(Singleton.class);
                log.info("Mediawiki custom tag: " + className + " registered");
            } catch (ClassNotFoundException e) {
                String msg = "Failed to load mediawiki tag class: " + className;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    }
}
