package com.softmotions.ncms.asm.render;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmResourceNotFoundException extends AsmRenderingException {

    final String resource;

    public String getResource() {
        return resource;
    }

    public AsmResourceNotFoundException(String resource) {
        super(resource);
        this.resource = resource;
    }

    public AsmResourceNotFoundException(String resource, Throwable cause) {
        super(resource, cause);
        this.resource = resource;
    }
}
