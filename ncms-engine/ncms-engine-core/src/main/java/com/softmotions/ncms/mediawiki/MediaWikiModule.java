package com.softmotions.ncms.mediawiki;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bliki.htmlcleaner.TagToken;
import info.bliki.wiki.filter.ITextConverter;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.xconfig.XConfig;

/**
 * MediaWiki integration module.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
public class MediaWikiModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(MediaWikiModule.class);

    private final NcmsEnvironment env;

    public MediaWikiModule(NcmsEnvironment env) {
        this.env = env;
    }

    @Override
    protected void configure() {

        ClassLoader cl = ObjectUtils.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader()
        );

        XConfig xcfg = env.xcfg();
        MapBinder<String, TagToken> tagsBinder =
                MapBinder.newMapBinder(binder(), String.class, TagToken.class);

        List<XConfig> tcfgs = xcfg.subPattern("mediawiki.tags.tag");
        for (final XConfig tcfg : tcfgs) {
            String name = tcfg.textXPath("@name", null);
            if (StringUtils.isBlank(name)) {
                continue;
            }
            String className = tcfg.textXPath("@class", null);
            if (StringUtils.isBlank(className)) {
                continue;
            }
            try {
                Class<?> clazz = cl.loadClass(className);
                if (!TagToken.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Tag class: " + className +
                                               " does not implement: " + TagToken.class.getName());
                }
                tagsBinder.addBinding(name).to((Class<? extends TagToken>) clazz).in(Singleton.class);
            } catch (ClassNotFoundException e) {
                String msg = "Failed to load mediawiki tag class: " + className;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }

        MediaWikiConverter converter = new MediaWikiConverter(true);
        bind(MediaWikiServices.class).asEagerSingleton();
        bind(MediaWikiRenderer.class).in(Singleton.class);
        bind(ITextConverter.class).toInstance(converter);
        bind(MediaWikiRS.class).in(Singleton.class);
    }
}
