package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.util.Locale;

import com.softmotions.ncms.media.MediaResource;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmResourceLoader {

    /**
     * Return true if given resource exists.
     *
     * @param name   - resource name
     * @param locale - resource locale
     * @return exists
     */
    boolean exists(String name, Locale locale);

    /**
     * Load the specified resource.
     *
     * @param name   - resource name
     * @param locale - resource locale
     * @return resource
     */
    MediaResource load(String name, Locale locale) throws IOException;
}
