package com.softmotions.ncms.asm.render.ldrs;

import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.Locale;

/**
 * Template loader based on {@link com.softmotions.ncms.media.MediaRepository}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmMediaServiceResourceLoader implements AsmResourceLoader {

    final MediaReader reader;

    @Inject
    public AsmMediaServiceResourceLoader(MediaReader reader) {
        this.reader = reader;
    }

    public boolean exists(String name, Locale locale) {
        MediaResource res = reader.findMediaResource(name, locale);
        return (res != null);
    }

    public MediaResource load(String name, Locale locale) throws IOException {
        return reader.findMediaResource(name, locale);
    }
}
