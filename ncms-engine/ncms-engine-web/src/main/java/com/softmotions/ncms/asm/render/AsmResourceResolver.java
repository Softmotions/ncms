package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmResourceResolver {

    /**
     * Resolve resource location, and return template data reader.
     *
     * @param location Location of template file
     */
    Reader resolveResource(AsmRendererContext ctx, String location) throws IOException;
}
