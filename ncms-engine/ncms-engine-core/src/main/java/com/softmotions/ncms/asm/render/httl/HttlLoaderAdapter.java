package com.softmotions.ncms.asm.render.httl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import httl.Engine;
import httl.Resource;
import httl.spi.Loader;

import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.media.MediaResource;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlLoaderAdapter implements Loader {

    private Engine engine;

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> list(String suffix) throws IOException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean exists(String name, Locale locale) {
        AsmRendererContext ctx = AsmRendererContext.get();
        return ctx != null && ctx.getLoader().exists(name, locale);
    }

    @Override
    public Resource load(String name, Locale locale, String encoding) throws IOException {
        AsmRendererContext ctx = AsmRendererContext.get();
        if (ctx == null) {
            return null;
        }
        MediaResource asmres = ctx.getLoader().load(name, locale);
        return (asmres != null ? new HttlResourceAsmAdapter(asmres, engine) : null);
    }

    private static final class HttlResourceAsmAdapter implements Resource {

        private final MediaResource res;

        private final Engine engine;

        private HttlResourceAsmAdapter(MediaResource res, Engine engine) {
            this.res = res;
            this.engine = engine;
        }

        @Override
        public String getName() {
            return res.getName();
        }

        @Override
        public String getEncoding() {
            return res.getEncoding();
        }

        @Override
        public Locale getLocale() {
            return res.getLocale();
        }

        @Override
        public long getLastModified() {
            return res.getLastModified();
        }

        @Override
        public long getLength() {
            return res.getLength();
        }

        @Override
        public String getSource() throws IOException {
            return res.getSource();
        }

        @Override
        public Reader openReader() throws IOException {
            return res.openReader();
        }

        @Override
        public InputStream openStream() throws IOException {
            return res.openStream();
        }

        @Override
        public Engine getEngine() {
            return engine;
        }
    }
}
