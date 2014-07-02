package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.CollectionUtils;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.guid.RandomGUID;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.softmotions.ncms.asm.PageSecurityService.UpdateMode.ADD;
import static com.softmotions.ncms.asm.PageSecurityService.UpdateMode.REMOVE;

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

    final WSUserDatabase userdb;

    final AsmAttributeManagersRegistry amRegistry;

    final PageSecurityService pageSecurity;

    @Inject
    public PageRS(SqlSession sess,
                  AsmDAO adao,
                  ObjectMapper mapper,
                  NcmsMessages messages,
                  WSUserDatabase userdb,
                  AsmAttributeManagersRegistry amRegistry,
                  PageSecurityService pageSecurity) {
        super(PageRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;
        this.userdb = userdb;
        this.amRegistry = amRegistry;
        this.pageSecurity = pageSecurity;
    }


    @GET
    @Path("/path/{id}")
    public ObjectNode selectPageLabelPath(@PathParam("id") Long id) {
        ObjectNode res = mapper.createObjectNode();
        Map<String, Object> qres = selectOne("selectNavPath", "id", id);
        if (qres == null) {
            throw new NotFoundException();
        }
        ArrayNode idPath = res.putArray("idPath");
        ArrayNode labelPath = res.putArray("labelPath");
        ArrayNode guidPath = res.putArray("guidPath");

        String cpath = (String) qres.get("nav_cached_path");
        if (cpath == null) {
            guidPath.add((String) qres.get("guid"));
            labelPath.add((String) qres.get("name"));
            idPath.add(id);
            return res;
        }
        cpath = StringUtils.strip(cpath, "/");
        if (StringUtils.isBlank(cpath)) {
            guidPath.add((String) qres.get("guid"));
            labelPath.add((String) qres.get("name"));
            idPath.add(id);
            return res;
        }
        String[] idsArr = cpath.split("/");
        if (idsArr.length == 0) {
            guidPath.add((String) qres.get("guid"));
            labelPath.add((String) qres.get("name"));
            idPath.add(id);
            return res;
        }
        Long[] ids = new Long[idsArr.length];
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = Long.parseLong(idsArr[i]);
            idPath.add(ids[i]);
        }

        List<Map<String, Object>> rows = select("selectPageInfoIN",
                                                "ids", ids);
        Map<Long, Map<String, Object>> rowsMap = new HashMap<>(ids.length);
        for (Map<String, Object> row : rows) {
            Number eId = (Number) row.get("id");
            if (eId == null) {
                continue;
            }
            rowsMap.put(eId.longValue(), row);
        }
        for (Long eId : ids) {
            Map<String, Object> row = rowsMap.get(eId);
            if (eId == null) {
                labelPath.addNull();
                guidPath.addNull();
            } else {
                labelPath.add((String) row.get("name"));
                guidPath.add((String) row.get("guid"));
            }
        }
        guidPath.add((String) qres.get("guid"));
        labelPath.add((String) qres.get("name"));
        idPath.add(id);
        return res;
    }


    /**
     * Get page data for info pane.
     *
     * @param id Page ID
     * @return
     */
    @GET
    @Path("/info/{id}")
    public ObjectNode selectPageInfo(@Context HttpServletRequest req, @PathParam("id") Long id) {
        Map<String, Object> row = selectOne("selectPageInfo", "id", id);
        if (row == null) {
            throw new NotFoundException();
        }
        ObjectNode res = mapper.createObjectNode();
        JsonUtils.populateObjectNode(row, res);

        String username = (String) row.get("owner");
        WSUser user = (username != null) ? userdb.findUser(username) : null;
        if (user != null) {
            JsonUtils.populateObjectNode(user, res.putObject("owner"),
                                         "name", "fullName");
        }

        username = (String) row.get("muser");
        user = (username != null) ? userdb.findUser(username) : null;
        if (user != null) {
            JsonUtils.populateObjectNode(user, res.putObject("muser"),
                                         "name", "fullName");
        }

        res.put("accessmask", pageSecurity.getUserRights(id, req.getRemoteUser()));
        return res;
    }


    @GET
    @Path("/edit/{id}")
    public ObjectNode selectPageEdit(@Context HttpServletRequest req, @PathParam("id") Long id) {
        ObjectNode res = mapper.createObjectNode();

        Asm page = adao.asmSelectById(id);
        if (page == null) {
            throw new NotFoundException();
        }
        Asm template = null;
        Iterator<Asm> piter = page.getAllParentsIterator();
        while (piter.hasNext()) {
            Asm next = piter.next();
            if (next.isTemplate()) {
                template = next;
                break;
            }
        }
        res.put("id", page.getId());
        res.put("guid", page.getName());
        res.put("name", page.getHname());
        if (template == null) {
            res.putNull("template");
        } else {
            res.putObject("template")
                    .put("id", template.getId())
                    .put("name", template.getName())
                    .put("description", template.getDescription());


        }
        Collection<AsmAttribute> eattrs = page.getEffectiveAttributes();
        Collection<AsmAttribute> gattrs = new ArrayList<>(eattrs.size());
        for (AsmAttribute a : eattrs) {
            if (!StringUtils.isBlank(a.getLabel())) { //it is GUI attribute?
                if (template != null) {
                    AsmAttribute tmplAttr = template.getEffectiveAttribute(a.getName());
                    if (tmplAttr != null && Objects.equals(tmplAttr.getType(), a.getType())) {
                        if (!Objects.equals(tmplAttr.getOptions(), a.getOptions())) {
                            //force template attribute options
                            a.setOptions(tmplAttr.getOptions());
                            update("updateAttributeOptions", a);
                        }
                    }
                    AsmAttributeManager am = amRegistry.getByType(a.getType());
                    if (am != null) {
                        a = am.prepareGUIAttribute(template, tmplAttr, a);
                        if (a == null) {
                            continue;
                        }
                    }
                }
                gattrs.add(a);
            }
        }
        res.putPOJO("attributes", gattrs);
        return res;
    }


    @PUT
    @Path("/edit/{id}")
    public void savePage(@Context HttpServletRequest req,
                         @PathParam("id") Long id,
                         ObjectNode data) {

        Asm page = adao.asmSelectById(id);
        if (page == null) {
            throw new NotFoundException();
        }
        if (!page.getType().startsWith("page")) {
            throw new BadRequestException();
        }

        Map<String, AsmAttribute> attrIdx = new HashMap<>();
        for (AsmAttribute attr : page.getEffectiveAttributes()) {
            if (attr.getLabel() == null || attr.getLabel().isEmpty()) {
                continue; //no gui label, skipping
            }
            attrIdx.put(attr.getName(), attr);
        }
        Iterator<String> fnamesIt = data.fieldNames();
        while (fnamesIt.hasNext()) {
            String fname = fnamesIt.next();
            AsmAttribute attr = attrIdx.get(fname);
            if (attr == null) {
                continue;
            }
            AsmAttributeManager am = amRegistry.getByType(attr.getType());
            if (am == null) {
                log.warn("Missing attribute manager for type: " + attr.getType());
                continue;
            }
            if (attr.getAsmId() != id) { //parent attr
                attr = attr.cloneDeep();
                attr.asmId = id;
            }
            am.applyAttributeValue(attr, data.get(fname));
            update("upsertAttribute", attr);
        }
    }

    /**
     * Set template page for page assembly.
     * Return the same data
     *
     * @param id
     */
    @PUT
    @Path("/template/{id}/{templateId}")
    @Transactional
    public ObjectNode setTemplate(@Context HttpServletRequest req,
                                  @PathParam("id") Long id,
                                  @PathParam("templateId") Long templateId) {
        Integer ts = selectOne("selectPageTemplateStatus", templateId);
        if (ts == null || ts.intValue() == 0) {
            log.warn("Assembly: " + templateId + " is not page template");
            throw new BadRequestException();
        }
        adao.asmRemoveAllParents(id);
        adao.asmSetParent(id, templateId);

        //Strore incompatible attribute names to clean-up
        List<String> attrsToRemove = new ArrayList<>();
        Asm page = adao.asmSelectById(id);
        Collection<AsmAttribute> attrs = page.getEffectiveAttributes();

        for (AsmAttribute a : attrs) {
            AsmAttribute oa = a.getOverridenParent();
            if (oa != null &&
                a.getAsmId() == id.longValue() &&
                !Objects.equals(oa.getType(), a.getType())) { //types incompatible
                attrsToRemove.add(a.getName());
            }
        }

        if (!attrsToRemove.isEmpty()) {
            delete("deleteAttrsByNames",
                   "asmId", id,
                   "names", attrsToRemove);
        }

        return selectPageEdit(req, id);
    }


    /**
     * Set the page owner
     *
     * @param id    Page ID
     * @param owner Owner username
     */
    @PUT
    @Path("/owner/{id}/{owner}")
    @Transactional
    public JsonNode setPageOwner(@PathParam("id") String id,
                                 @PathParam("owner") String owner) {

        WSUser user = userdb.findUser(owner);
        if (user == null) {
            throw new NotFoundException();
        }
        update("setPageOwner",
               "id", id,
               "owner", owner);

        ObjectNode res = mapper.createObjectNode();
        JsonUtils.populateObjectNode(user, res.putObject("owner"),
                                     "name", "fullName");
        return res;
    }


    @PUT
    @Path("/new")
    @Transactional
    public void newPage(@Context HttpServletRequest req,
                        ObjectNode spec) {

        String name = spec.hasNonNull("name") ? spec.get("name").asText() : null;
        Long parent = spec.hasNonNull("parent") ? spec.get("parent").asLong() : null;
        String type = spec.hasNonNull("type") ? spec.get("type").asText() : null;

        if (name == null) {
            throw new BadRequestException("name");
        }
        String guid;
        Long id;
        do {
            guid = new RandomGUID().toString();
            id = adao.asmSelectIdByName(name);
        } while (id != null); //very uncommon

        update("mergeNewPage",
               "guid", guid,
               "name", name,
               "description", name,
               "type", type,
               "user", req.getRemoteUser(),
               "nav_parent_id", parent,
               "nav_cached_path", getPageIDsPath(parent),
               "recursive_acl", selectOne("getRecursiveAcl", "pid", parent));
    }


    private String getPageIDsPath(Long id) {
        if (id == null) {
            return "/";
        }
        List<Long> alist = new ArrayList<>();
        alist.add(id);
        Long pid = id;
        do {
            pid = selectOne("selectParentID", pid);
            if (pid != null) {
                alist.add(pid);
            }
        } while (pid != null);
        Collections.reverse(alist);
        return "/" + CollectionUtils.join("/", alist) + "/";
    }


    @DELETE
    @Path("/{id}")
    @Transactional
    public void dropPage(@Context HttpServletRequest req,
                         @PathParam("id") Long id) {
        delete("dropPage", "id", id);
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
                                gen.writeStringField("label", (String) row.get("name"));
                                gen.writeStringField("description", (String) row.get("description"));
                                String type = (String) row.get("type");
                                int status = 0;
                                if ("page.folder".equals(type)) {
                                    status |= 1;
                                }
                                gen.writeNumberField("status", status);
                                gen.writeStringField("type", type);
                                gen.writeStringField("options", (String) row.get("options"));

                                //todo load page access rights?

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

    @Path("/acl/{pid}")
    @GET
    public JsonNode getAcl(@PathParam("pid") Long pid,
                           @QueryParam("recursive") Boolean recursive) {
        ArrayNode res = mapper.createArrayNode();

        for (PageSecurityService.AclEntity aclEntity : pageSecurity.getAcl(pid, recursive)) {
            res.addPOJO(aclEntity);
        }

        return res;
    }

    @Path("/acl/{pid}/{user}")
    @PUT
    public void addToAcl(@PathParam("pid") Long pid,
                         @PathParam("user") String user,
                         @QueryParam("recursive") boolean recursive) {

        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        pageSecurity.addUserRights(pid, user, recursive);
    }

    @Path("/acl/{pid}/{user}")
    @POST
    public void updateAcl(@PathParam("pid") Long pid,
                          @PathParam("user") String user,
                          @QueryParam("recursive") boolean recursive,
                          @QueryParam("rights") String rights,
                          @QueryParam("add") boolean isAdd) {
        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        pageSecurity.updateUserRights(pid, user, rights, isAdd ? ADD : REMOVE, recursive);
    }

    @Path("/acl/{pid}/{user}")
    @DELETE
    public void deleteFromAcl(@PathParam("pid") Long pid,
                              @PathParam("user") String user,
                              @QueryParam("recursive") boolean recursive,
                              @QueryParam("forceRecursive") boolean force) {
        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        if (force) {
            pageSecurity.deleteUserRecursive(pid, user);
        } else {
            pageSecurity.deleteUserRights(pid, user, recursive);
        }
    }

    Long getPathLastIdSegment(String path) {
        if (path == null) {
            return null;
        }
        int idx = path.lastIndexOf('/');
        if (idx == -1 || idx == path.length() - 1) {
            return Long.valueOf(path);
        }
        return Long.valueOf(path.substring(idx + 1));
    }


    Map createSelectLayerQ(String path) {
        Long pId = getPathLastIdSegment(path);
        Map<String, Object> ret = new TinyParamMap();
        if (pId != null) {
            ret.put("nav_parent_id", pId);
        }
        return ret;
    }


}
