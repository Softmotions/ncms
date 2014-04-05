package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmResourceResolver {

    /**
     * List resource names.
     */
    List<String> listResources(AsmRendererContext ctx, String directory, String suffix) throws IOException;

    /**
     * Returns true if resource specified by
     * location exists and can be retrieved by
     * {@link #openResourceReader(AsmRendererContext, String)}
     */
    boolean isResourceExists(AsmRendererContext ctx, String location);

    /**
     * Resolve resource location, and return resource content reader.
     * If resource is not exists it returns <code>null</code>
     *
     * @param location Resource file reader or <code>null</code> if resource is not exists
     */
    Reader openResourceReader(AsmRendererContext ctx, String location) throws IOException;

    /**
     * Resolve resource location, and return resource content input stream.
     * If resource is not exists it returns <code>null</code>
     *
     * @param location Resource file input stream or <code>null</code> if resource is not exists
     */
    InputStream openResourceInputStream(AsmRendererContext ctx, String location) throws IOException;
}
