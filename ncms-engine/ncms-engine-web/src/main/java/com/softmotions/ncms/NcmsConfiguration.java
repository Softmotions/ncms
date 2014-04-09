package com.softmotions.ncms;

import ninja.utils.NinjaProperties;
import com.softmotions.commons.weboot.WBConfiguration;

/**
 * Ncms configuration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsConfiguration extends WBConfiguration {

    public static final String DEFAULT_CFG_RESOURCE = "com/softmotions/ncms/ncms-configuration.xml";

    public NcmsConfiguration(NinjaProperties ninjaProperties) {
        super(ninjaProperties);
    }

    public NcmsConfiguration(NinjaProperties ninjaProperties, String cfgResource, boolean resource) {
        super(ninjaProperties, cfgResource, resource);
    }

    public String getNcmsPrefix() {
        String p = impl().getString("ncms-prefix", "/ncms");
        return p.charAt(0) != '/' ? '/' + p : p;
    }

    public String getEnvironmentType() {
        String etype = ninjaProperties.get("ncms.environment");
        if (etype == null) {
            throw new RuntimeException("Missing required 'ncms.environment' " +
                                       "property in 'application.conf'");
        }
        return etype;
    }

    public String getDBEnvironmentType() {
        String etype = ninjaProperties.get("ncms.db.environment");
        if (etype == null) {
            throw new RuntimeException("Missing required 'ncms.db.environment' " +
                                       "property in 'application.conf'");
        }
        return etype;
    }
}
