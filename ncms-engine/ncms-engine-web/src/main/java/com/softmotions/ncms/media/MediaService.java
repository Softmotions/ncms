package com.softmotions.ncms.media;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Generic media service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface MediaService {

    File getBasedir();

    Closeable acquireReadResourceLock(String path);

    Closeable acquireWriteResourceLock(String path);

    /**
     * Import given directory
     * into this media regtistry.
     *
     * @param dir Directory to import.
     * @throws IOException
     */
    void importDirectory(File dir) throws IOException;

    /**
     * Find media resource. Returns null if no resources found.
     * Path can be in the following forms:
     * 1. Full path: /foo/bar
     * 2. URI form: entity:{id} eg: entity:123
     *
     * @param path Media resource specification.
     * @return
     */
    MediaResource findMediaResource(String path);

}
