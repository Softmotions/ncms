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
        normalizePrefix("site-files-root");
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
        return getAsmRoot() + guid;
    }

    public String getAsmLink(Long id) {
        return getAsmRoot() + id;
    }

    public String getAsmRoot() {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/asm/";
    }

    public String getFileLink(Long id) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/rs/media/fileid/" + id;
    }

    public String getFileLink(Long id, boolean inline) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/rs/media/fileid/" + id + "?inline=true";
    }

    public String getResourceLink(String spec) {
        if (spec.contains("://")) {
            return spec;
        }
        Long fid = getFileIdByResourceSpec(spec);
        if (fid != null) {
            return getFileLink(fid, true);
        }
        if (spec.toLowerCase().startsWith("page:")) { //Page reference
            spec = spec.substring("page:".length());
            int ind = spec.indexOf('|');
            if (ind != -1) {
                spec = spec.substring(0, ind).trim();
            }
        }
        return getAsmLink(spec);
    }

    public Long getFileIdByResourceSpec(String spec) {
        spec = spec.toLowerCase();
        if (!spec.startsWith("media:") && !spec.startsWith("image:")) {
            return null;
        }
        spec = spec.substring("media:".length()); /*'image:' string has the same length*/
        if (spec.charAt(0) == '/') {
            spec = spec.substring(1);
        }
        int ind;
        int ind1 = spec.indexOf('/');
        int ind2 = spec.indexOf('|');
        if (ind1 == -1 || ind2 == -1) {
            ind = Math.max(ind1, ind2);
        } else {
            ind = Math.min(ind1, ind2);
        }
        if (ind != -1) {
            spec = spec.substring(0, ind);
        }
        try {
            return Long.valueOf(spec);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
