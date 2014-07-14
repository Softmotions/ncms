package com.softmotions.ncms.media;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Generic media service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface MediaService {

    /**
     * Import given directory
     * into this media regtistry.
     *
     * @param dir Directory to import.
     * @throws java.io.IOException
     */
    void importDirectory(File dir) throws IOException;

    /**
     * Find media resource. Returns null if no resources found.
     * Path can be in the following forms:
     * 1. Full path: /foo/bar
     * 2. URI form: entity:{id} eg: entity:123
     *
     * @param path   Media resource specification.
     * @param locale Desired locale, can be null.
     * @return
     */
    MediaResource findMediaResource(String path, Locale locale);

    /**
     * Ensure existensce of resized image file
     * for specified image source file identified by path.
     *
     * @param path      The original file path
     * @param width     Desired file width
     * @param height    Desired file height
     * @param skipSmall Skip resizing/checking image with dimensions
     *                  smaller or equal to given height and width values
     */
    void ensureResizedImage(String path, Integer width, Integer height,
                            boolean skipSmall) throws IOException;

    void ensureResizedImage(long id, Integer width, Integer height,
                            boolean skipSmall) throws IOException;

    /**
     * Update all reasized image files
     * for specified image source file identified by path.
     *
     * @param path
     */
    void updateResizedImages(String path) throws IOException;

    void updateResizedImages(long id) throws IOException;
}
