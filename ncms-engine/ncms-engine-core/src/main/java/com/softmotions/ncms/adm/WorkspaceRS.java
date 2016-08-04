package com.softmotions.ncms.adm;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.security.NcmsSecurityContext;
import com.softmotions.web.security.WSUser;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/ws")
@Produces("application/json;charset=UTF-8")
public class WorkspaceRS {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceRS.class);

    private final NcmsEnvironment env;

    private final PageService pageService;

    private Map<String, String> helpTopics = new HashMap<>();

    private final NcmsSecurityContext sctx;

    @Inject
    public WorkspaceRS(NcmsEnvironment env,
                       PageService pageService,
                       NcmsSecurityContext sctx) {
        this.env = env;
        this.pageService = pageService;
        this.sctx = sctx;
    }

    @Start
    public void start() {
        List<HierarchicalConfiguration<ImmutableNode>> topics = env.xcfg().configurationsAt("help.topics.topic");
        for (HierarchicalConfiguration topic : topics) {
            String key = topic.getString("[@key]");
            if (StringUtils.isBlank(key)) {
                continue;
            }

            String alias = topic.getString("[@alias]");
            if (!StringUtils.isBlank(alias)) {
                helpTopics.put(key, pageService.resolvePageLink(alias));
            } else {
                helpTopics.put(key, topic.getString("")); //get config tag content
            }
        }
    }

    @GET
    @Path("state")
    public WSUserState state(@Context HttpServletRequest req,
                             @Context HttpServletResponse resp) throws Exception {
        WSUser user = sctx.getWSUser(req);
        if (user == null) {
            if (!req.authenticate(resp)) {
                throw new ForbiddenException("");
            } else {
                user = (WSUser) sctx.getWSUser(req);
            }
            if (user == null) {
                throw new ForbiddenException("");
            }
        }
        return new WSUserState(user, req);
    }

    @PUT
    @Path("state")
    public void saveState(Map<String, Object> props) {
        log.info("Save state: {}", props);
        //todo
    }

    @PUT
    @Path("state/{property}")
    public void saveStateProperty(@PathParam("property") String property, String value) {
        log.info("Save state[ {}] = {}", property, value);
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
            put("properties", properties);
            put("helpTopics", getHelpTopics());
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

    private Map<String, String> getHelpTopics() {
        return helpTopics;
    }
}
