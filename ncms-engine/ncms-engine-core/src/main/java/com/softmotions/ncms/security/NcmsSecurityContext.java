package com.softmotions.ncms.security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.softmotions.web.security.WSUser;

/**
 * Generic interface for authorisation purposes
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */

public interface NcmsSecurityContext {

    @NotNull
    WSUser getWSUser(Principal p);

    @NotNull
    WSUser getWSUser(HttpServletRequest req);
}
