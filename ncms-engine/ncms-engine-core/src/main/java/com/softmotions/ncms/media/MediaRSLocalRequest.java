package com.softmotions.ncms.media;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.HttpServletRequestAdapter;

import javax.servlet.ServletContext;
import java.io.File;
import java.security.Principal;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public final class MediaRSLocalRequest extends HttpServletRequestAdapter {

    private final File file;

    private final NcmsEnvironment env;

    MediaRSLocalRequest(NcmsEnvironment env, File file) {
        this.file = file;
        this.env = env;
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
        return this::getRemoteUser;
    }

    public ServletContext getServletContext() {
        return env.getServletContext();
    }

    public String getCharacterEncoding() {
        return "UTF-8";
    }
}
