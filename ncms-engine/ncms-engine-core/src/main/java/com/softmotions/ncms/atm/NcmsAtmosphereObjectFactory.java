package com.softmotions.ncms.atm;

import java.util.ArrayList;
import java.util.List;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.atmosphere.inject.AtmosphereConfigAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("SynchronizeOnThis")
public final class NcmsAtmosphereObjectFactory implements AtmosphereObjectFactory<AbstractModule> {

    private static final Logger log = LoggerFactory.getLogger(NcmsAtmosphereObjectFactory.class);

    private volatile Injector _injector;

    private AtmosphereConfig config;

    private final List<AbstractModule> modules = new ArrayList<>(1);

    @Override
    public void configure(AtmosphereConfig config) {
        log.info("Configuring nCMS atmosphere object factory");
        this.config = config;
        modules.add(new AtmosphereModule());
    }

    @Override
    public <T, U extends T> T newClassInstance(Class<T> classType,
                                               Class<U> defaultType)
            throws InstantiationException, IllegalAccessException {

        T ret = getInjector().getInstance(defaultType);
        if (ret instanceof AtmosphereConfigAware) {
            ((AtmosphereConfigAware) ret).configure(config);
        }
        return ret;
    }

    @Override
    public AtmosphereObjectFactory allowInjectionOf(AbstractModule m) {
        modules.add(m);
        return this;
    }

    private Injector getInjector() {
        if (_injector != null) {
            return _injector;
        }
        synchronized (this) {
            if (_injector == null) {
                Injector parent = (Injector) config.framework()
                                                   .getServletContext()
                                                   .getAttribute(Injector.class.getName());
                if (parent == null) {
                    throw new RuntimeException("Unable to find Guice Injector in the current servlet context");
                }
                log.info("Atmosphere guice child injector created");
                _injector = parent.createChildInjector(modules.toArray(new AbstractModule[modules.size()]));
            }
        }
        return _injector;
    }

    private class AtmosphereModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BroadcasterFactory.class).toProvider(() -> {
                return config.getBroadcasterFactory();
            });
            bind(AtmosphereFramework.class).toProvider(() -> {
                return config.framework();
            });
            bind(AtmosphereResourceFactory.class).toProvider(() -> {
                return config.resourcesFactory();
            });
            bind(MetaBroadcaster.class).toProvider(() -> {
                return config.metaBroadcaster();
            });
            bind(AtmosphereResourceSessionFactory.class).toProvider(() -> {
                return config.sessionFactory();
            });
            bind(AtmosphereConfig.class).toProvider(() -> config);
        }
    }
}
