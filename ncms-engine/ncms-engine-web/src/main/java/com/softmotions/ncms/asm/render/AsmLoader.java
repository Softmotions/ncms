package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmLoader {

    /**
     * list resource names.
     *
     * @param suffix resource suffix
     * @return resource names.
     */
    List<String> list(String suffix) throws IOException;

    /**
     * is exists resource.
     *
     * @param name   - resource name
     * @param locale - resource locale
     * @return exists
     */
    boolean exists(String name, Locale locale);

    /**
     * load resource.
     *
     * @param name     - resource name
     * @param locale   - resource locale
     * @param encoding - resource encoding
     * @return resource
     */
    AsmResource load(String name, Locale locale, String encoding) throws IOException;
}
