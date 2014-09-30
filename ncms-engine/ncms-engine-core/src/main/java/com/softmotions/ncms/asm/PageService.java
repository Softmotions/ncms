package com.softmotions.ncms.asm;

import javax.servlet.http.HttpServletRequest;

/**
 * Page info service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface PageService {

    CachedPage getIndexPage(HttpServletRequest req);

    CachedPage getCachedPage(Long id, boolean create);

    CachedPage getCachedPage(String guidOrAlias, boolean create);

    String resolvePageAlias(String guid);

    String resolvePageLink(Long id);

    String resolvePageLink(String guidOrAlias);

    String resolveResourceLink(String wikiResource);

    String resolvePageGuid(String wikiResource);
}
