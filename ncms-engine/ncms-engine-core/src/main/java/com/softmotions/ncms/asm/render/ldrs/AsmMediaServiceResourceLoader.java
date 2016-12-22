package com.softmotions.ncms.asm.render.ldrs;

import java.io.IOException;
import java.util.Locale;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

/**
 * Template loader based on {@link com.softmotions.ncms.media.MediaRepository}
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class AsmMediaServiceResourceLoader implements AsmResourceLoader {

    private final MediaReader reader;

    @Inject
    public AsmMediaServiceResourceLoader(MediaReader reader) {
        this.reader = reader;
    }

    @Override
    public boolean exists(String name, Locale locale) {
        MediaResource res = reader.findMediaResource(name, locale);
        return (res != null);
    }

    @Override
    public MediaResource load(String name, Locale locale) throws IOException {
        return reader.findMediaResource(name, locale);
    }
}
