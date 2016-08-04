package com.softmotions.ncms.security;

import java.security.Principal;
import javax.validation.constraints.NotNull;

import com.softmotions.web.security.WSUser;

/**
 * FIXME Описание
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */

public interface NcmsSecurityContext {

    @NotNull
    WSUser getWSUser(Principal p);
}
