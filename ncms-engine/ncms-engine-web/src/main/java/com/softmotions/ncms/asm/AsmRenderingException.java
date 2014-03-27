package com.softmotions.ncms.asm;

/**
 * Assembly rendering exception.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRenderingException extends AsmException {

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
