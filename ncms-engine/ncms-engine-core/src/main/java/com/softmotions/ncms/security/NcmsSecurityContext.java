package com.softmotions.ncms.security;

import java.security.Principal;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.ShiroException;

import com.softmotions.web.security.WSUser;

/**
 * Generic interface for authorisation purposes
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */

public interface NcmsSecurityContext {

    @Nonnull
    WSUser getWSUser(Principal p, Locale locale) throws ShiroException;

    @Nonnull
    WSUser getWSUser(Principal p) throws ShiroException;

    @Nonnull
    WSUser getWSUser(HttpServletRequest req) throws ShiroException;
}
