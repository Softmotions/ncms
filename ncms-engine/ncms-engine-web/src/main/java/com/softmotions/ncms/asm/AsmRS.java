package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Редактирование выбранного экземпляра ассембли.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/asms")
@Produces("application/json")
public class AsmRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmRS.class);

    final AsmDAO adao;

    final ObjectMapper mapper;

    final NcmsMessages messages;

    final AsmAttributeManagersRegistry amRegistry;


    @Inject
    public AsmRS(SqlSession sess,
                 AsmDAO adao, ObjectMapper mapper,
                 AsmAttributeManagersRegistry amRegistry,
                 NcmsMessages messages) {
        super(AsmRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;
        this.amRegistry = amRegistry;
    }

    /**
     * Create new empty assembly instance
     */
    @PUT
    @Path("/new/{name}")
    public Asm newasm(@Context HttpServletRequest req,
                      @PathParam("name") String name) {
        name = name.trim();
        Asm asm;
        synchronized (Asm.class) {
            Long id = adao.asmSelectIdByName(name);
            if (id != null) {
                String msg = messages.get("ncms.asm.name.already.exists", req, name);
                throw new NcmsMessageException(msg, true);
            }
            asm = new Asm(name);
            adao.asmInsert(asm);
        }
        return asm;
    }

    @PUT
    @Path("/rename/{id}/{name}")
    public void rename(@Context HttpServletRequest req,
                       @PathParam("id") Long id,
                       @PathParam("name") String name) {
        name = name.trim();
        synchronized (Asm.class) {
            Long oid = adao.asmSelectIdByName(name);
            if (oid != null && !oid.equals(id)) {
                String msg = messages.get("ncms.asm.name.already.other", req, name);
                throw new NcmsMessageException(msg, true);
            }
            if (oid == null) {
                adao.asmRename(id, name);
            }
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        adao.asmRemove(id);
    }

    @PUT
    @Path("/{id}/core")
    public ObjectNode corePut(@PathParam("id") Long id, ObjectNode coreSpec) {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        ObjectNode res = mapper.createObjectNode();
        String locaton = coreSpec.get("location").asText();
        if (StringUtils.isBlank(locaton)) {
            if (asm.getCore() != null) {
                adao.coreDelete(asm.getCore().getId(), null);
                asm.setCore(null);
            }
        }
        AsmCore core = adao.selectOne("selectAsmCore", "location", locaton);
        if (core == null) {
            core = new AsmCore(locaton);
            adao.coreInsert(core);
        }
        adao.update("asmUpdateCore",
                    "id", id,
                    "coreId", core.getId());
        res.putPOJO("core", core);
        res.putPOJO("effectiveCore", core);
        return res;
    }

    @DELETE
    @Path("/{id}/core")
    public ObjectNode coreDelete(@PathParam("id") Long id) {
        ObjectNode res = mapper.createObjectNode();
        adao.update("asmUpdateCore",
                    "id", id,
                    "coreId", null);
        Asm asm = get(id);
        res.putPOJO("core", asm.getCore());
        res.putPOJO("effectiveCore", asm.getEffectiveCore());
        return res;
    }


    @GET
    @Path("/{id}")
    @JsonView(Asm.ViewFull.class)
    @Transactional
    public Asm get(@PathParam("id") Long id) {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        return asm;
    }


    @GET
    @Path("/basic/{name}")
    @JsonView(Asm.ViewFull.class)
    @Transactional
    public ObjectNode getBasic(@PathParam("name") String name) {
        ObjectNode res = mapper.createObjectNode();
        Asm asm = adao.asmSelectByName(name);
        if (asm == null) {
            throw new NotFoundException();
        }
        res.put("id", asm.getId());
        res.put("name", asm.getName());
        res.put("description", asm.getDescription());
        return res;
    }


    @DELETE
    @Path("/{id}/parents")
    @Transactional
    public String[] removeParent(@PathParam("id") Long id, JsonNode jsdata) throws IOException {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        if (!jsdata.isArray()) {
            throw new BadRequestException();
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
        return asm.getParentRefs();
    }


    @PUT
    @Path("/{id}/parents")
    @Transactional
    public String[] saveParent(@PathParam("id") Long id, JsonNode jsdata) throws IOException {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        if (!jsdata.isArray()) {
            throw new BadRequestException();
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
        return asm.getParentRefs();
    }


    @PUT
    @Path("/{id}/props")
    @Transactional
    public void updateAssemblyProps(@PathParam("id") Long asmId,
                                    ObjectNode props) {

        TinyParamMap args = new TinyParamMap();
        args.param("id", asmId);
        if (props.hasNonNull("description")) {
            args.param("description", props.get("description").asText());
        }
        if (props.hasNonNull("template")) {
            args.param("template", props.get("template").asBoolean() ? 1 : 0);
        }
        update("updateAssemblyProps", args);
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
            throw new NotFoundException();
        }
        return attr;
    }


    @DELETE
    @Path("/{id}/attribute/{name}")
    public void rmAsmAttribute(@PathParam("id") Long asmId,
                               @PathParam("name") String name) {
        delete("deleteAttribute",
               "name", name,
               "asm_id", asmId);
    }


    /**
     * Reorder assembly attributes.
     *
     * @param ordinalForm Assembly attribute ordinal
     * @param ordinalTo   Ordinal of another assembly attribute
     */
    @PUT
    @Path("/attributes/reorder/{ordinal1}/{ordinal2}")
    @Transactional
    public void exchangeAttributesOrdinal(@PathParam("ordinal1") Long ordinal1,
                                          @PathParam("ordinal2") Long ordinal2) {
        update("exchangeAttributesOrdinal",
               "ordinal1", ordinal1,
               "ordinal2", ordinal2);
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
    public void putAsmAttributes(@PathParam("id") Long asmId,
                                 ObjectNode spec) {


        String oldName = spec.hasNonNull("old_name") ? spec.get("old_name").asText() : null;
        String name = spec.get("name").asText();

        if (oldName != null && !oldName.equals(name)) { //attribute renamed
            update("renameAttribute",
                   "asm_id", asmId,
                   "new_name", name,
                   "old_name", oldName);
        }
        AsmAttribute attr = selectOne("selectAttrByName",
                                      "asm_id", asmId,
                                      "name", name);
        if (attr == null) {
            attr = new AsmAttribute();
            attr.setName(name);
        }
        if (spec.hasNonNull("type")) {
            attr.setType(spec.get("type").asText());
        }
        if (spec.hasNonNull("label")) {
            attr.setLabel(spec.get("label").asText());
        }
        if (spec.hasNonNull("required")) {
            attr.setRequired(spec.get("required").asBoolean());
        }
        AsmAttributeManager am = amRegistry.getByType(attr.getType());
        if (am != null) {
            if (spec.hasNonNull("options")) {
                attr = am.applyAttributeOptions(attr, spec.get("options"));
            }
            if (spec.hasNonNull("value")) {
                attr = am.applyAttributeValue(attr, spec.get("value"));
            }
        } else {
            log.warn("Missing atribute manager for given type: '" + attr.getType() + '\'');
        }
        attr.asmId = asmId;
        update("upsertAttribute", attr);
    }


    @GET
    @Path("select")
    @Produces("application/json")
    @Transactional
    public List<Map> select(@Context HttpServletRequest req) {
        return selectByCriteria(createQ(req).withStatement("select"));
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
        val = req.getParameter("type");
        if (val != null) {
            cq.withParam("type", val);
        }
        val = req.getParameter("exclude");
        if (val != null) {
            cq.withParam("exclude", Long.parseLong(val));
        }
        val = req.getParameter("template");
        if (BooleanUtils.toBoolean(val)) {
            cq.withParam("template", 1);
        }
        boolean orderUsed = false;
        val = req.getParameter("sortAsc");
        if (!StringUtils.isBlank(val)) {
            orderUsed = true;
            cq.orderBy("asm." + val).asc();
        }
        val = req.getParameter("sortDesc");
        if (!StringUtils.isBlank(val)) {
            orderUsed = true;
            cq.orderBy("asm." + val).desc();
        }
        if (!orderUsed) {
            cq.orderBy("asm.type").asc();
            cq.orderBy("asm.name").asc();
        }


        return cq;
    }
}
