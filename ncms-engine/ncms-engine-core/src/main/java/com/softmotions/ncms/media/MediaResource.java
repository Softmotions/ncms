package com.softmotions.ncms.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface MediaResource {


    @Nullable
    Long getId();

    /**
     * Get the resource name.
     */
    String getName();

    /**
     * Get the the resource encoding.
     */
    @Nullable
    String getEncoding();

    /**
     * Get resource content type.
     * Return <code>null</code> if content type is not known
     */
    @Nullable
    String getContentType();

    /**
     * Media file owner
     */
    @Nonnull
    String getOwner();

    /**
     * Get the the template last modified time.
     * Return <code>-1L</code> if last modified time is not known.
     */
    long getLastModified();

    /**
     * Get resource length.
     * Returns <code>-1L</code> if length is not known.
     */
    long getLength();

    /**
     * Get the template source.
     *
     * @return source
     * @throws java.io.IOException - If an I/O error occurs
     */
    String getSource() throws IOException;

    /**
     * Get real file system path of the media item.
     */
    Path getFileSystemPath();

    /**
     * Get the resource source reader.
     * <p/>
     * NOTE: Don't forget close the reader.
     * <p/>
     * <pre>
     * Reader reader = resource.openReader();
     * try {
     * 	 // do something ...
     * } finally {
     * 	 reader.close();
     * }
     * </pre>
     *
     * @return source reader
     * @throws java.io.IOException - If an I/O error occurs
     */
    Reader openReader() throws IOException;

    /**
     * Get the resource source input stream.
     * <p/>
     * NOTE: Don't forget close the input stream.
     * <p/>
     * <pre>
     * InputStream stream = resource.openStream();
     * try {
     * 	 // do something ...
     * } finally {
     * 	 stream.close();
     * }
     * </pre>
     *
     * @return source input stream
     * @throws java.io.IOException - If an I/O error occurs
     */
    InputStream openStream() throws IOException;


    long writeTo(Writer out) throws IOException;

    long writeTo(OutputStream out) throws IOException;

    /**
     * Get the resource locale.
     *
     * @return locale
     */
    @Nullable
    Locale getLocale();

    /**
     * Width of image.
     *
     * @return -1 if width is not known
     */
    int getImageWidth();

    /**
     * Height of image.
     *
     * @return -1 if width is not known
     */
    int getImageHeight();

    @Nullable
    String getDescription();

}
