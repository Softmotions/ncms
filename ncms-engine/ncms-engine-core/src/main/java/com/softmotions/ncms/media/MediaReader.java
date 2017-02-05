package com.softmotions.ncms.media;

import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * Read-only access interface for media resources.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@ThreadSafe
public interface MediaReader {

    @Nonnull
    String resolveFileLink(Long id);

    @Nonnull
    String resolveFileLink(Long id, boolean inline);

    @Nullable
    Long getFileIdByResourceSpec(String wikiResource);

    /**
     * Find media resource. Returns {@code null} if no resources found.
     * Path can be in the following forms:
     * 1. Full path: /foo/bar
     * 2. URI form: entity:{id} eg: entity:123
     *
     * @param path   Media resource specification.
     * @param locale Desired locale, can be null.
     * @return Media resource description of {@code null} if resource is not found
     */
    @Nullable
    MediaResource findMediaResource(String path, @Nullable Locale locale);

    @Nullable
    MediaResource findMediaResource(Long id, @Nullable Locale locale);

    /**
     * Retrieve the specified media resource.
     *
     * @param id     Resource ID
     * @param req    Servlet request
     * @param width  Desired image width. Can be {@code null}
     * @param height Desired image height. Can be {@code null}
     * @param inline If true requested resource will be rendered for inline viewing.
     * @return
     * @throws Exception
     */
    @Nonnull
    Response get(Long id,
                 HttpServletRequest req,
                 @Nullable Integer width,
                 @Nullable Integer height,
                 boolean inline) throws Exception;

    /**
     * Returns {@code true} if all security checks passed
     * and media resource allowed to be served as response.
     *
     * @param resource Media resource object
     * @param req      Client request
     */
    boolean isAllowedToResponse(MediaResource resource,
                                HttpServletRequest req);

    /**
     * Base directory for media content
     */
    @Nonnull
    File getBaseDir();
}
