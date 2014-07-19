package com.softmotions.ncms.media;

import java.util.Locale;

/**
 * Read-only access interface for media resources.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface MediaReader {

    /**
     * Find media resource. Returns {@code null} if no resources found.
     * Path can be in the following forms:
     * 1. Full path: /foo/bar
     * 2. URI form: entity:{id} eg: entity:123
     *
     * @param path   Media resource specification.
     * @param locale Desired locale, can be null.
     * @return
     */
    MediaResource findMediaResource(String path, Locale locale);
}
