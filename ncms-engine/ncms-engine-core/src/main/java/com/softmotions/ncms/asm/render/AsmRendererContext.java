package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;
import com.softmotions.commons.cont.Stack;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.weboot.i18n.I18n;

/**
 * Rendering context for assembly renderer.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@NotThreadSafe
@SuppressWarnings("unchecked")
public abstract class AsmRendererContext extends HashMap<String, Object> {

    public static final ThreadLocal<Stack<AsmRendererContext>> ASM_CTX = new ThreadLocal<>();

    protected Map<String, Object> userData;

    protected AsmRendererContext() {
    }

    protected AsmRendererContext(Map<String, Object> userData) {
        this.userData = userData;
    }

    public boolean isRendered(Asm asm) {
        return isRendered(asm.getId());
    }

    public boolean isRendered(Long asmId) {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        if (asmRendererContexts != null) {
            for (AsmRendererContext ctx : asmRendererContexts) {
                if (ctx.getAsm().getId().equals(asmId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isRendered() {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        return asmRendererContexts != null && asmRendererContexts.contains(this);
    }

    /**
     * Push the current context in the ThreadLocal {@link #ASM_CTX}
     */
    public AsmRendererContext push() {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        if (asmRendererContexts == null) {
            asmRendererContexts = new Stack<>();
            ASM_CTX.set(asmRendererContexts);
        }
        if (asmRendererContexts.contains(this)) {
            throw new AsmRenderingException("Cycling assembly dependency on: " + this);
        }
        asmRendererContexts.push(this);
        return this;
    }

    /**
     * Pop the current content from the ThreadLocal {@link #ASM_CTX}
     */
    public AsmRendererContext pop() {
        Stack<AsmRendererContext> asmRendererContexts = ASM_CTX.get();
        asmRendererContexts.pop();
        return this;
    }

    public static AsmRendererContext getSafe() {
        Stack<AsmRendererContext> sctx = ASM_CTX.get();
        if (sctx == null || sctx.isEmpty()) {
            throw new RuntimeException("Missing AsmRendererContext.ASM_CTX for the current thread: " +
                                       Thread.currentThread());
        }
        return sctx.peek();
    }

    @Nullable
    public static AsmRendererContext get() {
        Stack<AsmRendererContext> sctx = ASM_CTX.get();
        if (sctx == null || sctx.isEmpty()) {
            return null;
        }
        return sctx.peek();
    }

    public void setUserData(String key, Object value) {
        if (userData == null) {
            userData = new HashMap<>();
        }
        userData.put(key, value);
    }

    @Nullable
    public <T> T getUserData(String key) {
        if (userData == null) {
            return null;
        }
        //noinspection unchecked
        return (T) userData.get(key);
    }

    @Nonnull
    public AsmRendererContext getRootContext() {
        AsmRendererContext ret = this;
        //noinspection ObjectEquality
        while (ret.getParent() != null && ret != ret.getParent()) {
            ret = ret.getParent();
        }
        return ret;
    }

    public abstract AsmRendererContext getParent();

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
    @Nullable
    public abstract String getDedicatedParam(String pname);

    /**
     * Assembly instance bound to this context.
     * You can freely change properties of this assembly instance.
     */
    @Nonnull
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

    /**
     * Guice injector.
     */
    public abstract Injector getInjector();

    public abstract NcmsEnvironment getEnvironment();

    /**
     * Classloader used to load resources within this context.
     */
    public abstract ClassLoader getClassLoader();

    public abstract PageService getPageService();

    public abstract MediaRepository getMediaRepository();

    /**
     * Locale used in this context.
     * Cannot be null.
     */
    public abstract Locale getLocale();

    public abstract I18n getI18n();

    public abstract void render(@Nullable Writer writer) throws AsmRenderingException, IOException;

    @Nullable
    public abstract Object renderAttribute(String attributeName,
                                           @Nullable Map<String, String> opts);

    @Nullable
    public abstract Object renderAttribute(Asm asm,
                                           String attributeName,
                                           @Nullable Map<String, String> opts);

    public abstract AsmResourceLoader getLoader();

    @Nonnull
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
