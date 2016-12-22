package com.softmotions.ncms.asm.render;

import com.google.common.base.MoreObjects;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmTemplateEvaluationException extends AsmRenderingException {

    private final String location;

    private final AsmRendererContext ctx;

    public String getLocation() {
        return location;
    }

    public AsmRendererContext getCtx() {
        return ctx;
    }

    public AsmTemplateEvaluationException(AsmRendererContext ctx, String location, Throwable cause) {
        super(cause);
        this.ctx = ctx;
        this.location = location;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("location", location)
                          .add("ctx", ctx)
                          .add("cause", getCause())
                          .toString();
    }
}
