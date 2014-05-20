package com.softmotions.ncms.media;

import java.io.File;
import java.io.IOException;

/**
 * Generic media service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface MediaService {

    /**
     * Import given directory
     * into Ncms media regtistry.
     *
     * @param dir Directory to import.
     * @throws IOException
     */
    void importDirectory(File dir) throws IOException;
}
