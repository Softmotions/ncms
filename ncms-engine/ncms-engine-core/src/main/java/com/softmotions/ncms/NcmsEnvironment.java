package com.softmotions.ncms;

import com.softmotions.weboot.WBConfiguration;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Ncms configuration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@ThreadSafe
public class NcmsEnvironment extends WBConfiguration {

    private static final String CORE_PROPS_LOCATION = "/com/softmotions/ncms/core/Core.properties";

    private final Properties coreProps;

    private String ncmsPrefix;

    private String ncmsRoot;

    private String adminRoot;

    private String environmentType;

    private String dbEnvironment;


    public String getNcmsVersion() {
        return coreProps.getProperty("project.version");
    }

    public NcmsEnvironment() {
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
        this.ncmsPrefix = xcfg.getString("ncms-prefix", "/ncms");
        this.ncmsRoot = sctx.getContextPath() + this.ncmsPrefix;
        this.adminRoot = this.ncmsRoot + "/adm";
        this.environmentType = xcfg.getString("environment", "production");
        this.dbEnvironment = xcfg.getString("db-environment", "production");
    }

    private void normalizePrefix(String property) {
        String val = xcfg().getString(property);
        if (StringUtils.isBlank(val) || "/".equals(val)) {
            val = "";
        } else {
            val = val.trim();
            if (!val.startsWith("/")) {
                val = '/' + val;
            }
            if (val.endsWith("/")) {
                val = val.substring(0, val.length() - 1);
            }
        }
        xcfg().setProperty(property, val);
    }

    @Nonnull
    public String getApplicationName() {
        return xcfg().getString("app-name", "Ncms");
    }

    @Nullable
    public String getLogoutRedirect() {
        String ret = xcfg().getString("logout-redirect");
        if (StringUtils.isBlank(ret)) {
            return xcfg.getString("site-root");
        }
        return null;
    }

    @Nonnull
    public String getNcmsPrefix() {
        return ncmsPrefix;
    }

    @Nonnull
    public String getEnvironmentType() {
        return environmentType;
    }

    @Nonnull
    public String getDBEnvironmentType() {
        return dbEnvironment;
    }

    @Nonnull
    public String getNcmsRoot() {
        return ncmsRoot;
    }

    @Nonnull
    public String getNcmsAdminRoot() {
        return adminRoot;
    }

    @Nonnull
    public String getAbsoluteLink(HttpServletRequest req, String link) {
        XMLConfiguration x = xcfg();
        boolean preferRequestUrl = x.getBoolean("site-root[@preferRequestUrl]", false);
        if (preferRequestUrl) {
            link = req.getScheme() + "://" +
                   req.getServerName() +
                   ":" + req.getServerPort() +
                   link;
        } else {
            link = x.getString("site-root") + link;
        }
        return link;
    }
}
