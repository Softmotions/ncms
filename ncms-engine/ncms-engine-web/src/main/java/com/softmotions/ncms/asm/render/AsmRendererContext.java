package com.softmotions.ncms.asm.render;

import com.softmotions.commons.cont.Stack;
import com.softmotions.ncms.asm.Asm;

import com.google.inject.Injector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Rendering context for assembly rendereres.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class AsmRendererContext extends HashMap<String, Object> {

    public static final ThreadLocal<Stack<AsmRendererContext>> ASM_CTX = new ThreadLocal<>();

    /**
     * Push the current context in the ThreadLocal {@link #ASM_CTX}
     */
    public void push() {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        if (asmRendererContexts == null) {
            asmRendererContexts = new Stack<>();
            ASM_CTX.set(asmRendererContexts);
        }
        asmRendererContexts.push(this);
    }

    /**
     * Pop the current content from the ThreadLocal {@link #ASM_CTX}
     */
    public void pop() {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        asmRendererContexts.pop();
    }

    public static AsmRendererContext getSafe() {
        Stack<AsmRendererContext> sctx = ASM_CTX.get();
        if (sctx == null || sctx.isEmpty()) {
            throw new RuntimeException("Missing AsmRendererContext.ASM_CTX for the current thread: " +
                                       Thread.currentThread());
        }
        return sctx.peek();
    }

    public static AsmRendererContext get() {
        Stack<AsmRendererContext> sctx = ASM_CTX.get();
        if (sctx == null || sctx.isEmpty()) {
            return null;
        }
        return sctx.peek();
    }

    public abstract AsmRenderer getRenderer();

    /**
     * Raw http servlet request.
     */
    public abstract HttpServletRequest getServletRequest();

    /**
     * Raw http servlet response.
     */
    public abstract HttpServletResponse getServletResponse();

    /**
     * Get request parameters dedicated for
     * current context assembly.
     */
    public abstract Map<String, String[]> getDedicatedRequestParams();

    /**
     * Get the specifica dedicated request parameter by specified name.
     *
     * @param pname Parameter name stripped from assembly parameter prefix.
     */
    public abstract String getDedicatedParam(String pname);

    /**
     * Assembly instance bound to this context.
     * You can freely change properties of this assembly instance.
     */
    public abstract Asm getAsm();

    /**
     * Create new rendering subcontext for
     * {@link com.softmotions.ncms.asm.Asm assembly}
     * referenced by <code>asmname</code>.
     *
     * @param asmname Name of assembly used in child context.
     * @param out Writer will be used to generate content.
     */
    public abstract AsmRendererContext createSubcontext(String asmname, Writer out) throws AsmResourceNotFoundException;

    /**
     * Guice injector.
     */
    public abstract Injector getInjector();

    /**
     * Classloader used to load resources within this context.
     */
    public abstract ClassLoader getClassLoader();

    /**
     * Locale used in this context.
     * Cannot be null.
     */
    public abstract Locale getLocale();

    public abstract void render() throws AsmRenderingException, IOException;

    public abstract String renderAttribute(String attributeName, Map<String, String> opts);

    /**
     * List resource names.
     */
    public abstract List<String> listResources(String directory, String suffix) throws IOException;

    /**
     * Returns true if resource specified by
     * location exists and can be retrieved by
     * {@link #openResourceReader(String)}
     */
    public abstract boolean isResourceExists(String location);

    /**
     * Resolve resource location, and return resource content reader.
     * If resource is not exists it returns <code>null</code>
     *
     * @param location Resource file reader or <code>null</code> if resource is not exists
     */
    public abstract Reader openResourceReader(String location) throws IOException;

    /**
     * Resolve resource location, and return resource content input stream.
     * If resource is not exists it returns <code>null</code>
     *
     * @param location Resource file input stream or <code>null</code> if resource is not exists
     */
    public abstract InputStream openResourceInputStream(String location) throws IOException;
}
