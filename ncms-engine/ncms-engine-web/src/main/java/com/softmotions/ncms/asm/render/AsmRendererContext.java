package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Rendering context for assembly rendereres.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRendererContext {

    /**
     * Raw http servlet request.
     */
    HttpServletRequest getServletRequest();

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

    Asm getContextAsm();

    AsmRendererContext createChildContext(String asmname, String attrname);
}
