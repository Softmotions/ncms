package com.softmotions.ncms.jaxrs;

import javax.ws.rs.core.Response;

import com.softmotions.weboot.i18n.I18n;
import com.softmotions.weboot.jaxrs.MessageException;

/**
 * Exception used to generate messages
 * for Qooxdoo based GUI clients.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
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

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (hasErrorMessages()) {
            return super.fillInStackTrace();
        }
        return this;
    }

    @Override
    public Response.ResponseBuilder inject(Response.ResponseBuilder rb, I18n i18n) {
        return super.inject(rb, i18n);
    }

    public Response.ResponseBuilder injectNotification(Response.ResponseBuilder rb, I18n i18n) {
        rb.header("X-Softmotions", "notification");
        return super.inject(rb, i18n);
    }
}
