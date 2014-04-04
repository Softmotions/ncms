package com.softmotions.ncms.asm.render;

import com.google.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class ClasspathAsmResourceResolver implements AsmResourceResolver {

    public Reader resolveResource(AsmRendererContext ctx, String location) throws IOException {
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
}
