package com.softmotions.ncms.media;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.softmotions.commons.cont.Pair;

/**
 * Generic media service.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@ThreadSafe
public interface MediaRepository extends MediaReader, Closeable {

    int RESIZE_SKIP_SMALL = 1;

    int RESIZE_COVER_AREA = 1 << 1;

    int IMPORT_OVERWRITE = 1;

    int IMPORT_WATCH = 1 << 1;

    int IMPORT_CLEANUP_MISSING = 1 << 2;

    int IMPORT_SYSTEM = 1 << 3;

    /**
     * Import given directory
     * into this media repository.
     */
    void importDirectory(String source,
                         String target,
                         String[] includes,
                         String[] excludes,
                         int flags) throws IOException;

    /**
     * Import given file
     * into this media repository.
     *
     * @param source    File path to import. It must be file.
     * @param target    Path to to the target file in the repository.
     * @param overwrite If {@code false} pre-existed file will not be overridden
     * @param system    If {@code true} file will be marked as system
     * @param user      Optional file owner
     */
    Long importFile(String source,
                    String target,
                    boolean overwrite,
                    boolean system,
                    @Nullable String user) throws IOException;


    Long importFile(InputStream source,
                    String target,
                    boolean system,
                    @Nullable String user) throws IOException;

    /**
     * Ensure existence of resized image file
     * for specified image source file identified by path.
     *
     * @param path   The original file path
     * @param width  Desired file width
     * @param height Desired file height
     * @param flags  {@link #RESIZE_SKIP_SMALL}, {@link #RESIZE_COVER_AREA}
     */
    @Nullable
    Pair<Integer, Integer> ensureResizedImage(String path,
                                              @Nullable Integer width,
                                              @Nullable Integer height,
                                              int flags) throws IOException;

    @Nullable
    Pair<Integer, Integer> ensureResizedImage(long id,
                                              @Nullable Integer width,
                                              @Nullable Integer height,
                                              int flags) throws IOException;

    /**
     * Update all resized image files
     * for specified image source file identified by path.
     */
    void updateResizedImages(String path) throws IOException;

    void updateResizedImages(long id) throws IOException;

    /**
     * Returns dedicated page media folder path
     *
     * @param pageId Page id
     */
    String getPageLocalFolderPath(Long pageId);

    /**
     * Copy media files from source page to the target page
     *
     * @param sourcePageId Source page id
     * @param targetPageId Target page id
     * @param owner        Page files owner. If `null` source
     *                     owner will be preserved
     * @return Copied files mapping: `source file id => target file id`
     */
    Map<Long, Long> copyPageMedia(long sourcePageId,
                                  long targetPageId,
                                  @Nullable String owner) throws IOException;
}

