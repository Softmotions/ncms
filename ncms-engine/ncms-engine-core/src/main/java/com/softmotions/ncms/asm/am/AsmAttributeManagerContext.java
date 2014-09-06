package com.softmotions.ncms.asm.am;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Assembly attribute manager context.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@RequestScoped
public class AsmAttributeManagerContext {

    private static final Logger log = LoggerFactory.getLogger(AsmAttributeManagerContext.class);

    private final HttpServletRequest request;

    public HttpServletRequest getRequest() {
        return request;
    }

    @Inject
    public AsmAttributeManagerContext(HttpServletRequest request) {
        this.request = request;
    }


    public void finish() {

    }
}
