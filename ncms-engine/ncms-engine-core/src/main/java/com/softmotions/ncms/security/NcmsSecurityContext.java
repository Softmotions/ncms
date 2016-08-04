package com.softmotions.ncms.security;

import java.security.Principal;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.ShiroException;

import com.softmotions.web.security.WSUser;

/**
 * Generic interface for authorisation purposes
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */

public interface NcmsSecurityContext {

    @NotNull
    WSUser getWSUser(Principal p, Locale locale) throws ShiroException;

    @NotNull
    WSUser getWSUser(Principal p) throws ShiroException;

    @NotNull
    WSUser getWSUser(HttpServletRequest req) throws ShiroException;
}
