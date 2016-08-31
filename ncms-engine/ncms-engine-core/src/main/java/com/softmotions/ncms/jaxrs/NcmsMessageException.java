package com.softmotions.ncms.jaxrs;

import com.softmotions.weboot.jaxrs.MessageException;

/**
 * Exception used to generate messages
 * for Qooxdoo based GUI clients.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("ClassWithTooManyConstructors")
public class NcmsMessageException extends MessageException {

    public NcmsMessageException() {
    }

    public NcmsMessageException(String message) {
        super(message);
    }

    public NcmsMessageException(String message, boolean err) {
        super(message, err);
    }

    public NcmsMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public NcmsMessageException(String message, Object request) {
        super(message, request);
    }

    public NcmsMessageException(String message, boolean err, Object request) {
        super(message, err, request);
    }

    public NcmsMessageException(String message, Throwable cause, Object request) {
        super(message, cause, request);
    }

    public NcmsMessageException(Throwable cause) {
        super(cause);
    }
}
