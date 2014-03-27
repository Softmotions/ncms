package com.softmotions.ncms.asm;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmException extends RuntimeException {

    public AsmException(String message) {
        super(message);
    }

    public AsmException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsmException(Throwable cause) {
        super(cause);
    }
}
