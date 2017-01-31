package com.softmotions.ncms.asm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;

import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;

/**
 * Page info service.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@ThreadSafe
public interface PageService {

    @Nullable
    IndexPage getIndexPage(HttpServletRequest req, boolean requirePublished);

    @Nullable
    String getIndexPageLanguage(HttpServletRequest req);

    @Nullable
    CachedPage getCachedPage(Long id, boolean create);

    @Nullable
    CachedPage getCachedPage(String guidOrAlias, boolean create);

    @Nullable
    String resolvePageAlias(String guid);

    @Nullable
    String resolvePageLink(@Nullable Long id);

    @Nullable
    String resolvePageLink(@Nullable String guidOrAlias);

    @Nullable
    String resolveResourceLink(@Nullable String wikiResource);

    @Nullable
    String resolvePageGuid(@Nullable String wikiResource);

    @Nonnull
    PageSecurityService getPageSecurityService();

    @Nonnull
    AsmAttributeManagersRegistry getAsmAttributeManagersRegistry();
}
