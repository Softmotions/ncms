package com.softmotions.ncms.users;

import java.io.Serializable;
import java.security.Principal;

/**
 * Ncms user principal.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface NcmsUserPrincipal extends Principal, Serializable {

    String getEmail();

    String getFullName();

}
