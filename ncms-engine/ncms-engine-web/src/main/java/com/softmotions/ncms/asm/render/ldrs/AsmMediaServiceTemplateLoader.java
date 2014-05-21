package com.softmotions.ncms.asm.render.ldrs;

import com.softmotions.ncms.asm.render.AsmTemplateLoader;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.media.MediaService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Template loader based on {@link com.softmotions.ncms.media.MediaService}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmMediaServiceTemplateLoader implements AsmTemplateLoader {

    final MediaService mediaService;

    @Inject
    public AsmMediaServiceTemplateLoader(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public List<String> list(String suffix) throws IOException {
        return Collections.EMPTY_LIST;
    }

    public boolean exists(String name, Locale locale) {
        MediaResource res = mediaService.findMediaResource(name, locale);
        return (res != null);
    }

    public MediaResource load(String name, Locale locale, String encoding) throws IOException {
        return mediaService.findMediaResource(name, locale);
    }
}
