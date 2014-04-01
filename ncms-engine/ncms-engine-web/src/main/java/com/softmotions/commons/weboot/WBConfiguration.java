package com.softmotions.commons.weboot;


import ninja.utils.NinjaProperties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class WBConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WBConfiguration.class);

    protected final NinjaProperties ninjaProperties;

    protected final XMLConfiguration xcfg;

    protected WBConfiguration(NinjaProperties ninjaProperties) {
        this(ninjaProperties, null, true);
    }

    protected WBConfiguration(NinjaProperties ninjaProperties, String cfgResource, boolean resource) {
        this.ninjaProperties = ninjaProperties;
        URL cfgUrl = toCfgUrl(cfgResource, resource);
        if (cfgUrl == null) {
            throw new RuntimeException("Unable to find configuration: " + cfgResource);
        }
        try {
            xcfg = new XMLConfiguration(cfgUrl);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public XMLConfiguration impl() {
        return xcfg;
    }

    public abstract String getEnvironmentType();

    public abstract String getDBEnvironmentType();

    public NinjaProperties getNinjaProperties() {
        return ninjaProperties;
    }

    URL toCfgUrl(String cfgResource, boolean resource) {
        if (cfgResource == null) {
            return null;
        }
        URL cfgUrl = null;
        if (resource) {
            ClassLoader cl =
                    ObjectUtils.firstNonNull(Thread.currentThread().getContextClassLoader(),
                                             getClass().getClassLoader());
            cfgUrl = cl.getResource(cfgResource);
        }
        if (cfgUrl == null) {
            InputStream is = null;
            try {
                cfgUrl = new URL(cfgResource);
                is = cfgUrl.openStream();
            } catch (IOException e) {
                cfgUrl = null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
            File cfgFile = new File(cfgResource);
            if (cfgFile.exists()) {
                try {
                    cfgUrl = cfgFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    log.error("", e);
                }
            }
        }
        return cfgUrl;
    }
}
