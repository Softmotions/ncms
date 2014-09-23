package com.softmotions.ncms.asm.render;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRendererContextFactory {

    AsmRendererContext createStandalone(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        Object asmRef) throws AsmRenderingException;
}
