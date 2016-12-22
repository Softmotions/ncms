package com.softmotions.ncms.asm;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Basic cached page information.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface CachedPage {

    enum PATH_TYPE {
        GUID,
        LABEL,
        ID
    }

    @Nonnull
    Asm getAsm();

    @Nonnull
    Long getId();

    @Nullable
    String getAlias();

    @Nonnull
    String getName();

    @Nullable
    String getHname();

    boolean isPublished();

    @Nullable
    Long getNavParentId();

    @Nonnull
    <T> Map<PATH_TYPE, T> fetchNavPaths();
}
