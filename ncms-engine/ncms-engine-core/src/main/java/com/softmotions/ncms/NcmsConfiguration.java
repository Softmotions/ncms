package com.softmotions.ncms;

import com.softmotions.weboot.WBConfiguration;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Ncms configuration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsConfiguration extends WBConfiguration {

    public static volatile NcmsConfiguration INSTANCE;

    private static final String CORE_PROPS_LOCATION = "/com/softmotions/ncms/core/Core.properties";

    private final Properties coreProps;

    public String getNcmsVersion() {
        return coreProps.getProperty("project.version");
    }

    public NcmsConfiguration() {
        if (INSTANCE == null) {
            synchronized (NcmsConfiguration.class) {
                if (INSTANCE == null) {
                    INSTANCE = this;
                }
            }
        }
        coreProps = new Properties();
        try (InputStream is = getClass().getResourceAsStream(CORE_PROPS_LOCATION)) {
            if (is == null) {
                throw new RuntimeException("Jar resource not found: " + CORE_PROPS_LOCATION);
            }
            coreProps.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(String location, ServletContext sctx) {
        super.load(location, sctx);
        normalizePrefix("site-root");
        normalizePrefix("ncms-prefix");
    }

    private void normalizePrefix(String property) {
        String val = impl().getString(property);
        if (!StringUtils.isBlank(val)) {
            val = val.trim();
            if (!val.startsWith("/")) {
                val = '/' + val;
            }
            if (val.endsWith("/")) {
                val = val.substring(0, val.length() - 1);
            }
            impl().setProperty(property, val);
        }
    }

    public String getApplicationName() {
        return impl().getString("app-name", "Ncms");
    }

    public String getHelpSite() {
        return impl().getString("help-site");
    }

    public String getLogoutRedirect() {
        return impl().getString("logout-redirect");
    }

    public String getNcmsPrefix() {
        String p = impl().getString("ncms-prefix", "/ncms");
        return p.charAt(0) != '/' ? '/' + p : p;
    }

    public String getEnvironmentType() {
        String etype = xcfg.getString("environment");
        if (etype == null) {
            throw new RuntimeException("Missing required '<environment>' " +
                                       "property in application config");
        }
        return etype;
    }

    public String getDBEnvironmentType() {
        String etype = xcfg.getString("db-environment");
        if (etype == null) {
            throw new RuntimeException("Missing required 'ncms.db.environment' " +
                                       "property in 'application.conf'");
        }
        return etype;
    }

    public String getAsmLink(String guid) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/asm/" + guid;
    }

    public String getAsmLink(Long id) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/asm/" + id;
    }

    public String getFileLink(Long id) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/rs/media/fileid/" + id;
    }

    public String getFileLink(Long id, boolean inline) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/rs/media/fileid/" + id + "?inline=true";
    }

    public String getPageLink(String spec) {
        if (spec.contains("://")) {
            return spec;
        }
        if (spec.startsWith("page:")) {
            return getAsmLink(spec.substring("page:".length()));
        } else {
            return getAsmLink(spec);
        }
    }
}
