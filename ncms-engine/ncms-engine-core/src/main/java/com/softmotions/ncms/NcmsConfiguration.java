package com.softmotions.ncms;

import ninja.lifecycle.Dispose;
import ninja.utils.NinjaProperties;
import com.softmotions.commons.io.DirUtils;
import com.softmotions.weboot.WBConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Ncms configuration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsConfiguration extends WBConfiguration {

    public static final String DEFAULT_CFG_RESOURCE = "com/softmotions/ncms/ncms-configuration.xml";

    private final File tmpdir;

    private final ServletContext servletContext;

    public NcmsConfiguration(ServletContext servletContext, NinjaProperties ninjaProperties) {
        this(servletContext, ninjaProperties, null, true);
    }

    public NcmsConfiguration(ServletContext servletContext,
                             NinjaProperties ninjaProperties,
                             String cfgResource, boolean resource) {
        super(ninjaProperties, cfgResource, resource);
        this.servletContext = servletContext;
        String dir = impl().getString("tmpdir");
        if (StringUtils.isBlank(dir)) {
            dir = System.getProperty("java.io.tmpdir");
        }
        tmpdir = new File(dir);
        log.info("Using dir: " + tmpdir.getAbsolutePath());
        try {
            DirUtils.ensureDir(tmpdir, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    /**
     * System-wide tmp dir.
     */
    public File getTmpdir() {
        return tmpdir;
    }

    /**
     * Tmp dir cleared on application shutdown.
     */
    public File getSessionTmpdir() {
        synchronized (this) {
            if (sessionTmpDir == null) {
                try {
                    sessionTmpDir = Files.createTempDirectory("ncms-").toFile();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
        return sessionTmpDir;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    private File sessionTmpDir;

    public String substitutePath(String path) {
        String webappPath = getServletContext().getRealPath("/");
        if (webappPath != null) {
            if (webappPath.endsWith("/")) {
                webappPath = webappPath.substring(0, webappPath.length() - 1);
            }
            path = path.replace("{webapp}", webappPath);
        }
        path = path.replace("{cwd}", System.getProperty("user.dir"))
                .replace("{home}", System.getProperty("user.home"))
                .replace("{tmp}", getTmpdir().getAbsolutePath());

        if (path.contains("{newtmp}")) {
            path = path.replace("{newtmp}", getSessionTmpdir().getAbsolutePath());
        }
        return path;
    }

    public String getAsmLink(String guid) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/asm/" + guid;
    }

    public String getAsmLink(Long id) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/asm/" + id;
    }

    public String getFileLink(Long id) {
        return getServletContext().getContextPath() + getNcmsPrefix() + "/rc/media/fileid/" + id;
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

    @Dispose(order = 1)
    public void dispose() {
        synchronized (this) {
            if (sessionTmpDir != null) {
                try {
                    FileUtils.deleteDirectory(sessionTmpDir);
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }
}
