package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.media.MediaResource;

import java.io.IOException;
import java.util.Locale;

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
