package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmResource {

    /**
     * Get the template name.
     *
     * @return name
     */
    String getName();

    /**
     * Get the the template encoding.
     *
     * @return encoding
     */
    String getEncoding();

    /**
     * Get the the template locale.
     *
     * @return locale
     */
    Locale getLocale();

    /**
     * Get the the template last modified time.
     *
     * @return last modified time
     */
    long getLastModified();

    /**
     * Get the the template length.
     *
     * @return source length
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
     * Get the template source reader.
     * <p>
     * NOTE: Don't forget close the reader.
     * <p>
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
     * @throws IOException - If an I/O error occurs
     */
    Reader openReader() throws IOException;

    /**
     * Get the template source input stream.
     * <p>
     * NOTE: Don't forget close the input stream.
     * <p>
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
     * @throws IOException - If an I/O error occurs
     */
    InputStream openStream() throws IOException;

}
