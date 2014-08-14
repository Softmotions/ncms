package com.softmotions.ncms.user;

import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/user/env")
@Produces("application/json")
public class UserEnvRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(UserEnvRS.class);

    @Inject
    public UserEnvRS(SqlSession sess) {
        super(UserEnvRS.class.getName(), sess);
    }


    @PUT
    @Path("set/{type}/{value}")
    @Transactional
    public Collection addSet(@Context HttpServletRequest req,
                             @PathParam("type") String type,
                             @PathParam("value") String value) {
        String vcol = "svalue";
        try {
            Integer.parseInt(value);
            vcol = "ivalue";
        } catch (NumberFormatException ignored) {
        }

        update("addSet",
               "userid", req.getRemoteUser(),
               "vcol", vcol,
               "type", type,
               "value", value);
        return getSet(req, type);
    }


    @DELETE
    @Path("set/{type}/{value}")
    @Transactional
    public Collection delSet(@Context HttpServletRequest req,
                             @PathParam("type") String type,
                             @PathParam("value") String value) {

        String vcol = "svalue";
        try {
            Integer.parseInt(value);
            vcol = "ivalue";
        } catch (NumberFormatException ignored) {
        }
        delete("delSet",
               "userid", req.getRemoteUser(),
               "vcol", vcol,
               "type", type,
               "value", value);

        return getSet(req, type);
    }

    @GET
    @Path("set/{type}")
    @Transactional
    public Collection getSet(@Context HttpServletRequest req,
                             @PathParam("type") String type) {
        List<Map<String, Object>> res = select("getSet",
                                               "type", type,
                                               "userid", req.getRemoteUser());
        List ret = new ArrayList(res.size());
        for (Map<String, Object> rec : res) {
            Object val = rec.get("SVALUE");
            if (val != null) {
                ret.add(val);
            }
            val = rec.get("IVALUE");
            if (val != null) {
                ret.add(val);
            }
        }
        return ret;
    }
}
