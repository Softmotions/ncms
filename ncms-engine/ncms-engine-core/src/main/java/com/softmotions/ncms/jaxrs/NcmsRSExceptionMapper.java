package com.softmotions.ncms.jaxrs;

import java.util.List;
import java.util.MissingResourceException;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.jboss.resteasy.spi.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;
import com.softmotions.weboot.i18n.I18n;
import com.softmotions.weboot.jaxrs.MessageException;
import com.softmotions.weboot.jaxrs.Messages;

/**
 * Handles Ncms REST API exceptions in friendly to qooxdoo GUI clients way.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("ChainOfInstanceofChecks")
@Provider
public class NcmsRSExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(NcmsRSExceptionMapper.class);

    private final I18n i18n;

    static {
        MessageException.APP_ID = "Softmotions";
    }

    @Inject
    public NcmsRSExceptionMapper(I18n i18n) {
        this.i18n = i18n;
    }

    private String shiroExceptionMsg(ShiroException e) {
        if (e instanceof UnauthenticatedException) {
            return "ncms.access.notAuthenticated";
        } else if (e instanceof UnknownAccountException) {
            return "ncms.access.notFound";
        } else {
            return "ncms.jaxrs.forbidden";
        }
    }

    @Nullable
    private String toLocaleMsg(String msg) {
        if (msg == null) {
            return null;
        }
        try {
            return i18n.get(msg);
        } catch (MissingResourceException ignored) {
            return msg;
        }
    }

    @Nullable
    private String toLocaleMsg(String msg, HttpServletRequest req) {
        if (msg == null) {
            return null;
        }
        try {
            return i18n.get(msg, req);
        } catch (MissingResourceException ignored) {
            return msg;
        }
    }

    private String toHeaderMsg(String msg) {
        return Messages.toHeaderMsg(toLocaleMsg(msg));
    }

    private String toHeaderMsg(String msg, MessageException me) {
        Object req = me.getRequest();
        if (req instanceof HttpServletRequest) {
            return Messages.toHeaderMsg(toLocaleMsg(msg, (HttpServletRequest) req));
        } else {
            return Messages.toHeaderMsg(toLocaleMsg(msg));
        }
    }

    @Override
    public Response toResponse(Exception ex) {
        if (log.isDebugEnabled()) {
            log.debug("Mapping of exception:", ex);
        }

        // todo review it!!
        if (!"Softmotions".equals(MessageException.APP_ID)) {
            MessageException.APP_ID = "Softmotions";
        }

        Response.ResponseBuilder rb;
        //noinspection IfStatementWithTooManyBranches
        if (ex instanceof NotFoundException) {

            log.warn("HTTP 404: {}", ex.getMessage());
            rb = Response.status(Response.Status.NOT_FOUND)
                         .type(MediaType.TEXT_PLAIN_TYPE)
                         .entity(ex.getMessage());
            rb.header("X-Softmotions-Err0", toHeaderMsg("ncms.jaxrs.notfound"));

        } else if (ex instanceof MessageException) {

            MessageException mex = (MessageException) ex;
            if (mex.hasErrorMessages()) {
                rb = Response.serverError();
            } else {
                rb = Response.ok();
            }
            if (ex instanceof NcmsNotificationException) {
                rb.header("X-Softmotions", "notification");
            }
            List<String> mlist = mex.getErrorMessages();
            for (int i = 0, l = mlist.size(); i < l; ++i) {
                rb.header("X-Softmotions-Err" + i, toHeaderMsg(mlist.get(i), mex));
            }
            mlist = mex.getRegularMessages();
            for (int i = 0, l = mlist.size(); i < l; ++i) {
                rb.header("X-Softmotions-Msg" + i, toHeaderMsg(mlist.get(i), mex));
            }

        } else if (ex instanceof ShiroException) {

            rb = Response.status(Response.Status.FORBIDDEN)
                         .header("X-Softmotions-Err0",
                                 toHeaderMsg(shiroExceptionMsg((ShiroException) ex)));

        } else if (ex instanceof ForbiddenException) {

            rb = Response.status(Response.Status.FORBIDDEN)
                         .header("X-Softmotions-Err0",
                                 toHeaderMsg(!StringUtils.isBlank(ex.getMessage()) ? ex.getMessage() :
                                             i18n.get("ncms.jaxrs.forbidden")));

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
