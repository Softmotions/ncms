package com.softmotions.ncms.asm;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.softmotions.commons.Converters;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.asm.events.AsmCreatedEvent;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsNotificationException;
import com.softmotions.web.security.WSUser;
import com.softmotions.weboot.i18n.I18n;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;
import com.softmotions.weboot.security.WBSecurityContext;

/**
 * Редактирование выбранного экземпляра ассембли.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
@Path("adm/asms")
@Produces("application/json;charset=UTF-8")
public class AsmRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmRS.class);

    final AsmDAO adao;

    final ObjectMapper mapper;

    final I18n i18n;

    final AsmAttributeManagersRegistry amRegistry;

    final NcmsEventBus ebus;

    final Provider<AsmAttributeManagerContext> amCtxProvider;

    final WBSecurityContext sctx;

    @Inject
    public AsmRS(SqlSession sess,
                 AsmDAO adao, ObjectMapper mapper,
                 AsmAttributeManagersRegistry amRegistry,
                 I18n i18n,
                 NcmsEventBus ebus,
                 Provider<AsmAttributeManagerContext> amCtxProvider,
                 WBSecurityContext sctx) {
        super(AsmRS.class, sess);
        this.adao = adao;
        this.mapper = mapper;
        this.i18n = i18n;
        this.amRegistry = amRegistry;
        this.ebus = ebus;
        this.amCtxProvider = amCtxProvider;
        this.sctx = sctx;
    }

    /**
     * Create new empty assembly instance
     */
    @PUT
    @Path("/new/{name}")
    @Transactional
    public Asm newasm(@Context HttpServletRequest req,
                      @PathParam("name") String name) {
        name = name.trim();
        Asm asm;
        ebus.unlockOnTxFinish(Asm.acquireLock(0L));
        Long id = adao.asmSelectIdByName(name);
        if (id != null) {
            String msg = i18n.get("ncms.asm.name.already.exists", req, name);
            throw new NcmsNotificationException(msg, true, req);
        }
        asm = new Asm(name);
        adao.asmInsert(asm);
        ebus.fireOnSuccessCommit(
                new AsmCreatedEvent(this,
                                    asm,
                                    req));
        return asm;
    }

    @PUT
    @Path("/rename/{id}/{name}")
    @Transactional
    public void rename(@Context HttpServletRequest req,
                       @PathParam("id") Long id,
                       @PathParam("name") String name) {
        name = name.trim();
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        Long oid = adao.asmSelectIdByName(name);
        if (oid != null && !oid.equals(id)) {
            String msg = i18n.get("ncms.asm.name.already.other", req, name);
            throw new NcmsNotificationException(msg, true, req);
        }
        if (oid == null) {
            adao.asmRename(id, name);
        }
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
    }

    @DELETE
    @Path("/delete/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id,
                       @Context HttpServletRequest req) {
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        adao.asmRemove(id);
        ebus.fireOnSuccessCommit(new AsmRemovedEvent(this, id, req));
    }

    @PUT
    @Path("/{id}/core")
    @Transactional
    public ObjectNode corePut(@PathParam("id") Long id,
                              @Context HttpServletRequest req,
                              ObjectNode coreSpec) {
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException("");
        }
        ObjectNode res = mapper.createObjectNode();
        AsmCore core = adao.asmSetCore(asm, coreSpec.get("location").asText());
        res.putPOJO("core", core);
        res.putPOJO("effectiveCore", core);
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
        return res;
    }

    @DELETE
    @Path("/{id}/core")
    @Transactional
    public ObjectNode coreDelete(@PathParam("id") Long id,
                                 @Context HttpServletRequest req) {
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        ObjectNode res = mapper.createObjectNode();
        adao.update("asmUpdateCore",
                    "id", id,
                    "coreId", null);
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        res.putPOJO("core", asm.getCore());
        res.putPOJO("effectiveCore", asm.getEffectiveCore());
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
        return res;
    }

    @GET
    @Path("/get/{id}")
    @JsonView(Asm.ViewFull.class)
    @Transactional
    public Asm get(@PathParam("id") Long id) throws Exception {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException("");
        }
        asm.accessRoles = adao.asmAccessRoles(asm.getId());
        return asm;
    }

    @GET
    @Path("/basic/{name}")
    @Transactional
    public ObjectNode getBasic(@PathParam("name") String name) {
        ObjectNode res = mapper.createObjectNode();
        Asm asm = adao.asmSelectByName(name);
        if (asm == null) {
            throw new NotFoundException("");
        }
        res.put("id", asm.getId());
        res.put("name", asm.getName());
        res.put("description", asm.getDescription());
        return res;
    }


    @DELETE
    @Path("/{id}/parents")
    @Transactional
    public String[] removeParent(@PathParam("id") Long id,
                                 @Context HttpServletRequest req,
                                 JsonNode jsdata) throws IOException {
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException("");
        }
        if (!jsdata.isArray()) {
            throw new BadRequestException("");
        }
        ArrayNode an = (ArrayNode) jsdata;
        for (int i = 0, l = an.size(); i < l; ++i) {
            JsonNode pnode = an.get(i);
            if (!pnode.isObject() || !pnode.has("id")) {
                continue;
            }
            adao.asmRemoveParent(id, pnode.get("id").asLong());
        }
        asm = adao.asmSelectById(id); //refresh
        if (asm == null) {
            throw new NotFoundException("");
        }
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
        return asm.getParentRefs();
    }


    @PUT
    @Path("/{id}/parents")
    @Transactional
    public String[] saveParent(@PathParam("id") Long id,
                               @Context HttpServletRequest req,
                               JsonNode jsdata) throws IOException {
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException("");
        }
        if (!jsdata.isArray()) {
            throw new BadRequestException("");
        }
        ArrayNode an = (ArrayNode) jsdata;
        Set<String> currParents = asm.getAllParentNames();
        Set<Asm> newParents = new HashSet<>();

        for (int i = 0, l = an.size(); i < l; ++i) {
            JsonNode pnode = an.get(i);
            if (!pnode.isObject() || !pnode.has("id") || !pnode.has("name")) {
                continue;
            }
            String pname = pnode.get("name").asText();
            if (currParents.contains(pname) || pname.equals(asm.getName())) {  //self or already parent
                continue;
            }
            Asm pasm = adao.asmSelectById(pnode.get("id").asLong());
            if (pasm == null || !pasm.getName().equals(pname)) {
                continue;
            }
            Set<String> pparents = pasm.getAllParentNames();
            if (pparents.contains(asm.getName())) { //cyclic dependency
                continue;
            }
            newParents.add(pasm);
        }
        for (final Asm np : newParents) { //insert parent
            adao.asmSetParent(asm, np);
        }
        asm = adao.asmSelectById(id); //refresh
        if (asm == null) {
            throw new NotFoundException("");
        }
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
        return asm.getParentRefs();
    }

    @PUT
    @Path("/{id}/props")
    @Transactional
    public void updateAssemblyProps(@PathParam("id") Long id,
                                    @Context HttpServletRequest req,
                                    ObjectNode props) {

        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        TinyParamMap args = new TinyParamMap();
        args.param("id", id);
        if (props.hasNonNull("description")) {
            args.param("description", props.get("description").asText());
        }
        if (props.hasNonNull("published")) {
            args.param("published", props.get("published").asBoolean());
        }
        if (props.hasNonNull("templateMode")) {
            String mode = props.get("templateMode").asText();
            args.param("template", !"none".equals(mode));
            args.param("template_mode", mode);
        }
        if (props.hasNonNull("controller")) {
            String controller = props.get("controller").asText();
            if (!StringUtils.isEmpty(controller)) {
                ClassLoader cl = ObjectUtils.firstNonNull(
                        Thread.currentThread().getContextClassLoader(),
                        getClass().getClassLoader()
                );
                try {
                    Class clazz = cl.loadClass(controller);
                    if (!AsmController.class.isAssignableFrom(clazz)) {
                        //todo report error to user
                        log.warn("Assembly controller does not implement: {}", AsmController.class.getName());
                    }
                } catch (ClassNotFoundException ignored) {
                    //todo report error to user
                    log.warn("Failed to find assembly controller class: {}", controller);
                    controller = "";
                }
            } else {
                controller = null;
            }
            args.param("controller", controller);
        }
        if (props.hasNonNull("accessRoles")) {
            String[] roles = props.get("accessRoles").asText().split(",");
            for (int i = 0, l = roles.length; i < l; ++i) {
                roles[i] = roles[i].trim();
            }
            adao.setAsmAccessRoles(id, roles);
        }
        update("updateAssemblyProps", args);
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
    }


    /**
     * GET asm attribute
     */
    @GET
    @Path("/{id}/attribute/{name}")
    @JsonView(Asm.ViewLarge.class)
    public AsmAttribute getAsmAttribute(@PathParam("id") Long asmId,
                                        @PathParam("name") String name) {
        AsmAttribute attr = adao.selectOne("attrByAsmAndName",
                                           "asm_id", asmId,
                                           "name", name);
        if (attr == null) {
            throw new NotFoundException("");
        }
        return attr;
    }


    @DELETE
    @Path("/{id}/attribute/{name}")
    @Transactional
    public void rmAsmAttribute(@PathParam("id") Long asmId,
                               @PathParam("name") String name,
                               @QueryParam("recursive") Boolean recursive,
                               @Context HttpServletRequest req) {
        ebus.unlockOnTxFinish(Asm.acquireLock(asmId));
        delete("deleteAttribute",
               "name", name,
               "asm_id", asmId);
        if (recursive != null && recursive) {
            deleteAttributeFromChilds(asmId, name);
        }
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, asmId, req)
                        .hint("asmui", true)
        );
    }


    private void deleteAttributeFromChilds(long parentId, String name) {
        update("deleteAttributeFromChilds",
               "parent_id", parentId,
               "name", name);
        List<Number> childIds = select("selectNotEmptyChilds",
                                       "parent_id", parentId);
        for (Number cId : childIds) {
            deleteAttributeFromChilds(cId.longValue(), name);
        }
    }

    /**
     * Reorder assembly attributes.
     *
     * @param ordinal1 Assembly attribute ordinal
     * @param ordinal2 Ordinal of another assembly attribute
     */
    @PUT
    @Path("/attributes/reorder/{ordinal1}/{ordinal2}")
    @Transactional
    public void exchangeAttributesOrdinal(@PathParam("ordinal1") Long ordinal1,
                                          @PathParam("ordinal2") Long ordinal2,
                                          @Context HttpServletRequest req) {
        if (ordinal1.equals(ordinal2)) {
            return;
        }
        Long id = selectOne("selectAsmIdByOrdinal", ordinal1);
        if (id != null) {
            ebus.unlockOnTxFinish(Asm.acquireLock(id));
        }
        update("exchangeAttributesOrdinal",
               "ordinal1", ordinal1,
               "ordinal2", ordinal2);
        if (id != null) {
            ebus.fireOnSuccessCommit(
                    new AsmModifiedEvent(this, id, req)
                            .hint("asmui", true)
            );
        }
    }

    /**
     * PUT asm attributes values/options
     * <p/>
     * Attributes JSON data spec:
     * <pre>
     *     {
     *        {attr name} : {
     *           asmId : {Long?} optional id of attribute owner assembly
     *           type : {String?} attribute type,
     *           value : { attr json value representation ?},
     *           options : { attr json options representation ?}
     *           required {Boolean?}
     *        },
     *              ...
     *     }
     * </pre>
     */
    @PUT
    @Path("/{id}/attributes")
    @Consumes("application/json")
    @Transactional
    public void putAsmAttributes(@PathParam("id") Long id,
                                 @Context SecurityContext sctx,
                                 @Context HttpServletRequest req,
                                 ObjectNode spec) throws Exception {

        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        AsmAttributeManagerContext amCtx = amCtxProvider.get();
        amCtx.setAsmId(id);

        String oldName = spec.hasNonNull("old_name") ? spec.get("old_name").asText() : null;
        String name = spec.get("name").asText();

        if (oldName != null && !oldName.equals(name)) { //attribute renamed
            renameAttribute(id, oldName, name);
        }
        AsmAttribute attr = selectOne("selectAttrByName",
                                      "asm_id", id,
                                      "name", name);
        if (attr == null) {
            attr = new AsmAttribute();
            attr.setName(name);
        }
        if (spec.hasNonNull("type")) {
            String oldType = attr.getType();
            attr.setType(spec.get("type").asText());
            if (!attr.getType().equals(oldType)) {
                attr.setEffectiveValue(null);
            }
        }
        if (spec.hasNonNull("label")) {
            attr.setLabel(spec.get("label").asText());
        }
        if (spec.hasNonNull("required")) {
            attr.setRequired(spec.get("required").asBoolean());
        }
        amCtx.registerAttribute(attr);
        AsmAttributeManager am = amRegistry.getByType(attr.getType());
        if (am != null) {
            if (am.isUniqueAttribute()) {
                Asm asm = adao.asmSelectById(id);
                if (asm == null) {
                    throw new NotFoundException();
                }
                AsmAttribute attr2 = asm.getUniqueEffectiveAttributeByType(attr.getType());
                if (attr2 != null && !Objects.equals(attr.getName(), attr2.getName())) {
                    String msg = i18n.get("ncms.asm.type.exists", req, attr.getType());
                    throw new NcmsNotificationException(msg, true);
                }
            }
            if (spec.hasNonNull("options")) {
                attr = am.applyAttributeOptions(amCtx, attr, spec.get("options"));
            }
            if (spec.hasNonNull("value")) {
                attr = am.applyAttributeValue(amCtx, attr, spec.get("value"));
            }
        } else {
            log.warn("Missing atribute manager for given type: '{}'", attr.getType());
        }
        attr.asmId = id;
        adao.asmUpsertAttribute(attr);
        if (attr.getId() == null) {
            Number gid = selectOne("prevAttrID");
            if (gid != null) {
                attr.setId(gid.longValue());
            }
        }
        if (am != null) {
            am.attributePersisted(amCtx, attr, spec.get("value"), spec.get("options"));
        }
        amCtx.flush();
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req)
                        .hint("asmui", true)
        );
    }

    private void renameAttribute(long asmId, String name, String newName) {
        update("renameAttribute",
               "asm_id", asmId,
               "new_name", newName,
               "old_name", name);
        renameAttributeChilds(asmId, name, newName);
    }

    private void renameAttributeChilds(long parentAsmId, String name, String newName) {
        update("renameAttributeChilds",
               "parent_id", parentAsmId,
               "new_name", newName,
               "old_name", name);
        List<Number> childIds = select("selectNotEmptyChilds",
                                       "parent_id", parentAsmId);
        for (Number cId : childIds) {
            renameAttributeChilds(cId.longValue(), name, newName);
        }
    }

    @GET
    @Path("/select")
    public Response select(@Context final HttpServletRequest req) {
        return Response.ok((StreamingOutput) output -> {
            final JsonGenerator gen = new JsonFactory().createGenerator(output);
            gen.writeStartArray();
            selectByCriteria(createQ(req), context -> {
                Map<String, Object> row = (Map<String, Object>) context.getResultObject();
                try {
                    gen.writeStartObject();
                    int template = BooleanUtils.toInteger(Converters.toBoolean(row.get("template")));
                    String type = (String) row.get("type");
                    if (template == 1) {
                        gen.writeStringField("icon", "ncms/icon/16/asm/template.png");
                    } else if ("news.page".equals(type)) {
                        gen.writeStringField("icon", "ncms/icon/16/asm/news.png");
                    } else if (!StringUtils.isBlank(type)) {
                        gen.writeStringField("icon", "ncms/icon/16/asm/page.png");
                    } else {
                        gen.writeStringField("icon", "ncms/icon/16/asm/other.png");
                    }
                    gen.writeNumberField("id", ((Number) row.get("id")).longValue());
                    gen.writeStringField("name", (String) row.get("name"));
                    gen.writeStringField("hname", (String) row.get("hname"));
                    gen.writeStringField("description", (String) row.get("description"));
                    gen.writeStringField("type", (String) row.get("type"));
                    gen.writeNumberField("published", BooleanUtils.toInteger(Converters.toBoolean(row.get("published"))));
                    gen.writeNumberField("template", template);
                    gen.writeEndObject();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "select");
            gen.writeEndArray();
            gen.flush();
        }).type("application/json;charset=UTF-8")
                       .build();
    }

    @GET
    @Path("select/count")
    @Produces("text/plain;charset=UTF-8")
    @Transactional
    public Integer count(@Context HttpServletRequest req) {
        //noinspection ConstantConditions
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
        val = req.getParameter("type");
        if (val != null) {
            cq.withParam("type", val);
        }
        val = req.getParameter("exclude");
        if (val != null) {
            cq.withParam("exclude", Long.parseLong(val));
        }

        val = req.getParameter("template");
        if (val != null) {
            if (BooleanUtils.toBoolean(val)) {
                cq.withParam("template", true);
                val = req.getParameter("pageId");
                if (!StringUtils.isBlank(val)) {
                    List<Map<String, Object>> rows = select("selectAsmTParents", new RowBounds(0, 1), Long.parseLong(val));
                    Map<String, Object> prow = rows.isEmpty() ? null : rows.iterator().next();
                    String type = (prow != null) ? (String) prow.get("type") : null;
                    if ("news.page".equals(type)) {
                        cq.withParam("template_mode", "news");
                        String pname = (String) prow.get("pname");
                        if (pname != null) {
                            pname = pname.indexOf('_') > 0 ? pname.substring(0, pname.indexOf('_')) : pname;
                            cq.withParam("name_restriction", pname + "_%");
                        }
                    } else {
                        cq.withParam("template_mode", "page");
                    }
                }
                WSUser u = sctx.getWSUser(req);
                if (u != null && !u.isHasAnyRole("admin", "asmin.asm")) {
                    cq.withParam("roles", u.getRoleNames());
                }
            } else {
                cq.withParam("template", false);
            }
        }

        boolean orderUsed = false;
        val = req.getParameter("sortAsc");
        if (!StringUtils.isBlank(val)) {
            orderUsed = true;
            if ("icon".equals(val)) {
                cq.orderBy("asm.template").asc();
                cq.orderBy("asm.type").asc();
            } else {
                cq.orderBy("asm." + val).asc();
            }
        }
        val = req.getParameter("sortDesc");
        if (!StringUtils.isBlank(val)) {
            orderUsed = true;
            if ("icon".equals(val)) {
                cq.orderBy("asm.template").desc();
                cq.orderBy("asm.type").desc();
            } else {
                cq.orderBy("asm." + val).desc();
            }
        }
        if (!orderUsed) {
            cq.orderBy("asm.type").asc();
            cq.orderBy("asm.template").asc();
            cq.orderBy("asm.name").asc();
        }
        return cq;
    }
}
