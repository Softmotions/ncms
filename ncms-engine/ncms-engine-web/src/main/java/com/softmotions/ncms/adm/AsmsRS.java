package com.softmotions.ncms.adm;

import com.softmotions.commons.weboot.mb.MBCriteriaQuery;
import com.softmotions.commons.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Map;

/**
 * Assemblies selector.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/asms")
public class AsmsRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmsRS.class);

    final ObjectMapper mapper;


    @Inject
    public AsmsRS(ObjectMapper mapper, SqlSession sql) {
        super(AsmsRS.class.getName(), sql);
        this.mapper = mapper;
    }

    @GET
    @Path("select")
    @Produces("application/json")
    @Transactional
    public JsonNode select(@Context HttpServletRequest req) {
        ArrayNode asmList = mapper.createArrayNode();
        List<Map> results = selectByCriteria(createQ(req).withStatement("select"));
        //todo
        return asmList;
    }

    @GET
    @Path("select/count")
    @Produces("text/plain")
    @Transactional
    public Integer count(@Context HttpServletRequest req) {
        return selectOneByCriteria(createQ(req).withStatement("count"));
    }

    private MBCriteriaQuery createQ(HttpServletRequest req) {
        MBCriteriaQuery cq = createCriteria();
        String val = req.getParameter("firstRow");
        if (val != null) {
            Integer frow = Integer.valueOf(val);
            cq.offset(frow);
            val = req.getParameter("lastRow");
            if (val != null) {
                Integer lrow = Integer.valueOf(val);
                cq.limit(Math.abs(frow - lrow) + 1);
            }
        }
        val = req.getParameter("stext");
        if (!StringUtils.isBlank(val)) {
            val = val.trim() + '%';
            cq.withParam("name", val);
        }
        val = req.getParameter("sortAsc");
        if (!StringUtils.isBlank(val)) {
            cq.orderBy(val).asc();
        }
        val = req.getParameter("sortDesc");
        if (!StringUtils.isBlank(val)) {
            cq.orderBy(val).asc();
        }
        return cq;
    }


}
