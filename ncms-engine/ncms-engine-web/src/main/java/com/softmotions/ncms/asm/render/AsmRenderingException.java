package com.softmotions.ncms.asm.render;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRenderingException extends RuntimeException {

    public AsmRenderingException(String message) {
        super(message);
    }

    public AsmRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
