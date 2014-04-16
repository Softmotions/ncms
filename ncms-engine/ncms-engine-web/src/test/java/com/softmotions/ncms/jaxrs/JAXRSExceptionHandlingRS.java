package com.softmotions.ncms.jaxrs;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("test")
@Produces("application/json")
public class JAXRSExceptionHandlingRS {

    @GET
    @Path("runtime-exception")
    public JsonNode runtimeException() {
        throw new RuntimeException("a2d01be21ed4449ba48d0ba2019fa8fd");
    }

    @GET
    @Path("message-exception")
    public JsonNode messageException() {
        NcmsMessageException me = new NcmsMessageException();
        me.addMessage("37e871c1226f425e8b0b774f276c3fa4", true);
        me.addMessage("3635166bb1d940cd868b6cd744ee8cb3", true);
        me.addMessage("11264600f10d455283c5a13de2beb0ac", false);
        throw me;
    }

    @GET
    @Path("message-regular-exception")
    public JsonNode messageRegularException() {
        throw new NcmsMessageException("5fb3c71bf81441c492ac3d7e784c8701", false);
    }

}
