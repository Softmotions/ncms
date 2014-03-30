package com.softmotions.ncms;


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
 * Various Ncms constants.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsConfiguration {

    public static final String DEFAULT_CFG_RESOURCE = "com/softmotions/ncms/default-configuration.xml";

    private static final Logger log = LoggerFactory.getLogger(NcmsConfiguration.class);

    final NinjaProperties ninjaProperties;

    final XMLConfiguration xcfg;

    public NcmsConfiguration(NinjaProperties ninjaProperties) {
        this(ninjaProperties, null, true);
    }

    public NcmsConfiguration(NinjaProperties ninjaProperties, String cfgResource, boolean resource) {
        this.ninjaProperties = ninjaProperties;
        URL cfgUrl = toCfgUrl(cfgResource, resource);
        if (cfgUrl == null) {
            log.warn("Provided configuration path: " + cfgResource + " not found, " +
                     "fallback to the default: " + DEFAULT_CFG_RESOURCE);
            cfgUrl = toCfgUrl(DEFAULT_CFG_RESOURCE, true);
        }
        if (cfgUrl == null) {
            throw new RuntimeException("Unable to find ncms configuration: " + cfgResource);
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

    public String getEnvironmentType() {
        //application.environment.type
        String etype = ninjaProperties.get("application.environment.type");
        if (etype == null) {
            throw new RuntimeException("Missing required 'application.environment.type' " +
                                       "property in 'application.conf'");
        }
        return etype;
    }

    public NinjaProperties getNinjaProperties() {
        return ninjaProperties;
    }

    public String getNcmsPrefix() {
        String p = impl().getString("ncms-prefix", "/ncms");
        return p.charAt(0) != '/' ? "/" + p : p;
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
