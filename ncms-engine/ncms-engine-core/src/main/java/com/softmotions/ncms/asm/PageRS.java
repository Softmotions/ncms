package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.CollectionUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.guid.RandomGUID;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.commons.num.NumberUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.asm.events.AsmCreatedEvent;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.user.UserEnvRS;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.lifecycle.Start;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static com.softmotions.ncms.asm.CachedPage.PATH_TYPE;
import static com.softmotions.ncms.asm.PageSecurityService.UpdateMode.ADD;
import static com.softmotions.ncms.asm.PageSecurityService.UpdateMode.REMOVE;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Path("adm/pages")
@Produces("application/json;charset=UTF-8")
@Singleton
public class PageRS extends MBDAOSupport implements PageService {

    public static final int PAGE_STATUS_FOLDER_FLAG = 1;

    public static final int PAGE_STATUS_NOT_PUBLISHED_FLAG = 1 << 1;

    private static final Logger log = LoggerFactory.getLogger(PageRS.class);

    private static final Pattern GUID_REGEXP = Pattern.compile("^[0-9a-f]{32}$");

    private final AsmDAO adao;

    private final ObjectMapper mapper;

    private final NcmsMessages messages;

    private final WSUserDatabase userdb;

    private final AsmAttributeManagersRegistry amRegistry;

    private final PageSecurityService pageSecurity;

    private final NcmsEventBus ebus;

    private final UserEnvRS userEnvRS;

    private final MediaReader mediaReader;

    @GuardedBy("pagesCache")
    private final Map<Long, CachedPage> pagesCache;

    @GuardedBy("pagesCache")
    private final Map<String, CachedPage> pageGuid2Cache;

    @GuardedBy("pagesCache")
    private final Map<String, CachedPage> pageAlias2Cache;

    @GuardedBy("guid2AliasCache")
    private final Map<String, String> guid2AliasCache;

    private final Map<String, Long> lang2IndexPages;

    private final NcmsEnvironment env;

    private final Provider<AsmAttributeManagerContext> amCtxProvider;

    private final String asmRoot;

    @Inject
    public PageRS(SqlSession sess,
                  AsmDAO adao,
                  ObjectMapper mapper,
                  NcmsMessages messages,
                  WSUserDatabase userdb,
                  AsmAttributeManagersRegistry amRegistry,
                  PageSecurityService pageSecurity,
                  NcmsEventBus ebus,
                  UserEnvRS userEnvRS,
                  NcmsEnvironment env,
                  MediaReader mediaReader,
                  Provider<AsmAttributeManagerContext> amCtxProvider) {
        super(PageRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;
        this.userdb = userdb;
        this.amRegistry = amRegistry;
        this.pageSecurity = pageSecurity;
        this.ebus = ebus;
        this.userEnvRS = userEnvRS;
        this.pagesCache = new PagesLRUMap(env.xcfg().getInt("pages.lru-cache-size", 1024));
        this.guid2AliasCache = new LRUMap(env.xcfg().getInt("pages.lru-aliases-cache-size", 4096));
        this.pageGuid2Cache = new HashMap<>();
        this.pageAlias2Cache = new HashMap<>();
        this.lang2IndexPages = new HashMap<>();
        this.env = env;
        this.mediaReader = mediaReader;
        this.amCtxProvider = amCtxProvider;
        this.asmRoot = env.getNcmsRoot() + "/";
        this.ebus.register(this);
    }

    @GET
    @Path("/path/{id}")
    public ObjectNode selectPageLabelPath(@PathParam("id") Long id) {

        ObjectNode res = mapper.createObjectNode();
        CachedPage cp = getCachedPage(id, true);
        if (cp == null) {
            throw new NotFoundException("");
        }

        ArrayNode idPath = res.putArray("idPath");
        ArrayNode labelPath = res.putArray("labelPath");
        ArrayNode guidPath = res.putArray("guidPath");

        Map<PATH_TYPE, Object> navPaths = cp.fetchNavPaths();
        Long[] idPathArr = (Long[]) navPaths.get(PATH_TYPE.ID);
        String[] labelPathArr = (String[]) navPaths.get(PATH_TYPE.LABEL);
        String[] guidPathArr = (String[]) navPaths.get(PATH_TYPE.GUID);

        for (int i = 0; i < idPathArr.length; ++i) {
            idPath.add(idPathArr[i]);
            labelPath.add(labelPathArr[i]);
            guidPath.add(guidPathArr[i]);
        }
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
    public ObjectNode selectPageInfo(@Context HttpServletRequest req,
                                     @Context SecurityContext sctx,
                                     @PathParam("id") Long id) {
        Map<String, Object> row = selectOne("selectPageInfo", "id", id);
        if (row == null) {
            throw new NotFoundException("");
        }
        ObjectNode res = mapper.createObjectNode();
        JsonUtils.populateObjectNode(row, res);

        String username = (String) row.get("owner");
        WSUser user = (username != null) ? userdb.findUser(username) : null;
        if (user != null) {
            JsonUtils.populateObjectNode(user, res.putObject("owner"),
                                         "name", "fullName");
        } else {
            res.remove("owner");
        }

        username = (String) row.get("muser");
        user = (username != null) ? userdb.findUser(username) : null;
        if (user != null) {
            JsonUtils.populateObjectNode(user, res.putObject("muser"),
                                         "name", "fullName");
        } else {
            res.remove("muser");
        }

        res.put("accessMask", pageSecurity.getAccessRights(id, req));
        return res;
    }

    @GET
    @Path("/edit/{id}")
    public ObjectNode selectPageEdit(@Context HttpServletRequest req,
                                     @Context HttpServletResponse resp,
                                     @PathParam("id") Long id) throws Exception {
        ObjectNode res = mapper.createObjectNode();
        Asm page = adao.asmSelectById(id);
        if (page == null) {
            throw new NotFoundException("");
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
        res.put("published", page.isPublished());
        res.putPOJO("core", page.getEffectiveCore());
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
                    AsmAttributeManager am = amRegistry.getByType(a.getType());
                    if (am != null) {
                        a = am.prepareGUIAttribute(req, resp, page, template, tmplAttr, a);
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
    @Path("/publish/{id}")
    @Transactional
    public void publishPage(@Context HttpServletRequest req,
                            @PathParam("id") Long id) {
        updatePublishStatus(req, id, true);
    }

    @PUT
    @Path("/unpublish/{id}")
    @Transactional
    public void unpublishPage(@Context HttpServletRequest req,
                              @PathParam("id") Long id) {
        updatePublishStatus(req, id, false);
    }

    private void updatePublishStatus(HttpServletRequest req, Long id, boolean published) {
        Asm page = adao.asmSelectById(id);
        if (page == null || !pageSecurity.canEdit2(page, req)) {
            throw new ForbiddenException("Not authenticated");
        }
        update("updatePublishStatus",
               "id", id,
               "published", published);

        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, page.getId()));
    }

    @PUT
    @Path("/edit/{id}")
    @Transactional
    public void savePage(@Context HttpServletRequest req,
                         @Context SecurityContext sctx,
                         @PathParam("id") Long id,
                         ObjectNode data) throws Exception {

        AsmAttributeManagerContext amCtx = amCtxProvider.get();
        amCtx.setAsmId(id);

        Asm page = adao.asmSelectById(id);
        if (page == null) {
            throw new NotFoundException("");
        }
        if (!pageSecurity.canEdit2(page, req)) {
            throw new ForbiddenException("");
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
            am.applyAttributeValue(amCtx, attr, data.get(fname));
            update("upsertAttribute", attr);
            if (attr.getId() == null) {
                Number gid = selectOne("prevAttrID");
                if (gid != null) {
                    attr.setId(gid.longValue());
                }
            }
            am.attributePersisted(amCtx, attr, data.get(fname));
        }
        amCtx.flush();
        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, page.getId()));
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
                                  @Context HttpServletResponse resp,
                                  @PathParam("id") Long id,
                                  @PathParam("templateId") Long templateId) throws Exception {
        Asm page = adao.asmSelectById(id);
        if (!pageSecurity.canEdit2(page, req)) {
            throw new ForbiddenException("");
        }
        Integer ts = selectOne("selectPageTemplateStatus", templateId);
        if (ts == null || ts.intValue() == 0) {
            log.warn("Assembly: " + templateId + " is not page template");
            throw new BadRequestException("");
        }
        //adao.
        Collection<Long> aTemplates = pageSecurity.getAccessibleTemplates(req);
        if (!aTemplates.contains(templateId)) {
            log.warn("Template: " + templateId + " is not accesible for user");
            throw new ForbiddenException(messages.get("ncms.page.template.access.denied", req));
        }

        adao.asmRemoveAllParents(id);
        adao.asmSetParent(id, templateId);

        //Strore incompatible attribute names to clean-up
        List<String> attrsToRemove = new ArrayList<>();
        page = adao.asmSelectById(id);
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

        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, id));
        return selectPageEdit(req, resp, id);
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
    public JsonNode setPageOwner(@Context HttpServletRequest req,
                                 @PathParam("id") Long id,
                                 @PathParam("owner") String owner) {

        if (!pageSecurity.isOwner(id, req)) {
            throw new ForbiddenException("");
        }
        WSUser user = userdb.findUser(owner);
        if (user == null) {
            throw new NotFoundException("");
        }
        update("setPageOwner",
               "id", id,
               "owner", owner);

        ObjectNode res = mapper.createObjectNode();
        JsonUtils.populateObjectNode(user, res.putObject("owner"),
                                     "name", "fullName");

        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, id));
        return res;
    }

    @PUT
    @Path("/new")
    @Transactional
    public void newPage(@Context HttpServletRequest req,
                        ObjectNode spec) {

        String name = spec.hasNonNull("name") ? spec.get("name").asText().trim() : null;
        Long parent = spec.hasNonNull("parent") ? spec.get("parent").asLong() : null;
        String type = spec.hasNonNull("type") ? spec.get("type").asText().trim() : null;

        if (name == null ||
            !ArrayUtils.isAnyOf(type, "page.folder", "page", "news.page")) {
            throw new BadRequestException("");
        }
        if (parent == null && !req.isUserInRole("admin.structure")) {
            throw new ForbiddenException("");
        }
        if (parent != null &&
            ("news.page".equals(type) ?
             !pageSecurity.canNewsEdit(parent, req) :
             !pageSecurity.canEdit(parent, req))) {
            throw new ForbiddenException("");
        }

        String guid;
        Long id;
        do {
            guid = new RandomGUID().toString();
            id = adao.asmSelectIdByName(guid);
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

        id = adao.asmSelectIdByName(guid);
        ebus.fireOnSuccessCommit(new AsmCreatedEvent(this, id));
    }

    @PUT
    @Path("/update/basic")
    @Transactional
    public void updatePageBasic(@Context HttpServletRequest req,
                                @Context SecurityContext sctx,
                                ObjectNode spec) {
        String name = spec.hasNonNull("name") ? spec.get("name").asText().trim() : null;
        Long id = spec.hasNonNull("id") ? spec.get("id").asLong() : null;
        String type = spec.hasNonNull("type") ? spec.get("type").asText().trim() : null;
        if (id == null || name == null || type == null ||
            (!"page.folder".equals(type) && !"page".equals(type) && !"news.page".equals(type))) {
            throw new BadRequestException("");
        }

        if (!pageSecurity.canDelete(id, req)) {
            throw new ForbiddenException("");
        }

        if ("page".equals(type)) {
            if (count("selectNumberOfDirectChilds", id) > 0) {
                type = "page.folder";
            }
        }

        update("updatePageBasic",
               "id", id,
               "hname", name,
               "description", name,
               "type", type,
               "muser", pageSecurity.getCurrentWSUserSafe(sctx).getName());

        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, id));
    }

    @PUT
    @Path("/move")
    @Transactional
    public void movePage(@Context HttpServletRequest req,
                         ObjectNode spec) {
        long src = spec.hasNonNull("src") ? spec.get("src").longValue() : 0;
        long tgt = spec.hasNonNull("tgt") ? spec.get("tgt").longValue() : 0;
        if (src == 0) {
            throw new BadRequestException("");
        }
        Asm srcPage = adao.asmSelectById(src);
        Asm tgtPage = (tgt != 0) ? adao.asmSelectById(tgt) : null; //zero tgt => Root target
        if (srcPage == null) {
            throw new NotFoundException("");
        }
        if (tgtPage != null && !"page.folder".equals(tgtPage.getType())) {
            throw new BadRequestException("");
        }
        if (src == tgt) {
            String msg = messages.get("ncms.mmgr.folder.cantMoveIntoSelf", req, srcPage.getHname());
            throw new NcmsMessageException(msg, true);
        }
        if (tgtPage != null && "page.folder".equals(srcPage.getType())) {
            String srcPath = getPageIDsPath(src);
            String tgtPath = getPageIDsPath(tgt);
            if (tgtPath.startsWith(srcPath)) {
                String msg = messages.get("ncms.mmgr.folder.cantMoveIntoSubfolder", req,
                                          srcPage.getHname(), tgtPage.getHname());
                throw new NcmsMessageException(msg, true);
            }
        }

        //check user access
        if (
                (tgt == 0 && !req.isUserInRole("admin.structure")) ||
                !pageSecurity.checkAccess(tgt, req, 'w') ||
                !pageSecurity.checkAccess(src, req, 'd')) {
            throw new ForbiddenException("");
        }

        update("movePage",
               "id", src,
               "nav_cached_path", getPageIDsPath(tgt != 0 ? tgt : null),
               "nav_parent_id", (tgt != 0) ? tgt : null);

        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, srcPage.getId()));
    }


    @GET
    @Path("/referers/{guid}")
    public Response getPageReferers(@PathParam("guid") String guid,
                                    @Context HttpServletRequest req) {
        return Response.ok((StreamingOutput) o -> {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(o, "UTF-8"));
            pw.println("<!DOCTYPE html>");
            pw.println("<html>");
            pw.println("<body>");
            CachedPage cp = getCachedPage(guid, true);
            if (cp != null) {
                pw.print("<h2>");
                pw.print("<a href='" + (asmRoot + cp.getName()) + "'>");
                pw.print(messages.get("ncms.page.nodel.refs.list", req, cp.getHname()));
                pw.print("</a></h2>");
            }
            pw.println("<ol>");
            select("selectPagesDependentOn", context -> {
                Map<String, Object> row = (Map<String, Object>) context.getResultObject();
                String pguid = (String) row.get("guid");
                String name = (String) row.get("name");
                int published = NumberUtils.number2Int((Number) row.get("published"), 1);
                pw.println("<li><a href='" + (asmRoot + pguid) + "'>" + name + "</a> " +
                           (published == 0 ? "(not published)</li>" : "</li>"));
            }, guid);
            pw.println("</ol>");
            pw.println("</body>");
            pw.println("</html>");
            pw.flush();
        }).type("text/html;charset=UTF-8")
                .build();
    }


    @DELETE
    @Path("/{id}")
    @Transactional
    public ObjectNode dropPage(@Context HttpServletRequest req,
                               @PathParam("id") Long id) {

        ObjectNode ret = mapper.createObjectNode();
        Asm page = adao.asmSelectById(id);
        if (!pageSecurity.checkAccessAll2(page, req, "d")) {
            throw new ForbiddenException("");
        }
        if (count("selectCountOfDependentAttrs", page.getName()) > 0) {
            ret.put("error", "ncms.page.nodel.refs.found");
            return ret;
        }
        delete("dropPage", "id", id);
        ebus.fireOnSuccessCommit(new AsmRemovedEvent(this, id));
        return ret;
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

        return Response.ok((StreamingOutput) output -> {
            final boolean includePath = BooleanUtils.toBoolean(req.getParameter("includePath"));
            final JsonGenerator gen = new JsonFactory().createGenerator(output);
            gen.writeStartArray();
            Map q = createSelectLayerQ(path, req);
            q.put("user", req.getRemoteUser());
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
                                status |= PAGE_STATUS_FOLDER_FLAG;
                            }
                            if (!NumberUtils.number2Boolean((Number) row.get("published"))) { //page not published
                                status |= PAGE_STATUS_NOT_PUBLISHED_FLAG;
                            }
                            gen.writeNumberField("status", status);
                            gen.writeStringField("type", type);
                            gen.writeStringField("options", (String) row.get("options"));

                            String am;
                            if (req.isUserInRole("admin.structure") || req.getRemoteUser().equals(row.get("owner"))) {
                                am = pageSecurity.getAllRights();
                            } else {
                                am = pageSecurity.mergeRights((String) row.get("local_rights"), (String) row.get("recursive_rights"));
                            }
                            gen.writeStringField("accessMask", am);
                            if (includePath) {
                                String[] path1 = convertPageIDPath2LabelPath((String) row.get("nav_cached_path"));
                                gen.writeStringField("path",
                                                     (path1.length > 0 ? "/" + ArrayUtils.stringJoin(path1, "/") : "") +
                                                     "/" + row.get("hname"));
                            }
                            gen.writeEndObject();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, q);
            } finally {
                try {
                    gen.writeEndArray();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            gen.flush();
        }).type("application/json;charset=UTF-8")
                .build();
    }

    @GET
    @Path("/acl/{pid}")
    public JsonNode getAcl(@PathParam("pid") Long pid,
                           @QueryParam("recursive") Boolean recursive) {
        ArrayNode res = mapper.createArrayNode();

        for (PageSecurityService.AclEntity aclEntity : pageSecurity.getAcl(pid, recursive)) {
            res.addPOJO(aclEntity);
        }

        return res;
    }

    @PUT
    @Path("/acl/{pid}/{user}")
    public void addToAcl(@Context HttpServletRequest req,
                         @PathParam("pid") Long pid,
                         @PathParam("user") String user,
                         @QueryParam("recursive") boolean recursive) {

        if (!pageSecurity.isOwner(pid, req)) {
            throw new ForbiddenException("");
        }
        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        pageSecurity.addUserRights(pid, user, recursive);
    }

    @POST
    @Path("/acl/{pid}/{user}")
    public void updateAcl(@Context HttpServletRequest req,
                          @PathParam("pid") Long pid,
                          @PathParam("user") String user,
                          @QueryParam("recursive") boolean recursive,
                          @QueryParam("rights") String rights,
                          @QueryParam("add") boolean isAdd) {
        if (!pageSecurity.isOwner(pid, req)) {
            throw new ForbiddenException("");
        }

        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            throw new BadRequestException("User not found");
        }

        pageSecurity.updateUserRights(pid, user, rights, isAdd ? ADD : REMOVE, recursive);
    }

    @DELETE
    @Path("/acl/{pid}/{user}")
    public void deleteFromAcl(@Context HttpServletRequest req,
                              @PathParam("pid") Long pid,
                              @PathParam("user") String user,
                              @QueryParam("recursive") boolean recursive,
                              @QueryParam("forceRecursive") boolean force) {

        if (!pageSecurity.isOwner(pid, req)) {
            throw new ForbiddenException("");
        }
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

    @GET
    @Path("search/count")
    public Number searchPageCount(@Context HttpServletRequest req) {
        MBCriteriaQuery cq = createSearchQ(req, true);
        return selectOne(cq.getStatement(), cq);
    }

    @GET
    @Path("search")
    @Transactional
    public Response searchPage(@Context final HttpServletRequest req) {
        return Response.ok((StreamingOutput) output -> {
            final boolean includePath = BooleanUtils.toBoolean(req.getParameter("includePath"));
            final JsonGenerator gen = new JsonFactory().createGenerator(output);
            try {
                MBCriteriaQuery cq = createSearchQ(req, false);
                gen.writeStartArray();
                //noinspection InnerClassTooDeeplyNested
                select(cq.getStatement(), context -> {
                    Map<String, ?> row = (Map<String, ?>) context.getResultObject();
                    try {
                        boolean published = NumberUtils.number2Boolean((Number) row.get("published"));

                        gen.writeStartObject();
                        gen.writeStringField("icon", published ? "" : "ncms/icon/16/misc/exclamation.png");
                        gen.writeNumberField("id", NumberUtils.number2Long((Number) row.get("id"), 0));
                        gen.writeStringField("label", (String) row.get("hname"));
                        String am;
                        if (req.isUserInRole("admin.structure") || req.getRemoteUser().equals(row.get("owner"))) {
                            am = pageSecurity.getAllRights();
                        } else {
                            am = pageSecurity.mergeRights((String) row.get("local_rights"), (String) row.get("recursive_rights"));
                        }
                        gen.writeStringField("accessMask", am);
                        if (includePath) {
                            String[] path = convertPageIDPath2LabelPath((String) row.get("nav_cached_path"));
                            if (path.length > 0) {
                                gen.writeStringField("path",
                                                     ArrayUtils.stringJoin(path, "/") + "/" + row.get("hname"));
                            } else {
                                gen.writeStringField("path",
                                                     (String) row.get("hname"));
                            }
                        }
                        gen.writeBooleanField("published", published);
                        gen.writeStringField("type", (String) row.get("type"));

                        gen.writeEndObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, cq);
            } finally {
                try {
                    gen.writeEndArray();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            gen.flush();
        }).type("application/json;charset=UTF-8").build();
    }

    @GET
    @Path("rights/{pid}/{rights}")
    @Transactional
    public boolean checkAccess(@Context HttpServletRequest req,
                               @PathParam("pid") Long pid,
                               @PathParam("rights") String rights) {
        return pageSecurity.checkAccessAll(pid, req, rights);
    }

    @GET
    @Path("rights/{pid}")
    @Produces("text/plain")
    @Transactional
    public String getAccessRights(@Context HttpServletRequest req,
                                  @PathParam("pid") Long pid) {
        return pageSecurity.getAccessRights(pid, req);
    }

    @PUT
    @Path("collection/{collection}/{id}")
    public void putPageIntoUserCollection(@Context HttpServletRequest req,
                                          @PathParam("collection") String collection,
                                          @PathParam("id") Long id) {

        userEnvRS.addSet(req, collection, id.toString());
    }

    @DELETE
    @Path("collection/{collection}/{id}")
    public void delPageFromUserCollection(@Context HttpServletRequest req,
                                          @PathParam("collection") String collection,
                                          @PathParam("id") Long id) {
        userEnvRS.delSet(req, collection, id.toString());
    }


    @PUT
    @Path("single/{collection}/{id}")
    public ObjectNode putSinglePageIntoUserCollection(@Context HttpServletRequest req,
                                                      @Context SecurityContext sctx,
                                                      @PathParam("collection") String collection,
                                                      @PathParam("id") Long id) {
        userEnvRS.ensureSingle(req, collection, id.toString());
        ObjectNode info = selectPageInfo(req, sctx, id);
        info.setAll(selectPageLabelPath(id));
        return info;
    }

    @GET
    @Path("single/{collection}")
    public ObjectNode getSinglePageIntoUserCollection(@Context HttpServletRequest req,
                                                      @Context SecurityContext sctx,
                                                      @PathParam("collection") String collection) {
        Collection set = userEnvRS.getSet(req, collection);
        if (set.isEmpty() || !(set.iterator().next() instanceof Number)) {
            return mapper.createObjectNode(); //empty object
        }
        Long id = ((Number) set.iterator().next()).longValue();
        ObjectNode info = selectPageInfo(req, sctx, id);
        info.setAll(selectPageLabelPath(id));
        return info;
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

    private String[] convertPageIDPath2LabelPath(String idpath) {
        if (idpath == null || "/".equals(idpath)) {
            return org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
        }
        StringTokenizer st = new StringTokenizer(idpath, "/");
        int ct = st.countTokens();
        if (ct == 0) {
            return org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
        }
        Long[] ids = new Long[ct];
        String[] labels = new String[ct];
        boolean hasNotCached = false;
        for (int i = 0, l = ids.length; i < l; ++i) {
            ids[i] = Long.parseLong(st.nextToken());
            CachedPage cp = getCachedPage(ids[i], false);
            if (cp != null) {
                labels[i] = cp.getHname();
                ids[i] = -1L;
            } else {
                hasNotCached = true;
            }
        }
        if (!hasNotCached) {
            return labels;
        }
        Map<Number, Map> rows = selectMap("selectPageInfoIN", "id",
                                          "ids", ids);
        for (int i = 0, l = labels.length; i < l; ++i) {
            if (labels[i] == null) {
                Map row = rows.get(ids[i]);
                if (row != null) {
                    labels[i] = (String) row.get("name");
                }
            }
        }
        for (String l : labels) {
            if (l == null) {
                return Arrays.stream(labels).filter(s -> s != null).toArray(String[]::new);
            }
        }
        return labels;
    }

    private MBCriteriaQuery createSearchQ(HttpServletRequest req, boolean count) {
        MBCriteriaQuery cq = createCriteria();
        String val;
        if (!count) {
            val = req.getParameter("firstRow");
            if (val != null) {
                Integer frow = Integer.valueOf(val);
                cq.offset(frow);
                val = req.getParameter("lastRow");
                if (val != null) {
                    Integer lrow = Integer.valueOf(val);
                    cq.limit(Math.abs(frow - lrow) + 1);
                }
            }
        }
        val = req.getParameter("name");
        if (!StringUtils.isBlank(val)) {
            cq.withParam("name", val + "%");
        }
        String type = "page%";
        val = req.getParameter("type");
        if (!StringUtils.isBlank(val)) {
            type = val;
        } else if (BooleanUtils.toBoolean(req.getParameter("foldersOnly"))) {
            type = "page.folder";
        }
        val = req.getParameter("parentId");
        if (!StringUtils.isBlank(val)) {
            cq.withParam("parentId", Long.parseLong(val));
        }
        cq.withParam("type", type);
        cq.withParam("user", req.getRemoteUser());

        val = req.getParameter("collection");
        if (!StringUtils.isBlank(val)) {
            cq.withParam("collection", val);
        }
        if (!count) {
            val = req.getParameter("sortAsc");
            if (!StringUtils.isBlank(val)) {
                if ("label".equals(val)) {
                    val = "hname";
                }
                cq.orderBy("p." + val).asc();
            }
            val = req.getParameter("sortDesc");
            if (!StringUtils.isBlank(val)) {
                if ("label".equals(val)) {
                    val = "hname";
                }
                cq.orderBy("p." + val).desc();
            }
        }
        if (cq.getStatement() == null) {
            cq.withStatement(count ? "searchPageCount" : "searchPage");
        }
        return cq.finish();
    }

    private Long getPathLastIdSegment(String path) {
        if (path == null) {
            return null;
        }
        int idx = path.lastIndexOf('/');
        if (idx == -1 || idx == path.length() - 1) {
            return Long.valueOf(path);
        }
        return Long.valueOf(path.substring(idx + 1));
    }


    private Map createSelectLayerQ(String path, HttpServletRequest req) {
        Long pId = getPathLastIdSegment(path);
        Map<String, Object> ret = new TinyParamMap();
        if (pId != null) {
            ret.put("nav_parent_id", pId);
        }
        if (BooleanUtils.toBoolean(req.getParameter("foldersOnly"))) {
            ret.put("page_type", "page.folder");
        } else {
            ret.put("page_type", "page%");
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    //                     PageService implementation                        //
    ///////////////////////////////////////////////////////////////////////////

    private final class PagesLRUMap extends LRUMap {

        private PagesLRUMap(int maxSize) {
            super(maxSize, true);
        }

        protected boolean removeLRU(LinkEntry entry) {
            CachedPage cp = (CachedPage) entry.getValue();
            Long pid = cp.getId();
            synchronized (lang2IndexPages) {
                for (Long ipid : lang2IndexPages.values()) {
                    if (ipid.equals(pid)) {  //this is the index page, keep it in the cache!
                        return false;
                    }
                }
            }
            synchronized (pagesCache) {
                if (cp.getName() != null) {
                    pageGuid2Cache.remove(cp.getName());
                }
                if (cp.getAlias() != null) {
                    pageAlias2Cache.remove(cp.getAlias());
                }
            }
            return true;
        }
    }

    private final class CachedPageImpl implements CachedPage {

        private final Asm asm;

        public Asm getAsm() {
            return asm;
        }

        public Long getId() {
            return asm.getId();
        }

        public String getAlias() {
            return (asm.getNavAlias() != null ? asm.getNavAlias() : asm.getNavAlias2());
        }

        public String getName() {
            return asm.getName();
        }

        public String getHname() {
            return asm.getHname();
        }

        public boolean isPublished() {
            return asm.isPublished();
        }

        public Long getNavParentId() {
            return asm.getNavParentId();
        }

        public Map<PATH_TYPE, Object> fetchNavPaths() {
            String cpath = asm.getNavCachedPath();
            Map<PATH_TYPE, Object> res = new EnumMap<>(PATH_TYPE.class);
            cpath = (cpath != null) ? StringUtils.strip(cpath, "/") : null;
            if (StringUtils.isBlank(cpath)) {
                res.put(PATH_TYPE.GUID, new String[]{asm.getName()});
                res.put(PATH_TYPE.LABEL, new String[]{asm.getHname()});
                res.put(PATH_TYPE.ID, new Long[]{asm.getId()});
                return res;
            }
            @SuppressWarnings("ConstantConditions")
            String[] idsArr = cpath.split("/");
            String[] guidPath = new String[idsArr.length + 1];
            String[] labelPath = new String[idsArr.length + 1];
            Long[] idPath = new Long[idsArr.length + 1];
            int i;
            for (i = 0; i < idsArr.length; ++i) {
                Long pid = Long.valueOf(idsArr[i]);
                CachedPage cp = getCachedPage(pid, true);
                if (cp == null) {
                    guidPath[i] = null;
                    labelPath[i] = null;
                    idPath[i] = null;
                } else {
                    guidPath[i] = cp.getName();
                    labelPath[i] = cp.getHname();
                    idPath[i] = cp.getId();
                }
            }
            guidPath[i] = asm.getName();
            labelPath[i] = asm.getHname();
            idPath[i] = asm.getId();

            res.put(PATH_TYPE.GUID, guidPath);
            res.put(PATH_TYPE.LABEL, labelPath);
            res.put(PATH_TYPE.ID, idPath);

            return res;
        }

        private CachedPageImpl(Asm asm) {
            this.asm = asm;
        }
    }

    @Subscribe
    public void onAsmRemoved(AsmRemovedEvent ev) {
        Long pid = ev.getId();
        clearCachedPage(pid);
        boolean isIndex = false;
        synchronized (lang2IndexPages) {
            String[] lngs = lang2IndexPages.keySet().toArray(new String[lang2IndexPages.size()]);
            for (final String l : lngs) {
                if (pid.equals(lang2IndexPages.get(l))) {
                    isIndex = true;
                    break;
                }
            }
        }
        if (isIndex) {
            reloadIndexPages();
        }
    }

    @Subscribe
    public void onAsmModified(AsmModifiedEvent ev) {
        Long pid = ev.getId();
        clearCachedPageAlias(pid);
        long cc = adao.asmChildrenCount(pid);
        if (cc == 0) {
            clearCachedPage(pid);
        } else {
            clearCache();
        }
    }

    private void clearCachedPage(Long id) {
        synchronized (pagesCache) {
            CachedPage p = pagesCache.remove(id);
            if (p != null) {
                if (p.getName() != null) {
                    pageGuid2Cache.remove(p.getName());
                }
                if (p.getAlias() != null) {
                    pageAlias2Cache.remove(p.getAlias());
                }
            }
        }
    }

    private void clearCache() {
        synchronized (pagesCache) {
            pagesCache.clear();
            pageGuid2Cache.clear();
            pageAlias2Cache.clear();
        }
    }

    public CachedPage getCachedPage(Long id, boolean create) {
        CachedPage cp;
        synchronized (pagesCache) {
            cp = pagesCache.get(id);
        }
        if (cp == null) {
            if (!create) {
                return null;
            }
            Asm asm = adao.asmSelectById(id);
            if (asm == null) {
                return null;
            }
            cp = new CachedPageImpl(asm);
        }
        synchronized (pagesCache) {
            CachedPage cp2 = pagesCache.get(id);
            if (cp2 == null) {
                pagesCache.put(id, cp);
                if (cp.getName() != null) {
                    pageGuid2Cache.put(cp.getName(), cp);
                }
                if (cp.getAlias() != null) {
                    pageAlias2Cache.put(cp.getAlias(), cp);
                }
            } else {
                cp = cp2;
            }
        }
        return cp;
    }

    public CachedPage getCachedPage(String guidOrAlias, boolean create) {
        CachedPage cp;
        synchronized (pagesCache) {
            cp = pageGuid2Cache.get(guidOrAlias);
            if (cp == null) {
                cp = pageAlias2Cache.get(guidOrAlias);
            }
        }
        if (cp == null) {
            if (create) {
                Long id;
                if (GUID_REGEXP.matcher(guidOrAlias).matches()) {
                    id = adao.asmSelectIdByName(guidOrAlias);
                } else {
                    id = adao.asmSelectIdByAlias(guidOrAlias);
                }
                if (id != null) {
                    cp = getCachedPage(id, true);
                }
            }
        }
        return cp;
    }

    public String resolvePageAlias(String guid) {
        String alias;
        synchronized (guid2AliasCache) {
            alias = guid2AliasCache.get(guid);
        }
        if (alias != null) {
            return ("@".equals(alias) ? null : alias);
        }
        alias = adao.asmSelectAliasByGuid(guid);
        if (alias == null) {
            alias = "@";
        }
        synchronized (guid2AliasCache) {
            guid2AliasCache.put(guid, alias);
        }
        return ("@".equals(alias) ? null : alias);
    }


    public String resolvePageLink(Long id) {
        if (id == null) {
            return null;
        }
        //todo use alias
        return asmRoot + id;
    }

    public String resolvePageLink(String guidOrAlias) {
        if (guidOrAlias == null) {
            return null;
        }
        String guid = GUID_REGEXP.matcher(guidOrAlias).matches() ? guidOrAlias : null;
        String alias = (guid == null) ? guidOrAlias : null;
        if (guid != null) {
            alias = resolvePageAlias(guid);
        }
        return asmRoot + (alias != null ? alias : guid);
    }

    public String resolvePageGuid(String spec) {
        if (spec == null) {
            return null;
        }
        spec = spec.toLowerCase();
        if (spec.startsWith("page:")) { //Page reference
            int plen = "page:".length();
            spec = (spec.length() > plen && spec.charAt(plen) == '/') ? spec.substring(plen + 1) : spec.substring(plen);
            int ind = spec.indexOf('|');
            if (ind != -1) {
                spec = spec.substring(0, ind).trim();
            }
        } else if (spec.indexOf(asmRoot) == 0) {
            spec = spec.substring(asmRoot.length());
        }
        if (!spec.isEmpty() && spec.charAt(0) == '/') {
            spec = spec.substring(1);
        }
        if (GUID_REGEXP.matcher(spec).matches()) {
            return spec;
        } else {
            return null;
        }
    }

    public String resolveResourceLink(String spec) {
        if (spec == null) {
            return null;
        }
        if (spec.contains("://")) {
            return spec;
        }
        Long fid = mediaReader.getFileIdByResourceSpec(spec);
        if (fid != null) {
            return mediaReader.resolveFileLink(fid, true);
        }
        spec = spec.toLowerCase();
        if (spec.startsWith("page:")) { //Page reference
            int plen = "page:".length();
            spec = (spec.length() > plen && spec.charAt(plen) == '/') ? spec.substring(plen + 1) : spec.substring(plen);
            int ind = spec.indexOf('|');
            if (ind != -1) {
                spec = spec.substring(0, ind).trim();
            }
        } else if (spec.indexOf("/ncms/asm/") == 0) { //it is the legacy case todo remove it
            spec = spec.substring("/ncms/asm/".length());
        } else if (spec.indexOf(asmRoot) == 0) {
            spec = spec.substring(asmRoot.length());
        }
        return resolvePageLink(spec);
    }

    private void clearCachedPageAlias(Long pid) {
        CachedPage cp = getCachedPage(pid, false);
        if (cp != null) {
            synchronized (guid2AliasCache) {
                guid2AliasCache.remove(cp.getName());
            }
            return;
        }
        String guid = adao.asmSelectNameById(pid);
        if (guid != null) {
            synchronized (guid2AliasCache) {
                guid2AliasCache.remove(guid);
            }
        }
    }

    public CachedPage getIndexPage(HttpServletRequest req) {
        Locale locale = messages.getLocale(req);
        Long pid;
        synchronized (lang2IndexPages) {
            pid = lang2IndexPages.get(locale.getLanguage());
            if (pid == null) {
                if (!locale.equals(Locale.getDefault())) {
                    pid = lang2IndexPages.get(Locale.getDefault().getLanguage());
                }
                if (pid == null) {
                    pid = lang2IndexPages.get("*");
                }
            }
        }
        if (pid == null) {
            return null;
        }
        CachedPage p = getCachedPage(pid, true);
        if (p == null) {
            synchronized (lang2IndexPages) {
                String[] lngs = lang2IndexPages.keySet().toArray(new String[lang2IndexPages.size()]);
                for (final String l : lngs) {
                    Long id = lang2IndexPages.get(l);
                    if (pid.equals(id)) {
                        lang2IndexPages.remove(l);
                    }
                }
            }
        }
        return p;
    }

    @Transactional
    public void reloadIndexPages() {
        List<Map<String, Object>> ipages =
                select("selectAttrOptions",
                       "attrType", "mainpage",
                       "pageType", "page%");
        synchronized (lang2IndexPages) {
            lang2IndexPages.clear();
            for (Map row : ipages) {
                Long id = NumberUtils.number2Long((Number) row.get("id"), -1L);
                if (id.longValue() == -1L) {
                    continue;
                }
                CachedPage cp = getCachedPage(id, true);
                if (cp == null) {
                    continue;
                }
                String lp = ArrayUtils.stringJoin(cp.<String[]>fetchNavPaths().get(PATH_TYPE.LABEL), "/");
                KVOptions options = new KVOptions();
                options.loadOptions((String) row.get("options"));
                if (!"true".equals(options.get("enabled"))) {
                    continue;
                }
                String langs = (String) options.get("lang");
                String[] lcodes = ArrayUtils.split(langs, " ,;");
                for (String lang : lcodes) {
                    log.info("Registering page: '" + lp + "' as the MAIN PAGE for lang: " + lang);
                    lang2IndexPages.put(lang, id);
                }
                if (!lang2IndexPages.containsKey("*")) {
                    String dpl = this.env.xcfg().getString("pages.default-page-language", Locale.getDefault().getLanguage());
                    if (lang2IndexPages.containsKey(dpl)) {
                        log.info("Registering page: '" + lp + "' as the MAIN PAGE for lang: *");
                        lang2IndexPages.put("*", lang2IndexPages.get(dpl));
                    }
                }
            }
            if (lang2IndexPages.isEmpty()) {
                log.warn("No main pages found!");
            }
        }
    }


    @Start(order = 90, parallel = true)
    public void start() {
        reloadIndexPages();
    }

}
