package com.softmotions.ncms.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@SuppressWarnings("unchecked")
@Path("adm/user/env")
@Produces("application/json;charset=UTF-8")
public class UserEnvRS extends MBDAOSupport {

    @Inject
    public UserEnvRS(SqlSession sess) {
        super(UserEnvRS.class, sess);
    }

    @PUT
    @Path("single/{type}")
    @Transactional
    public Collection ensureSingle(@Context HttpServletRequest req,
                                   @PathParam("type") String type,
                                   String value) {
        delAllSet(req, type);
        return addSet(req, type, value);
    }

    @PUT
    @Path("set/{type}")
    @Transactional
    public Collection addSet(@Context HttpServletRequest req,
                             @PathParam("type") String type,
                             String value) {
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
    @Path("set/{type}")
    @Transactional
    public Collection delSet(@Context HttpServletRequest req,
                             @PathParam("type") String type,
                             String value) {

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


    @DELETE
    @Path("clear/{type}")
    @Transactional
    public void delAllSet(@Context HttpServletRequest req,
                          @PathParam("type") String type) {
        delete("delAllSet",
               "userid", req.getRemoteUser(),
               "type", type);
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
            Object val = rec.get("svalue");
            if (val != null) {
                ret.add(val);
            }
            val = rec.get("ivalue");
            if (val != null) {
                ret.add(val);
            }
        }
        return ret;
    }
}
