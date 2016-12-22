package com.softmotions.ncms.jaxrs;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
