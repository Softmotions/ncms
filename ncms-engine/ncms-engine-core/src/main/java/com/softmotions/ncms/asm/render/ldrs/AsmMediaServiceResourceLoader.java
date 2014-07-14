package com.softmotions.ncms.asm.render.ldrs;

import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.media.MediaService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.Locale;

/**
 * Template loader based on {@link com.softmotions.ncms.media.MediaService}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmMediaServiceResourceLoader implements AsmResourceLoader {

    final MediaService mediaService;

    @Inject
    public AsmMediaServiceResourceLoader(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public boolean exists(String name, Locale locale) {
        MediaResource res = mediaService.findMediaResource(name, locale);
        return (res != null);
    }

    public MediaResource load(String name, Locale locale) throws IOException {
        return mediaService.findMediaResource(name, locale);
    }
}
