package com.softmotions.ncms.asm;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;

/**
 * Page info service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@ThreadSafe
public interface PageService {

    @Nullable
    CachedPage getIndexPage(HttpServletRequest req);

    @Nullable
    CachedPage getCachedPage(Long id, boolean create);

    @Nullable
    CachedPage getCachedPage(String guidOrAlias, boolean create);

    @Nullable
    String resolvePageAlias(String guid);

    @Nullable
    String resolvePageLink(Long id);

    @Nullable
    String resolvePageLink(String guidOrAlias);

    @Nullable
    String resolveResourceLink(String wikiResource);

    @Nullable
    String resolvePageGuid(String wikiResource);
}
