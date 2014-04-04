package com.softmotions.ncms.asm.render;

/**
 * Assembly handler.
 * <p>
 * Handler may contain custom presentation logic.
 * Handler instance called by {@link com.softmotions.ncms.asm.render.AsmRenderer}
 * just before rendering the content of assembly.
 * <p>
 * Handler's class can contain Guice IoC binding annotations.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmHandler {

    /**
     * Execute assembly handler.
     * Handler can take full control on response,
     * in this case it should return <code>true<code> or
     * commit underling servlet response.
     *
     * @param ctx
     * @return True if no farther assembly rendering is required.
     * @throws Exception
     */
    boolean execute(AsmRendererContext ctx) throws Exception;
}
