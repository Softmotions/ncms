package com.softmotions.ncms.adm;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.web.security.WSUser;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
@Produces("application/json;charset=UTF-8")
public class WorkspaceRS {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceRS.class);

    private final NcmsEnvironment env;

    private final PageService pageService;

    @Inject
    public WorkspaceRS(NcmsEnvironment env, PageService pageService) {
        this.env = env;
        this.pageService = pageService;
    }

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
        HttpSession sess = req.getSession(false);
        if (sess != null) {
            sess.invalidate();
        }
        try {
            resp.sendRedirect(env.getLogoutRedirect());
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public class WSUserState extends HashMap<String, Object> {

        final Map<String, Object> properties = new HashMap<>();

        public WSUserState(WSUser user, HttpServletRequest req) {
            put("appName", env.getApplicationName());
            put("sessionId", req.getSession().getId());
            put("userId", user.getName());
            put("userLogin", user.getName());
            put("userFullName", user.getFullName());
            put("roles", user.getRoleNames());
            put("email", user.getEmail());
            put("time", new Date());
            put("helpSite", getHelpSite());
            properties.put("helpWiki", getHelpWikiSite());
            put("properties", properties);
        }
    }

    private String getHelpSite() {
        String alias = env.xcfg().getString("help.site[@alias]");
        if (!StringUtils.isBlank(alias)) {
            return pageService.resolvePageLink(alias);
        } else {
            return env.xcfg().getString("help.site");
        }
    }

    private String getHelpWikiSite() {
        String alias = env.xcfg().getString("help.wiki[@alias]");
        if (!StringUtils.isBlank(alias)) {
            return pageService.resolvePageLink(alias);
        } else {
            return env.xcfg().getString("help.wiki");
        }
    }
}
