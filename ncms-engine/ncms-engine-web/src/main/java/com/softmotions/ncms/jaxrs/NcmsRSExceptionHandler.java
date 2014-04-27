package com.softmotions.ncms.jaxrs;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;

/**
 * Handles Ncms REST API exceptions in friendly to qooxdoo GUI clients way.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("ChainOfInstanceofChecks")
@Provider
public class NcmsRSExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(NcmsRSExceptionHandler.class);

    public static final int MAX_MSG_LEN = 1024;

    public Response toResponse(Exception ex) {

        Response.ResponseBuilder rb;
        if (ex instanceof NotFoundException) {
            log.warn("HTTP 404: " + ex.getMessage());
            rb = Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity(ex.getMessage());

        } else if (ex instanceof NcmsMessageException) {

            if (log.isDebugEnabled()) {
                log.debug("NcmsMessageException", ex);
            }
            NcmsMessageException mex = (NcmsMessageException) ex;
            if (mex.hasErrorMessages()) {
                rb = Response.serverError();
            } else {
                rb = Response.ok();
            }
            List<String> messages = mex.getErrorMessages();
            for (int i = 0, l = messages.size(); i < l; ++i) {
                rb.header("Softmotions-Msg-Err" + i, StringUtils.left(messages.get(i), MAX_MSG_LEN));
            }
            messages = mex.getRegularMessages();
            for (int i = 0, l = messages.size(); i < l; ++i) {
                rb.header("Softmotions-Msg-Reg" + i, StringUtils.left(messages.get(i), MAX_MSG_LEN));
            }

        } else if (ex instanceof JsonMappingException ||
                   ex instanceof JsonParseException ||
                   ex instanceof ReaderException ||
                   ex instanceof BadRequestException) {

            log.warn("", ex);
            rb = Response.status(Response.Status.BAD_REQUEST)
                    .header("Softmotions-Msg-Err0",
                            ex.getMessage() != null ?
                            StringUtils.left(ex.getMessage(), MAX_MSG_LEN) : ex.toString()
                    );

        } else {
            log.error("", ex);
            rb = Response.serverError()
                    .header("Softmotions-Msg-Err0",
                            ex.getMessage() != null ?
                            StringUtils.left(ex.getMessage(), MAX_MSG_LEN) : ex.toString()
                    );
        }
        return rb.build();
    }
}
