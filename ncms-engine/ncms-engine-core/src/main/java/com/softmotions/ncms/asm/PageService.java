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

    CachedPage getCachedPage(String guid, boolean create);
}
