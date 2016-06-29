package com.softmotions.ncms.jaxrs;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;

/**
 * Set defaultcharset for request content type to UTF-8
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Provider
public class ResteasyUTF8CharsetFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(InputPart.DEFAULT_CONTENT_TYPE_PROPERTY, "text/plain; charset=UTF-8");
        requestContext.setProperty(InputPart.DEFAULT_CHARSET_PROPERTY, "UTF-8");
    }
}
