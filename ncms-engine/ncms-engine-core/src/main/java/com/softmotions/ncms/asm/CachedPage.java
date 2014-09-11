package com.softmotions.ncms.asm;

import java.util.Map;

/**
 * Basic cached page information.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface CachedPage {

    enum PATH_TYPE {
        GUID,
        LABEL,
        ID
    }

    Asm getAsm();

    Long getId();

    String getName();

    String getHname();

    boolean isPublished();

    Long getNavParentId();

    <T> Map<PATH_TYPE, T> fetchNavPaths();
}
