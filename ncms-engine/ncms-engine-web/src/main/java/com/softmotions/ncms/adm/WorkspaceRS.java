package com.softmotions.ncms.adm;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.web.security.WSUser;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/ws")
@Produces("application/json")
public class WorkspaceRS {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceRS.class);

    @Inject
    NcmsConfiguration cfg;

    @GET
    @Path("state")
    public WSUserState state(@Context SecurityContext sctx,
                             @Context HttpServletRequest req) {
        WSUser user = (WSUser) sctx.getUserPrincipal();
        return new WSUserState(user, req);
    }

    @PUT
    @Path("state")
    public void saveState(Map<String, Object> props) {
        log.info("Save state: " + props);
        //todo
    }

    @PUT
    @Path("state/{property}")
    public void saveStateProperty(@PathParam("property") String property, String value) {
        log.info("Save state[ " + property + "] = " + value);
        //todo
    }

    @GET
    @Path("logout")
    public void logout(@Context HttpServletRequest req,
                       @Context HttpServletResponse resp) {
        req.getSession().invalidate();
        try {
            resp.sendRedirect(cfg.getLogoutRedirect());
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public class WSUserState extends HashMap<String, Object> {

        final Map<String, Object> properties = new HashMap<>();

        public WSUserState(WSUser user, HttpServletRequest req) {
            put("appName", cfg.getApplicationName());
            put("sessionId", req.getSession().getId());
            put("userId", user.getName());
            put("userLogin", user.getName());
            put("userFullName", user.getFullName());
            put("email", user.getEmail());
            put("time", new Date());
            put("helpSite", cfg.getHelpSite());
            put("properties", properties);
        }
    }
}
