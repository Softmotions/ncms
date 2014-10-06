package com.softmotions.ncms.media;

import com.softmotions.commons.cont.Pair;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Generic media service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@ThreadSafe
public interface MediaRepository extends MediaReader, Closeable {

    int RESIZE_SKIP_SMALL = 1;

    int RESIZE_COVER_AREA = 1 << 1;


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
    Long importFile(String source,
                    String target,
                    boolean overwrite,
                    boolean system) throws IOException;


    Long importFile(InputStream source,
                    String target,
                    boolean system) throws IOException;

    /**
     * Ensure existensce of resized image file
     * for specified image source file identified by path.
     *
     * @param path   The original file path
     * @param width  Desired file width
     * @param height Desired file height
     * @param flags  {@link #RESIZE_SKIP_SMALL}, {@link #RESIZE_COVER_AREA}
     */
    @Nullable
    Pair<Integer, Integer> ensureResizedImage(String path, Integer width, Integer height, int flags) throws IOException;

    @Nullable
    Pair<Integer, Integer> ensureResizedImage(long id, Integer width, Integer height, int flags) throws IOException;

    /**
     * Update all reasized image files
     * for specified image source file identified by path.
     *
     * @param path
     */
    void updateResizedImages(String path) throws IOException;

    void updateResizedImages(long id) throws IOException;

    String getPageLocalFolderPath(Long pageId);

}

