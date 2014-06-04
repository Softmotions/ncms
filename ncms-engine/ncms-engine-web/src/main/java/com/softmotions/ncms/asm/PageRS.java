package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.guid.RandomGUID;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Path("adm/pages")
@Produces("application/json")
public class PageRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(PageRS.class);

    final AsmDAO adao;

    final ObjectMapper mapper;

    final NcmsMessages messages;

    @Inject
    public PageRS(SqlSession sess,
                  AsmDAO adao, ObjectMapper mapper,
                  NcmsMessages messages) {
        super(PageRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;
    }


    @PUT
    @Path("/new")
    @Transactional
    public void newpage(@Context HttpServletRequest req,
                        ObjectNode spec) {

        log.info("new page=" + spec);

        String name = spec.hasNonNull("name") ? spec.get("name").asText() : null;
        Long parent = spec.hasNonNull("parent") ? spec.get("parent").asLong() : null;
        String type = spec.hasNonNull("type") ? spec.get("type").asText() : null;

        if (name == null) {
            throw new BadRequestException("name");
        }

        synchronized (Asm.class) {

            String guid;
            Long id;

            do {
                guid = new RandomGUID().toString();
                id = adao.asmSelectIdByName(name);
            } while (id != null); //very uncommon

            insert("insertNewPage",
                   "guid", guid,
                   "name", name,
                   "type", type,
                   "nav_parent_id", parent);
        }
    }

    @Path("/layer")
    @GET
    public Response selectLayer(@Context final HttpServletRequest req) {
        return _selectLayer(req, null);
    }

    @Path("/layer/{path:.*}")
    @GET
    public Response selectLayer(@Context final HttpServletRequest req, @PathParam("path") String path) {
        return _selectLayer(req, path);
    }

    Response _selectLayer(@Context final HttpServletRequest req, final String path) {

        return Response.ok(new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                final JsonGenerator gen = new JsonFactory().createGenerator(output);
                gen.writeStartArray();
                Map q = createSelectLayerQ(path);
                String stmtName = q.containsKey("nav_parent_id") ? "selectChildLayer" : "selectRootLayer";
                try {
                    select(stmtName, new ResultHandler() {
                        public void handleResult(ResultContext context) {
                            Map<String, ?> row = (Map<String, ?>) context.getResultObject();
                            try {
                                gen.writeStartObject();
                                gen.writeNumberField("id", ((Number) row.get("id")).longValue());
                                gen.writeStringField("guid", (String) row.get("guid"));
                                gen.writeStringField("name", (String) row.get("name"));

                                gen.writeStringField("description", (String) row.get("description"));
                                String type = (String) row.get("type");
                                int status = 0;
                                if ("page.folder".equals(type)) {
                                    status |= 1;
                                }
                                gen.writeNumberField("status", status);
                                gen.writeStringField("type", type);
                                gen.writeStringField("options", (String) row.get("options"));
                                gen.writeEndObject();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }, q);
                } finally {
                    gen.writeEndArray();
                }
                gen.flush();
            }
        }).type("application/json")
                .encoding("UTF-8")
                .build();
    }


    Map createSelectLayerQ(String path) {
        Map<String, Object> ret = new TinyParamMap();
        if (path != null) {
            //todo
            //ret.put("nav_parent_id", Long.parseLong(req.getParameter("nav_parent_id")));
        }
        return ret;
    }


}
