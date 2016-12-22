package com.softmotions.ncms.asm.render;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmRenderingException extends RuntimeException {

    public AsmRenderingException(String message) {
        super(message);
    }

    public AsmRenderingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsmRenderingException(Throwable cause) {
        super(cause);
    }
}
