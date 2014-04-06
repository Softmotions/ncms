package com.softmotions.ncms.asm.render;

import httl.util.UrlUtils;

import com.google.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class ClasspathAsmResourceResolver implements AsmResourceResolver {

    public Reader openResourceReader(AsmRendererContext ctx, String location) throws IOException {
        location = location.charAt(0) == '/' ? location.substring(1) : location;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        InputStream res = cl.getResourceAsStream(location);
        if (res == null) {
            return null;
        }
        return new InputStreamReader(res, "UTF-8");
    }

    public InputStream openResourceInputStream(AsmRendererContext ctx, String location) throws IOException {
        location = location.charAt(0) == '/' ? location.substring(1) : location;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        return cl.getResourceAsStream(location);
    }

    public boolean isResourceExists(AsmRendererContext ctx, String location) {
        location = location.charAt(0) == '/' ? location.substring(1) : location;
        return (ctx.getClassLoader().getResource(location) != null);
    }

    public List<String> listResources(AsmRendererContext ctx, String directory, String suffix) throws IOException {
        directory = directory.charAt(0) == '/' ? directory.substring(1) : directory;
        return UrlUtils.listUrl(Thread.currentThread().getContextClassLoader().getResource(directory), suffix);
    }
}
