package com.softmotions.ncms.asm.render;

import com.softmotions.commons.cont.Stack;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.asm.Asm;

import com.google.inject.Injector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Rendering context for assembly rendereres.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class AsmRendererContext extends HashMap<String, Object> {

    public static final ThreadLocal<Stack<AsmRendererContext>> ASM_CTX = new ThreadLocal<>();

    public final Stack<Boolean> escapeStack = new Stack<>();

    /**
     * Push the current context in the ThreadLocal {@link #ASM_CTX}
     */
    public void push() {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        if (asmRendererContexts == null) {
            asmRendererContexts = new Stack<>();
            ASM_CTX.set(asmRendererContexts);
        }
        if (asmRendererContexts.contains(this)) {
            throw new AsmRenderingException("Cycling assembly dependency on: " + this);
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
     * Return true is current context is subcontext.
     *
     * @return
     */
    public abstract boolean isSubcontext();

    /**
     * Create new rendering subcontext for
     * {@link com.softmotions.ncms.asm.Asm assembly}
     * referenced by <code>asmname</code>.
     *
     * @param asmname Name of assembly used in child context.
     * @param out     Writer will be used to generate content.
     */
    public abstract AsmRendererContext createSubcontext(String asmname, Writer out) throws AsmResourceNotFoundException;

    public abstract AsmRendererContext createSubcontext(Asm asm) throws AsmResourceNotFoundException;

    /**
     * Guice injector.
     */
    public abstract Injector getInjector();

    public abstract NcmsConfiguration getCfg();

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

    public abstract Object renderAttribute(String attributeName, Map<String, String> opts);

    public abstract AsmResourceLoader getLoader();

    public String toString() {
        return "AsmRendererContext{asm=" + getAsm() + ", context=" + super.toString() + '}';
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        return getAsm().equals(((AsmRendererContext) o).getAsm());
    }

    public int hashCode() {
        return getAsm().hashCode();
    }
}
