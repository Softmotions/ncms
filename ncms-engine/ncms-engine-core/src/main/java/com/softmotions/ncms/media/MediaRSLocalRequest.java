package com.softmotions.ncms.media;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.web.HttpServletRequestAdapter;

import javax.servlet.ServletContext;
import java.io.File;
import java.security.Principal;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public final class MediaRSLocalRequest extends HttpServletRequestAdapter {

    private final File file;

    private final NcmsConfiguration cfg;

    MediaRSLocalRequest(NcmsConfiguration cfg, File file) {
        this.file = file;
        this.cfg = cfg;
    }

    public File getFile() {
        return file;
    }

    public String getMethod() {
        return "PUT";
    }

    public String getRemoteUser() {
        return "system";
    }

    public boolean isUserInRole(String role) {
        return true;
    }

    public Principal getUserPrincipal() {
        return new Principal() {
            public String getName() {
                return getRemoteUser();
            }
        };
    }

    public ServletContext getServletContext() {
        return cfg.getServletContext();
    }

    public String getCharacterEncoding() {
        return "UTF-8";
    }
}
