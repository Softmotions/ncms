package com.softmotions.ncms.jaxrs;

/**
 * Error in restfull service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsRSException extends NcmsMessageException {

    public NcmsRSException(String message) {
        super(message, true);
    }
}
