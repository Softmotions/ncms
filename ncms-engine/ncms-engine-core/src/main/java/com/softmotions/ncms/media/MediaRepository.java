package com.softmotions.ncms.media;

import java.io.Closeable;
import java.io.IOException;

/**
 * Generic media service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface MediaRepository extends MediaReader, Closeable {

    /**
     * Import given directory
     * into this media repository.
     */
    void importDirectory(String source,
                         String target,
                         String[] includes,
                         String[] excludes,
                         boolean overwrite,
                         boolean watch,
                         boolean system) throws IOException;

    /**
     * Import given file
     * into this media repository.
     *
     * @param source    File path to import. It must be file.
     * @param target    Path to to the target file in the repository.
     * @param overwrite If {@code false} pre-existed file will not be overriten it
     */
    void importFile(String source,
                    String target,
                    boolean overwrite,
                    boolean system) throws IOException;


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
