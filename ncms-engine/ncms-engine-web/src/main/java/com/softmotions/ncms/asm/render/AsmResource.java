package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.media.MediaResource;

import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmResource extends MediaResource {

    /**
     * Get the the template locale.
     *
     * @return locale
     */
    Locale getLocale();

}
