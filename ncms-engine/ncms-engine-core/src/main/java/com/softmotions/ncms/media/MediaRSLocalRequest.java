package com.softmotions.ncms.media;

import java.io.File;
import java.security.Principal;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.HttpServletRequestAdapter;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public final class MediaRSLocalRequest extends HttpServletRequestAdapter {

    private final File file;

    private final NcmsEnvironment env;

    private String user;

    MediaRSLocalRequest(NcmsEnvironment env) {
        this(env, null);
    }

    MediaRSLocalRequest(NcmsEnvironment env,
                        @Nullable File file) {
        this.file = file;
        this.env = env;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getMethod() {
        return "PUT";
    }

    @Override
    public String getRemoteUser() {
        return user != null ? user : "system";
    }

    public void setRemoteUser(String user) {
        this.user = user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public Principal getUserPrincipal() {
        return this::getRemoteUser;
    }

    @Override
    public ServletContext getServletContext() {
        return env.getServletContext();
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }
}
