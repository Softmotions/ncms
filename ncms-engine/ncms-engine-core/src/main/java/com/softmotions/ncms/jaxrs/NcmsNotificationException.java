package com.softmotions.ncms.jaxrs;

/**
 * Exception used for lightweight UI notifications.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("ClassWithTooManyConstructors")
public class NcmsNotificationException extends NcmsMessageException {

    public NcmsNotificationException() {
    }

    public NcmsNotificationException(String message) {
        super(message);
    }

    public NcmsNotificationException(String message, boolean err) {
        super(message, err);
    }

    public NcmsNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NcmsNotificationException(String message, Object request) {
        super(message, request);
    }

    public NcmsNotificationException(String message, boolean err, Object request) {
        super(message, err, request);
    }

    public NcmsNotificationException(String message, Throwable cause, Object request) {
        super(message, cause, request);
    }

    public NcmsNotificationException(Throwable cause) {
        super(cause);
    }
}
