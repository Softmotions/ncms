package com.softmotions.ncms.asm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Assembly rendering context.
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRenderingContext {

    AsmEngine getAssmEngine();

    HttpServletRequest getHttpServletRequest();

    HttpServletResponse getHttpServletResponse();

}
