package com.softmotions.ncms.adm;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/workspace")
public class WorkspaceRS {

    @GET
    @Path("{name}")
    public String hello(@PathParam("name") final String name) {
        return "Hello " + name;
    }
}
