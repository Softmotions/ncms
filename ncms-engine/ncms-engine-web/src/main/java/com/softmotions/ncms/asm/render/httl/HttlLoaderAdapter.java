package com.softmotions.ncms.asm.render.httl;

import httl.Engine;
import httl.Resource;
import httl.spi.loaders.AbstractLoader;
import httl.spi.loaders.resources.AbstractResource;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlLoaderAdapter extends AbstractLoader {

    protected List<String> doList(String directory, String suffix) throws IOException {
        AsmRendererContext ctx = AsmRendererContext.get();
        if (ctx == null) return Collections.EMPTY_LIST;
        return ctx.listResources(directory, suffix);
    }

    protected boolean doExists(String name, Locale locale, String path) throws IOException {
        AsmRendererContext ctx = AsmRendererContext.get();
        return (ctx != null && ctx.isResourceExists(path));
    }

    protected Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException {
        return new HttlLoaderResource(getEngine(), name, locale, encoding, path);
    }

    private static final class HttlLoaderResource extends AbstractResource {

        final String path;

        private HttlLoaderResource(Engine engine, String name, Locale locale, String encoding, String path) {
            super(engine, name, locale, encoding);
            this.path = path;
        }

        public Reader openReader() throws IOException {
            AsmRendererContext ctx = AsmRendererContext.getSafe();
            Reader reader = ctx.openResourceReader(path);
            if (reader == null) {
                throw new IOException("Resource: " + path + " not found");
            }
            return reader;
        }

        public InputStream openStream() throws IOException {
            AsmRendererContext ctx = AsmRendererContext.getSafe();
            InputStream is = ctx.openResourceInputStream(path);
            if (is == null) {
                throw new IOException("Resource: " + path + " not found");
            }
            return is;
        }
    }
}
