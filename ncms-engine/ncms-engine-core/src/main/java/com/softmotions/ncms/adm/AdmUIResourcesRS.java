package com.softmotions.ncms.adm;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.security.WSUser;
import com.softmotions.weboot.i18n.I18n;

/**
 * Accessible GUI resources configuration provider.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/ui")
@Produces("application/json;charset=UTF-8")
public class AdmUIResourcesRS {

    private static final Logger log = LoggerFactory.getLogger(AdmUIResourcesRS.class);

    final NcmsEnvironment env;

    final ObjectMapper mapper;

    final I18n msg;


    @Inject
    public AdmUIResourcesRS(NcmsEnvironment env, ObjectMapper mapper, I18n msg) {
        this.env = env;
        this.mapper = mapper;
        this.msg = msg;
    }

    /**
     * List of qooxdoo widgets available for user.
     *
     * @param section Section name widgets belongs to
     */
    @GET
    @Path("widgets/{section}")
    public JsonNode parts(@Context SecurityContext sctx,
                          @Context HttpServletRequest req,
                          @PathParam("section") String section) {
        ArrayNode arr = mapper.createArrayNode();
        WSUser user = (WSUser) sctx.getUserPrincipal();
        HierarchicalConfiguration<ImmutableNode> xcfg = env.xcfg();
        String cpath = "ui." + section + ".widget";
        for (HierarchicalConfiguration hc : xcfg.configurationsAt(cpath)) {
            String[] widgetRoles = env.attrArray(hc.getString("[@roles]"));
            if (widgetRoles.length == 0 || user.isHasAnyRole(widgetRoles)) {
                String qxClass = hc.getString("[@qxClass]");
                String icon = hc.getString("[@qxIcon]");
                ObjectNode on = mapper.createObjectNode()
                                      .put("qxClass", qxClass);
                String label = msg.get(qxClass + ".label", req);
                on.put("label", label != null ? label : qxClass);
                if (icon != null) {
                    on.put("icon", icon);
                }
                ArrayNode argsNode = on.putArray("args");
                for (String arg : env.attrArray(hc.getString("[@args]"))) {
                    argsNode.add(arg);
                }
                arr.add(on);
            }
        }
        return arr;
    }
}
