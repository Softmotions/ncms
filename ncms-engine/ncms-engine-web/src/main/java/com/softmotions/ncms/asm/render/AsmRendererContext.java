package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;

import com.google.inject.Injector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Rendering context for assembly rendereres.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRendererContext extends AsmResourceResolver, Map<String, Object> {

    /**
     * Raw http servlet request.
     */
    HttpServletRequest getServletRequest();

    /**
     * Raw http servlet response.
     */
    HttpServletResponse getServletResponse();

    /**
     * Get request parameters dedicated for
     * current context assembly.
     */
    Map<String, String[]> getDedicatedRequestParams();

    /**
     * Get the specifica dedicated request parameter by specified name.
     *
     * @param pname Parameter name stripped from assembly parameter prefix.
     */
    String getDedicatedParam(String pname);

    /**
     * Assembly instance bound to this context.
     * You can freely change properties of this assembly instance.
     */
    Asm getContextAsm();

    /**
     * Create new rendering subcontext for
     * {@link com.softmotions.ncms.asm.Asm assembly}
     * referenced by <code>asmname</code>.
     *
     * @param asmname Name of assembly used in child context.
     */
    AsmRendererContext createSubcontext(String asmname);

    /**
     * Guice injector.
     */
    Injector getInjector();


    ClassLoader getClassLoader();

}
