package com.softmotions.ncms.asm.render.httl;

import httl.Engine;
import httl.Resource;
import httl.spi.Loader;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.media.MediaResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

    @SuppressWarnings("unchecked")
    public List<String> list(String suffix) throws IOException {
        return Collections.EMPTY_LIST;
    }

    public boolean exists(String name, Locale locale) {
        AsmRendererContext ctx = AsmRendererContext.get();
        return ctx != null && ctx.getLoader().exists(name, locale);
    }

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

        public String getName() {
            return res.getName();
        }

        public String getEncoding() {
            return res.getEncoding();
        }

        public Locale getLocale() {
            return res.getLocale();
        }

        public long getLastModified() {
            return res.getLastModified();
        }

        public long getLength() {
            return res.getLength();
        }

        public String getSource() throws IOException {
            return res.getSource();
        }

        public Reader openReader() throws IOException {
            return res.openReader();
        }

        public InputStream openStream() throws IOException {
            return res.openStream();
        }

        public Engine getEngine() {
            return engine;
        }
    }
}
