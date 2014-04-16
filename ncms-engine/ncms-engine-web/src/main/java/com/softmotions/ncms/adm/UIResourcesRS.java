package com.softmotions.ncms.adm;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.web.security.WSUser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * Accessible GUI resources configuration provider.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/ui")
@Produces("application/json")
public class UIResourcesRS {

    @Inject
    NcmsConfiguration cfg;

    @Inject
    ObjectMapper mapper;

    @Inject
    NcmsMessages msg;

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
        XMLConfiguration xcfg = cfg.impl();
        String cpath = "ui." + section + ".widget";
        for (HierarchicalConfiguration hc : xcfg.configurationsAt(cpath)) {
            String[] widgetRoles = hc.getStringArray("[@roles]");
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
                arr.add(on);
            }
        }
        return arr;
    }
}
