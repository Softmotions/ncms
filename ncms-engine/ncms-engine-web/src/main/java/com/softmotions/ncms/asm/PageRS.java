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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Inject
    public PageRS(SqlSession sess,
                  AsmDAO adao, ObjectMapper mapper,
                  NcmsMessages messages,
                  WSUserDatabase userdb,
                  AsmAttributeManagersRegistry amRegistry) {
        super(PageRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;
        this.userdb = userdb;
        this.amRegistry = amRegistry;
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

        res.put("accessmask", getPageAccessMask(req, row));
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
    public JsonNode getAcl(@PathParam("pid") Long pid) {
        List<Map<String, ?>> acl = select("selectAclUserRights", "pid", pid);
        Collections.sort(acl, new Comparator<Map<String, ?>>() {
            public int compare(Map<String, ?> a1, Map<String, ?> a2) {
                int res = (Integer) a1.get("recursive") - (Integer) a2.get("recursive");
                return res != 0 ? res : ((String) a1.get("user")).compareTo((String) a2.get("user"));
            }
        });

        ArrayNode res = mapper.createArrayNode();
        for (Map<String, ?> user : acl) {
            WSUser wsUser = userdb.findUser((String) user.get("user"));
            if (wsUser == null) {
                continue;
            }
            res.addObject()
                    .put("recursive", (Integer) user.get("recursive"))
                    .put("user", wsUser.getName())
                    .put("userFullName", wsUser.getFullName())
                    .put("rights", (String) user.get("rights"));
        }

        return res;
    }

    @Path("/acl/{pid}/{user}")
    @PUT
    public JsonNode addToAcl(@Context final HttpServletRequest req,
                             @PathParam("pid") Long pid,
                             @PathParam("user") String user) {
        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);

        String defaultRights = "";
        if (StringUtils.equals(wsUser.getName(), (CharSequence) aclInfo.get("owner"))) {
            defaultRights = "wnd";
        }

        Number localAcl = aclInfo != null ? (Number) aclInfo.get("local_acl") : null;
        if (localAcl != null) {
            Integer count = selectOne("checkUserInAcl", "user", wsUser.getName(), "aids", new Number[]{localAcl});
            if (count > 0) {
                throw new BadRequestException("User already in local ACL");
            }
        } else {
            localAcl = selectOne("newAclId");
            update("setLocalAcl", "pid", pid, "acl", localAcl);
        }

        update("updateAclUserRights", "acl", localAcl, "user", wsUser.getName(), "rights", defaultRights);

        return mapper.createObjectNode()
                .put("recursive", 0)
                .put("user", wsUser.getName())
                .put("userFullName", wsUser.getFullName())
                .put("rights", "");
    }

    @Path("/acl/{pid}/{user}")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public JsonNode updateAcl(@Context final HttpServletRequest req,
                              @PathParam("pid") Long pid,
                              @PathParam("user") String user,
                              MultivaluedMap<String, String> form) {
        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }
        if (!form.containsKey("recursive") || form.get("recursive") == null || form.get("recursive").isEmpty()) {
            throw new BadRequestException();
        }

        boolean cr = BooleanUtils.toBoolean(form.get("recursive").get(0));

        String navPath = selectOne("selectNavPagePath", "pid", pid);

        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);
        Number recAcl = aclInfo != null ? (Number) aclInfo.get("recursive_acl") : null;
        Number locAcl = aclInfo != null ? (Number) aclInfo.get("local_acl") : null;

        List<String> rrParams = form.containsKey("role.recursive") ? form.get("role.recursive") : null;
        List<String> nRights = form.containsKey("rights") ? form.get("rights") : null;
        if (rrParams != null && !rrParams.isEmpty()) {
            boolean isSet = BooleanUtils.toBoolean(rrParams.get(0));
            if (isSet) {
                String locURights = locAcl != null ? (String) selectOne("selectUserRights", "user", wsUser.getName(), "acl", locAcl) : null;

                updateRecursiveAclUser(pid, navPath, wsUser.getName(), StringUtils.isBlank(locURights) ? "" : locURights, recAcl, true);

                if (locAcl != null) {
                    delete("deleteAclUser", "acl", locAcl, "user", wsUser.getName());
                }
            } else {
                unsetRecursiveForParents(pid, wsUser.getName(), navPath, true);

                List<Number> racls = select("childRecursiveAcls", "nav_path", navPath + pid + "/%", "with_user", wsUser.getName());
                for (Number racl : racls) {
                    Number nracl= selectOne("newAclId");
                    update("copyAcl", "prev_acl", racl, "new_acl", nracl);
                    update("updateChildRecursiveAcl",
                           "nav_path", navPath + pid + "/%",
                           "prev_acl", racl,
                           "new_acl", nracl);

                    delete("deleteAclUser", "acl", nracl, "user", wsUser.getName());
                }
            }
        } else if (nRights != null && !nRights.isEmpty()) {
            String rights = nRights.get(0);
            if (!cr) {
                // update local acl
                if (locAcl == null) {
                    locAcl = selectOne("newAclId");
                    update("setLocalAcl", "pid", pid, "acl", locAcl);
                }
                update("updateAclUserRights", "acl", locAcl, "user", wsUser.getName(), "rights", rights);
            } else {
                updateRecursiveAclUser(pid, navPath, wsUser.getName(), rights, recAcl, false);
            }
        }

        return mapper.createObjectNode();
    }

    @Path("/acl/{pid}/{user}")
    @DELETE
    public void deleteFromAcl(@Context final HttpServletRequest req,
                              @PathParam("pid") Long pid,
                              @PathParam("user") String user,
                              @QueryParam("recursive") boolean recursive) {

        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);

        Number locAcl = aclInfo != null ? (Number) aclInfo.get("local_acl") : null;
        Number recAcl = aclInfo != null ? (Number) aclInfo.get("recursive_acl") : null;
        if (!recursive && locAcl != null) {
            delete("deleteAclUser", "user", wsUser.getName(), "acl", locAcl);
        } else if (recursive && recAcl != null) {
            String navPath = selectOne("selectNavPagePath", "pid", pid);

            Number newRecAcl = selectOne("newAclId");
            update("copyAcl", "prev_acl", recAcl, "new_acl", newRecAcl);

            update("updateChildRecursiveAcl",
                   "pid", pid,
                   "nav_path", navPath + pid + "/%",
                   "prev_acl", recAcl,
                   "new_acl", newRecAcl);

            delete("deleteAclUser", "acl", newRecAcl, "user", wsUser.getName());

            List<Number> racls = select("childRecursiveAcls", "nav_path", navPath + pid + "/%", "exclude_acl", newRecAcl, "with_user", user);
            for (Number racl : racls) {
                Number nracl= selectOne("newAclId");
                update("copyAcl", "prev_acl", racl, "new_acl", nracl);
                update("updateChildRecursiveAcl",
                       "nav_path", navPath + pid + "/%",
                       "prev_acl", racl,
                       "new_acl", nracl);

                delete("deleteAclUser", "acl", nracl, "user", wsUser.getName());
            }

            unsetRecursiveForParents(pid, user, navPath, false);
        }
    }

    private String mergeRights(String r1, String r2) {
        String res = r1 != null ? r1 : "";
        for (char r : (r2 != null ? r2 : "").toCharArray()) {
            if (!StringUtils.contains(res, r)) {
                res += r;
            }
        }

        return res;
    }

    private String unsetRights(String from, String r) {
        return (from != null ? from : "").replaceAll("[" + (r != null ? r : "") + "]", "");
    }

    private void updateRecursiveAclUser(Long pid, String navPath, String user, String rights, Number recAcl, boolean isSet) {
        String urights = null;
        Number newRecAcl = selectOne("newAclId");
        if (recAcl != null) {
            if (isSet) {
                Integer count = selectOne("checkUserInAcl", "user", user, "aids", new Number[]{recAcl});
                if (count > 0) {
                    throw new BadRequestException("User already in recursive ACL");
                }
            } else {
                urights = unsetRights((String) selectOne("selectUserRights", "user", user, "acl", recAcl), rights);
                urights = StringUtils.isBlank(urights) ? null : urights;
            }

            update("copyAcl", "prev_acl", recAcl, "new_acl", newRecAcl);
        }

        update("updateChildRecursiveAcl",
               "pid", pid,
               "nav_path", navPath + pid + "/%",
               "prev_acl", recAcl,
               "new_acl", newRecAcl);
        update("updateAclUserRights", "acl", newRecAcl, "user", user, "rights", rights);

        List<Number> racls = select("childRecursiveAcls", "nav_path", navPath + pid + "/%", "exclude_acl", newRecAcl);
        for (Number racl : racls) {
            String rrights = selectOne("selectUserRights", "user", user, "acl", racl);
            if (rrights == null && urights != null) {
                continue;
            }

            Number nracl= selectOne("newAclId");
            update("copyAcl", "prev_acl", racl, "new_acl", nracl);
            update("updateChildRecursiveAcl",
                   "nav_path", navPath + pid + "/%",
                   "prev_acl", racl,
                   "new_acl", nracl);

            String nrights = urights == null ? mergeRights(rrights, rights) : unsetRights(rrights, urights);
            update("updateAclUserRights", "acl", nracl, "user", user, "rights", nrights);
        }
    }

    private void unsetRecursiveForParents(Long pid, String user, String navPath, boolean includeCurrent) {
        List<Map<String, ?>> ptus = select("parentAcls", "nav_path", navPath + (includeCurrent ? pid + "/" : ""));
        for (Map<String, ?> ptu : ptus) {
            Number pra = (Number) ptu.get("recursive_acl");
            String prur = pra != null ? (String) selectOne("selectUserRights", "user", user, "acl", pra) : null;
            if (prur == null) {
                // noop; user doesn't have recursive rights for this page.
                break;
            }

            Number cpid = (Number) ptu.get("id");
            Number pla = (Number) ptu.get("local_acl");
            String plur = "";

            if (pla == null) {
                pla = selectOne("newAclId");
                update("setLocalAcl", "pid", cpid, "acl", pla);
            } else {
                plur = selectOne("selectUserRights", "user", user, "acl", pla);
            }
            update("updateAclUserRights", "acl", pla, "user", user, "rights", mergeRights(prur, plur));

            Number npra = selectOne("newAclId");
            update("copyAcl", "prev_acl", pra, "new_acl", npra);
            delete("deleteAclUser", "acl", npra, "user", user);
            update("setRecursiveAcl", "pid", cpid, "acl", npra);
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


    private String getPageAccessMask(HttpServletRequest req, Map<String, Object> row) {
        // r - read access always set
        String user = req.getRemoteUser();
        if (user == null) {
            return "";
        }
        String owner = (String) row.get("owner");
        if (user.equals(owner) || req.isUserInRole("admin.structure")) {
            return "wnds";
        }

        String rights = "";
        List<Map<String, ?>> arights = select("selectAclUserRights", "pid", row.get("id"), "user", req.getRemoteUser());
        for (Map<String, ?> aright : arights) {
            rights = mergeRights(rights, (String) aright.get("rights"));
        }

        return rights;
    }


}
