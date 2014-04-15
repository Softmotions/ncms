package com.softmotions.ncms.adm;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/ws")
public class WorkspaceRS {

    @GET
    @Path("hello/{name}")
    public String hello(@PathParam("name") final String name,
                        @Context SecurityContext sctx) {
        return "Hello " + name + " you are logged in as: " + sctx.getUserPrincipal();
    }
}
