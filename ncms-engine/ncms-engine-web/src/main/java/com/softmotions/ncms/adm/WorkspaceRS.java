package com.softmotions.ncms.adm;

import com.softmotions.web.security.WSUser;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/ws")
@Produces("application/json")
public class WorkspaceRS {

    @GET
    @Path("state")
    public WSUserState state(@Context SecurityContext sctx) {
        WSUser user = (WSUser) sctx.getUserPrincipal();
        WSUserState ustate = new WSUserState(user);
        return ustate;
    }


    @GET
    @Path("hello/{name}")
    public String hello(@PathParam("name") final String name,
                        @Context SecurityContext sctx) {
        return "Hello " + name + " you are logged in as: " + sctx.getUserPrincipal();
    }

    public static class WSUserState extends HashMap<String, Object> {

        public WSUserState(WSUser user) {
            put("userName", user.getName());
            put("fullName", user.getFullName());
            put("email", user.getEmail());
            put("roles", user.getRoleNames());
            put("time", new Date());
        }
    }
}
