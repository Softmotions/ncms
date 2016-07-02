package com.softmotions.ncms.jaxrs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;
import com.softmotions.ncms.NcmsMessages;

/**
 * Handles Ncms REST API exceptions in friendly to qooxdoo GUI clients way.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("ChainOfInstanceofChecks")
@Provider
public class NcmsRSExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(NcmsRSExceptionHandler.class);

    public static final int MAX_MSG_LEN = 2048;

    private final NcmsMessages messages;

    @Inject
    public NcmsRSExceptionHandler(NcmsMessages messages) {
        this.messages = messages;
    }

    private String toHeaderMsg(String msg) {
        try {
            String str = StringUtils.left(URLEncoder.encode(msg, "UTF-8"), MAX_MSG_LEN);
            if (str.endsWith("%")) {  //todo review!!!
                str = str.substring(0, str.length() - 1);
            }
            return str;
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
        }
        return "";
    }

    public Response toResponse(Exception ex) {

        Response.ResponseBuilder rb;

        if (ex instanceof NotFoundException) {
            log.warn("HTTP 404: " + ex.getMessage());
            rb = Response.status(Response.Status.NOT_FOUND)
                         .type(MediaType.TEXT_PLAIN_TYPE)
                         .entity(ex.getMessage());
            rb.header("X-Softmotions-Err0", toHeaderMsg(messages.get("ncms.jaxrs.notfound")));

        } else if (ex instanceof NcmsMessageException) {

            NcmsMessageException mex = (NcmsMessageException) ex;
            if (mex.hasErrorMessages()) {
                rb = Response.serverError();
            } else {
                rb = Response.ok();
            }
            List<String> mlist = mex.getErrorMessages();
            for (int i = 0, l = mlist.size(); i < l; ++i) {
                rb.header("X-Softmotions-Err" + i, toHeaderMsg(mlist.get(i)));
            }
            mlist = mex.getRegularMessages();
            for (int i = 0, l = mlist.size(); i < l; ++i) {
                rb.header("X-Softmotions-Msg" + i, toHeaderMsg(mlist.get(i)));
            }

        } else if (ex instanceof ForbiddenException) {
            rb = Response.status(Response.Status.FORBIDDEN)
                         .header("X-Softmotions-Err0",
                                 toHeaderMsg(!StringUtils.isBlank(ex.getMessage()) ? ex.getMessage() :
                                             messages.get("ncms.jaxrs.forbidden")));

        } else if (ex instanceof JsonMappingException ||
                   ex instanceof JsonParseException ||
                   ex instanceof ReaderException ||
                   ex instanceof BadRequestException ||
                   ex instanceof javax.ws.rs.BadRequestException) {

            log.warn("", ex);
            rb = Response.status(Response.Status.BAD_REQUEST)
                         .header("X-Softmotions-Err0",
                                 toHeaderMsg(!StringUtils.isBlank(ex.getMessage()) ? ex.getMessage() : ex.toString()));
        } else {

            log.error("", ex);
            rb = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .header("X-Softmotions-Err0",
                                 toHeaderMsg(!StringUtils.isBlank(ex.getMessage()) ? ex.getMessage() : ex.toString()));
        }

        return rb != null ? rb.build() : Response.serverError().build();
    }
}
