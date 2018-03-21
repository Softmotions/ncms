package com.softmotions.ncms.asm;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.shiro.authz.UnauthorizedException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.softmotions.ncms.asm.CachedPage.PATH_TYPE;
import static com.softmotions.ncms.asm.PageSecurityService.UpdateMode.ADD;
import static com.softmotions.ncms.asm.PageSecurityService.UpdateMode.REMOVE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

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
import com.softmotions.commons.Converters;
import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.CollectionUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.guid.RandomGUID;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.commons.num.NumberUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.asm.events.AsmCreatedEvent;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.asm.render.AsmAttributesHandler;
import com.softmotions.ncms.asm.render.AsmRendererHelper;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.ncms.jaxrs.NcmsNotificationException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.user.UserEnvRS;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.i18n.I18n;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;
import com.softmotions.weboot.scheduler.Scheduled;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Path("adm/pages")
@Produces("application/json;charset=UTF-8")
@Singleton
@SuppressWarnings("unchecked")
public class PageRS extends MBDAOSupport implements PageService {

    public static final String INDEX_PAGE_REQUEST_ATTR_NAME = "_PAGERS_INDEX_PAGE";

    /**
     * Page is folder
     *
     * @warning this constant hardcoded in qooxdoo admin UI
     */
    public static final int PAGE_STATUS_FOLDER_FLAG = 1;

    /**
     * Page is published
     *
     * @warning this constant hardcoded in qooxdoo admin UI
     */
    public static final int PAGE_STATUS_NOT_PUBLISHED_FLAG = 1 << 1;

    /**
     * Page has parents (inheritance).
     *
     * @warning this constant hardcoded in qooxdoo admin UI
     */
    public static final int PAGE_STATUS_HAS_PARENTS = 1 << 2;

    private static final Logger log = LoggerFactory.getLogger(PageRS.class);

    private static final Pattern GUID_REGEXP = Pattern.compile("^[0-9a-f]{32}$");

    private final AsmDAO adao;

    private final ObjectMapper mapper;

    private final I18n i18n;

    private final WSUserDatabase userdb;

    private final Provider<AsmAttributeManagersRegistry> amRegistry;

    private final PageSecurityService pageSecurity;

    private final NcmsEventBus ebus;

    private final UserEnvRS userEnvRS;

    private final MediaRepository mrepo;

    //
    // Cached pages
    //

    @GuardedBy("pagesCache")
    private final Map<Long, CachedPage> pagesCache;

    @GuardedBy("pagesCache")
    private final Map<String, CachedPage> pageGuid2Cache;

    @GuardedBy("pagesCache")
    private final Map<String, CachedPage> pageAlias2Cache;

    @GuardedBy("guid2AliasCache")
    private final Map<String, String> guid2AliasCache;

    //
    // Index pages caches
    //

    /**
     * Map:  langCode => virtualHost => pageId
     * Star `*` included in this collection
     */
    @GuardedBy("lvh2IndexPages")
    private final Map<String, Map<String, Long>> lvh2IndexPages;

    /**
     * Map: pageId => langCode (First page language)
     * Star `*` lang is used as `null` in this collection
     */
    @GuardedBy("lvh2IndexPages")
    private final Map<Long, String> indexPage2FirstLang;

    /**
     * Map: pageId => langCode (Second page language)
     */
    @GuardedBy("lvh2IndexPages")
    private final Map<Long, String> indexPage2SecondLang;

    /**
     * Map: pageId => IndexPageSlot
     */
    @GuardedBy("lvh2IndexPages")
    private final Map<Long, IndexPageSlot> indexPage2Slot;

    // EOF index pages caches

    private final Provider<AsmAttributeManagerContext> amCtxProvider;

    private final String asmRoot;

    private final AsmRendererHelper helper;

    private final NcmsEnvironment env;

    @Inject
    public PageRS(SqlSession sess,
                  AsmDAO adao,
                  ObjectMapper mapper,
                  I18n i18n,
                  WSUserDatabase userdb,
                  PageSecurityService pageSecurity,
                  NcmsEventBus ebus,
                  UserEnvRS userEnvRS,
                  NcmsEnvironment env,
                  MediaRepository mrepo,
                  Provider<AsmAttributeManagersRegistry> amRegistry,
                  Provider<AsmAttributeManagerContext> amCtxProvider,
                  AsmRendererHelper helper) {
        super(PageRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.i18n = i18n;
        this.userdb = userdb;
        this.amRegistry = amRegistry;
        this.pageSecurity = pageSecurity;
        this.ebus = ebus;
        this.userEnvRS = userEnvRS;
        this.pagesCache = new PagesLRUMap<>(env.xcfg().getInt("pages.lru-cache-size", 1024));
        this.guid2AliasCache = new LRUMap<>(env.xcfg().getInt("pages.lru-aliases-cache-size", 8192));
        this.pageGuid2Cache = new HashMap<>();
        this.pageAlias2Cache = new HashMap<>();
        this.indexPage2Slot = new HashMap<>();
        this.lvh2IndexPages = new HashMap<>();
        this.indexPage2FirstLang = new HashMap<>();
        this.indexPage2SecondLang = new HashMap<>();
        this.mrepo = mrepo;
        this.amCtxProvider = amCtxProvider;
        this.env = env;
        this.asmRoot = env.getAppRoot() + "/";
        this.helper = helper;
        this.ebus.register(this);
    }


    @Override
    @Nonnull
    public PageSecurityService getPageSecurityService() {
        return pageSecurity;
    }

    @Override
    @Nonnull
    public AsmAttributeManagersRegistry getAsmAttributeManagersRegistry() {
        return amRegistry.get();
    }

    @GET
    @Path("/path/{id}")
    public ObjectNode selectPageLabelPath(@PathParam("id") Long id) {

        ObjectNode res = mapper.createObjectNode();
        CachedPage cp = getCachedPage(id, true);
        if (cp == null) {
            throw new NotFoundException();
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
            throw new NotFoundException();
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
            res.putObject("muser")
               .put("name", StringEscapeUtils.escapeHtml4(user.getName()))
               .put("fullName", StringEscapeUtils.escapeHtml4(user.getFullName()))
               .put("email", StringEscapeUtils.escapeHtml4(user.getEmail()));
        } else {
            res.remove("muser");
        }
        res.put("accessMask", pageSecurity.getAccessRights(id, req));
        IndexPageSlot ips;
        synchronized (lvh2IndexPages) {
            ips = indexPage2Slot.get(id);
        }
        if (ips != null) { // Page is an index page
            ObjectNode ipo = res.putObject("indexPage");
            ArrayNode virtualHosts = ipo.putArray("virtualHosts");
            ArrayNode langCodes = ipo.putArray("langCodes");
            for (String v : ips.virtualHosts) {
                virtualHosts.add(StringEscapeUtils.escapeHtml4(v));
            }
            for (String v : ips.langCodes) {
                langCodes.add(StringEscapeUtils.escapeHtml4(v));
            }
        }
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
            throw new NotFoundException();
        }
        Asm firstParent = null;
        Asm template = null;
        Iterator<Asm> piter = page.getAllParentsIterator();
        while (piter.hasNext()) {
            Asm next = piter.next();
            if (firstParent == null) {
                firstParent = next;
            }
            if (next.isTemplate()) {
                template = next;
                break;
            }
        }
        res.put("id", page.getId());
        res.put("guid", page.getName());
        res.put("name", page.getHname());
        res.put("published", page.isPublished());
        res.put("lockUser", page.getLockUser());
        if (page.getLockDate() != null) {
            res.put("lockDate", page.getLockDate().getTime());
        }
        res.putPOJO("core", page.getEffectiveCore());
        if (template == null) {
            res.putNull("template");
        } else {
            res.putObject("template")
               .put("id", template.getId())
               .put("name", template.getName())
               .put("description", template.getDescription());
        }
        if (firstParent != null) {
            res.putObject("firstParent")
               .put("id", firstParent.id)
               .put("name", firstParent.getName())
               .put("description", firstParent.getDescription());
        }

        Collection<AsmAttribute> eattrs = page.getEffectiveAttributes();
        Collection<AsmAttribute> gattrs = new ArrayList<>(eattrs.size());
        AsmAttributeManagersRegistry amreg = amRegistry.get();
        for (AsmAttribute a : eattrs) {
            if (!isBlank(a.getLabel())) { //it is GUI attribute?
                if (template != null) {
                    AsmAttribute tmplAttr = template.getEffectiveAttribute(a.getName());
                    AsmAttributeManager am = amreg.getByType(a.getType());
                    if (am != null) {
                        a = am.prepareGUIAttribute(req, resp, page, template, tmplAttr, a);
                        //noinspection ConstantConditions
                        if (a == null) {
                            continue;
                        }
                    }
                }
                gattrs.add(a);
            }
        }
        String contollerClassName = page.getEffectiveController();
        if (!isBlank(contollerClassName)) {
            Object controller = helper
                    .createControllerInstance(page,
                                              contollerClassName);
            if (controller instanceof AsmAttributesHandler) {
                Collection<AsmAttribute> iattrs =
                        ((AsmAttributesHandler) controller)
                                .onLoadedAttributes(page, gattrs);
                if (iattrs != null) {
                    gattrs = iattrs;
                }
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

    private void updatePublishStatus(HttpServletRequest req,
                                     Long id,
                                     boolean published) {
        ebus.unlockOnTxFinish(Asm.acquireLock(id));
        Asm page = adao.asmSelectById(id);
        if (page == null || !pageSecurity.canEdit2(page, req)) {
            throw new UnauthorizedException();
        }
        if (published && page.getEffectiveCore() == null) {
            throw new NcmsNotificationException("ncms.page.template.publish.error", true, req);
        }
        update("updatePublishStatus",
               "id", id,
               "published", published);
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, page.getId(), req)
                        .hint("published", published));

    }

    /**
     * Acquire edit lock on specified page.
     */
    @PUT
    @Path("/lock/{id}")
    @Transactional
    public ObjectNode lockPage(@Context HttpServletRequest req,
                               @PathParam("id") Long id) throws Exception {
        if (!pageSecurity.canEdit(id, req)) {
            throw new ForbiddenException();
        }
        WSUser wsUser = pageSecurity.getCurrentWSUserSafe(req);
        ObjectNode res = mapper.createObjectNode();
        String locker = adao.asmLock(id, wsUser.getName());
        if (locker != null) { // Page was locked by another user
            res.put("success", locker.equals(wsUser.getName()));
            res.put("locker", locker);
        } else {
            res.put("success", true);
            res.put("locker", wsUser.getName());
        }
        return res;
    }

    @PUT
    @Path("/unlock/{id}")
    @Transactional
    public ObjectNode unlockPage(@Context HttpServletRequest req,
                                 @PathParam("id") Long id) {
        if (!pageSecurity.canEdit(id, req)) {
            throw new ForbiddenException();
        }
        WSUser wsUser = pageSecurity.getCurrentWSUserSafe(req);
        ObjectNode res = mapper.createObjectNode();
        if (pageSecurity.isOwner(id, req)) {
            res.put("success", adao.asmUnlock(id));
        } else {
            res.put("success", adao.asmUnlock(id, wsUser.getName()));
        }
        return res;
    }

    private boolean unlockPage(HttpServletRequest req,
                               Long id,
                               boolean silent) {
        WSUser wsUser = pageSecurity.getCurrentWSUserSafe(req);
        if (pageSecurity.isOwner(id, req)) {
            return adao.asmUnlock(id, silent);
        } else {
            return adao.asmUnlock(id, wsUser.getName(), silent);
        }
    }

    private boolean lockPageOrFail(@Context HttpServletRequest req, Long id, boolean silent) {
        if (!pageSecurity.canEdit(id, req)) {
            throw new ForbiddenException();
        }
        String user = pageSecurity.getCurrentWSUserSafe(req).getName();
        String locker = adao.asmLock(id, user, silent);
        if (locker != null && !locker.equals(user)) {
            throw new NcmsMessageException(i18n.get("ncms.page.locked", req, locker), true);
        }
        return (locker != null);
    }

    @PUT
    @Path("/edit/{id}")
    @Transactional
    @Nullable
    public AsmCore savePage(@Context HttpServletRequest req,
                            @PathParam("id") Long id,
                            ObjectNode data) throws Exception {

        lockPageOrFail(req, id, true);
        AsmAttributeManagerContext amCtx = amCtxProvider.get();
        amCtx.setAsmId(id);

        Asm page = adao.asmSelectById(id);
        WSUser wsUser = pageSecurity.getCurrentWSUserSafe(req);
        if (page == null
            || !pageSecurity.canEdit2(page, req)
            || (page.getLockUser() != null
                && !page.getLockUser().equals(wsUser.getName()))) {
            throw new ForbiddenException();
        }
        Map<String, AsmAttribute> attrIdx = new HashMap<>();
        for (AsmAttribute attr : page.getEffectiveAttributes()) {
            if (attr.getLabel() == null || attr.getLabel().isEmpty()) {
                continue; //no gui label, skipping
            }
            attrIdx.put(attr.getName(), attr);
        }
        Iterator<String> fnamesIt = data.fieldNames();
        AsmAttributeManagersRegistry amreg = amRegistry.get();
        while (fnamesIt.hasNext()) {
            String fname = fnamesIt.next();
            AsmAttribute attr = attrIdx.get(fname);
            if (attr == null) {
                continue;
            }
            amCtx.registerAttribute(attr);

            AsmAttributeManager am = amreg.getByType(attr.getType());
            if (am == null) {
                log.warn("Missing attribute manager for type: {}", attr.getType());
                continue;
            }
            if (attr.getAsmId() != id) { //parent attr
                attr = attr.cloneDeep();
                attr.asmId = id;
            }
            am.applyAttributeValue(amCtx, attr, data.get(fname));
            adao.asmUpsertAttribute(attr);
            if (attr.getId() == null) {
                Number gid = selectOne("prevAttrID");
                if (gid != null) {
                    attr.setId(gid.longValue());
                }
            }
            am.attributePersisted(amCtx, attr, data.get(fname), null);
        }
        amCtx.flush();

        // "core":{"id":141,"location":"/site/httl/my/empty_core.httl","name":null,"templateEngine":null},
        adao.asmUnlock(id, wsUser.getName(), false);
        ebus.fireOnSuccessCommit(
                new AsmModifiedEvent(this, id, req));


        // Refresh page
        page = adao.asmSelectById(id);
        if (page == null) {
            throw new NotFoundException();
        }
        return page.getEffectiveCore();
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

        boolean alreadyLocked = lockPageOrFail(req, id, true);
        try {

            Asm page = adao.asmSelectById(id);
            if (!pageSecurity.canEdit2(page, req)) {
                throw new ForbiddenException();
            }
            Boolean ts = selectOne("selectPageTemplateStatus", templateId);
            if (ts == null) {
                log.warn("Assembly template: {} not found", templateId);
                throw new NotFoundException();
            }

            if (id.equals(templateId)) {
                log.warn("The page {} cannot reference itself as template", id);
                throw new ForbiddenException(i18n.get("ncms.page.template.same", req));
            }

            if (ts == true) {
                Collection<Long> aTemplates = pageSecurity.getAccessibleTemplates(req);
                if (!aTemplates.contains(templateId)) {
                    log.warn("Template: {} is not accesible for user", templateId);
                    throw new ForbiddenException(i18n.get("ncms.page.template.access.denied", req));
                }
            } else {
                // todo check permissions to assign arbitrary assembly as template?
            }

            adao.asmRemoveAllParents(id);
            adao.asmSetParent(id, templateId);

            //Strore incompatible attribute names to clean-up
            List<String> attrsToRemove = new ArrayList<>();
            page = adao.asmSelectById(id);
            if (page == null) {
                throw new NotFoundException();
            }
            Collection<AsmAttribute> attrs = page.getEffectiveAttributes();
            for (AsmAttribute a : attrs) {
                AsmAttribute oa = a.getOverriddenParent();
                if (oa != null &&
                    a.getAsmId() == id &&
                    !Objects.equals(oa.getType(), a.getType())) { //types incompatible
                    attrsToRemove.add(a.getName());
                }
            }
            if (!attrsToRemove.isEmpty()) {
                delete("deleteAttrsByNames",
                       "asmId", id,
                       "names", attrsToRemove);
            }
            ebus.fireOnSuccessCommit(
                    new AsmModifiedEvent(this, id, req)
                            .hint("template", templateId));

            return selectPageEdit(req, resp, id);

        } finally {
            if (!alreadyLocked) {
                unlockPage(req, id, true);
            }
        }
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

        ObjectNode res = mapper.createObjectNode();
        boolean alreadyLocked = lockPageOrFail(req, id, true);
        try {

            if (!pageSecurity.isOwner(id, req)) {
                throw new ForbiddenException();
            }
            WSUser user = userdb.findUser(owner);
            if (user == null) {
                throw new NotFoundException();
            }
            update("setPageOwner",
                   "id", id,
                   "owner", owner);
            JsonUtils.populateObjectNode(user, res.putObject("owner"),
                                         "name", "fullName");
            ebus.fireOnSuccessCommit(
                    new AsmModifiedEvent(this, id, req));
        } finally {
            if (!alreadyLocked) {
                unlockPage(req, id, true);
            }
        }
        return res;
    }

    /**
     * Create a copy of the page
     */
    @PUT
    @Path("/clone")
    @Transactional
    public void clonePage(@Context HttpServletRequest req,
                          ObjectNode spec) throws Exception {

        String guid;
        Long id;
        do {
            guid = new RandomGUID().toString();
            id = adao.asmSelectIdByName(guid);
        } while (id != null); //very uncommon

        long asmId = spec.path("id").asLong();
        ebus.unlockOnTxFinish(Asm.acquireLock(asmId));
        String asmName = adao.asmSelectNameById(asmId);
        if (asmName == null) {
            throw new BadRequestException();
        }

        // Copy assembly structure
        Asm page = adao.asmClone(asmId,
                                 guid,
                                 spec.path("type").asText(),
                                 spec.path("name").asText(),
                                 spec.path("name").asText(),
                                 null);

        // Copy assembly media files
        Map<Long, Long> fmap = mrepo.copyPageMedia(asmId, page.getId(), req.getRemoteUser());

        // Postprocess assembly attributes
        AsmAttributeManagerContext amCtx = amCtxProvider.get();
        amCtx.setAsmId(page.getId());
        for (AsmAttribute attr : page.getEffectiveAttributes()) {
            if (attr.getAsmId() != page.getId()) { //parent attr
                attr = attr.cloneDeep();
                attr.asmId = page.getId();
            }
            amCtx.registerAttribute(attr);
            AsmAttributeManagersRegistry amreg = amRegistry.get();
            AsmAttributeManager am = amreg.getByType(attr.getType());
            if (am == null) {
                log.warn("Missing attribute manager for type: {}", attr.getType());
                continue;
            }
            am.handleAssemblyCloned(amCtx, attr, fmap);
            adao.asmUpsertAttribute(attr);
        }
        amCtx.flush();
        ebus.fireOnSuccessCommit(
                new AsmCreatedEvent(this, page, req)
                        .hint("page", true));
    }

    /**
     * Create a new page
     */
    @PUT
    @Path("/new")
    @Transactional
    public JsonNode newPage(@Context HttpServletRequest req,
                            ObjectNode spec) {

        String name = spec.hasNonNull("name") ? spec.get("name").asText().trim() : null;
        Long parent = spec.hasNonNull("parent") ? spec.get("parent").asLong() : null;
        String type = spec.hasNonNull("type") ? spec.get("type").asText().trim() : null;

        if (name == null ||
            !ArrayUtils.isAnyOf(type, "page.folder", "page", "news.page")) {
            throw new BadRequestException();
        }
        if (parent == null && !req.isUserInRole("admin.structure")) {
            throw new ForbiddenException();
        }
        if (parent != null &&
            ("news.page".equals(type) ?
             !pageSecurity.canNewsEdit(parent, req) :
             !pageSecurity.canEdit(parent, req))) {
            throw new ForbiddenException();
        }

        String guid;
        Long id;
        do {
            guid = new RandomGUID().toString();
            id = adao.asmSelectIdByName(guid);
        } while (id != null); //very uncommon


        String pageIDsPath = getPageIDsPath(parent);
        update("mergeNewPage",
               "guid", guid,
               "name", name,
               "description", name,
               "type", type,
               "user", req.getRemoteUser(),
               "nav_parent_id", parent,
               "lang", getPageLang(pageIDsPath),
               "nav_cached_path", pageIDsPath,
               "recursive_acl", selectOne("getRecursiveAcl", "pid", parent));

        id = adao.asmSelectIdByName(guid);
        if (id == null) {
            throw new NotFoundException();
        }
        /*if ("news.page".equals(type)) {
            String alias = generateNewsAlias(name);

        }*/
        ebus.fireOnSuccessCommit(
                new AsmCreatedEvent(this, id, parent, name, name, req)
                        .hint("page", true));

        ObjectNode res = mapper.createObjectNode();
        res.put("id", id);
        res.put("name", name);
        res.put("type", type);
        res.put("parent", parent);
        return res;
    }

    @Nonnull
    public String getPageLang(String pageIDsPath) {
        int ind = pageIDsPath.indexOf('/', 1);
        String lang = null;
        Long pid = (ind == -1 || ind == 1) ? null : Long.parseLong(pageIDsPath.substring(1, ind));
        if (pid != null) {
            synchronized (lvh2IndexPages) {
                lang = indexPage2FirstLang.get(pid);
                if (lang == null) {
                    lang = indexPage2SecondLang.get(pid);
                }
            }
        }
        if (lang == null) {
            lang = i18n.getLocale(null).getLanguage();
        }
        return lang;
    }

    @PUT
    @Path("/update/basic")
    @Transactional
    public void updatePageBasic(@Context HttpServletRequest req,
                                @Context SecurityContext sctx,
                                ObjectNode spec) {

        String name = spec.hasNonNull("name") ? spec.get("name").asText().trim() : null;
        String type = spec.hasNonNull("type") ? spec.get("type").asText().trim() : null;
        Long id = spec.hasNonNull("id") ? spec.get("id").asLong() : null;
        if (id == null || name == null || type == null
            || (!"page.folder".equals(type) && !"page".equals(type) && !"news.page".equals(type))) {
            throw new BadRequestException();
        }
        boolean alreadyLocked = lockPageOrFail(req, id, true);
        try {

            final CachedPage page = getCachedPage(id, true);
            if (page == null) {
                throw new NotFoundException();
            }
            Long parent = page.getNavParentId();
            if ("news.page".equals(type) ?
                !(parent != null && pageSecurity.canNewsEdit(parent, req)) :
                !pageSecurity.canDelete(id, req)) {
                throw new ForbiddenException();
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
                   "muser", pageSecurity.getCurrentWSUserSafe(req).getName());

            ebus.fireOnSuccessCommit(
                    new AsmModifiedEvent(this, id, req));

        } finally {
            if (!alreadyLocked) {
                unlockPage(req, id, true);
            }
        }
    }

    @PUT
    @Path("/move")
    @Transactional
    public void movePage(@Context HttpServletRequest req,
                         ObjectNode spec) {

        long src = spec.hasNonNull("src") ? spec.get("src").longValue() : 0;
        long tgt = spec.hasNonNull("tgt") ? spec.get("tgt").longValue() : 0;
        if (src == 0) {
            throw new BadRequestException();
        }
        ebus.unlockOnTxFinish(Asm.acquireLock(tgt));
        boolean alreadyLocked = lockPageOrFail(req, src, true);
        try {

            Asm srcPage = adao.asmSelectById(src);
            Asm tgtPage = (tgt != 0) ? adao.asmSelectById(tgt) : null; //zero tgt => Root target
            if (srcPage == null) {
                throw new NotFoundException();
            }
            if (tgtPage != null && !"page.folder".equals(tgtPage.getType())) {
                throw new BadRequestException();
            }
            if (src == tgt) {
                String msg = i18n.get("ncms.mmgr.folder.cantMoveIntoSelf", req, srcPage.getHname());
                throw new NcmsNotificationException(msg, true, req);
            }
            if (tgtPage != null && "page.folder".equals(srcPage.getType())) {
                String srcPath = getPageIDsPath(src);
                String tgtPath = getPageIDsPath(tgt);
                if (tgtPath.startsWith(srcPath)) {
                    String msg = i18n.get("ncms.mmgr.folder.cantMoveIntoSubfolder", req,
                                          srcPage.getHname(), tgtPage.getHname());
                    throw new NcmsNotificationException(msg, true, req);
                }
            }

            //check user access
            if (
                    (tgt == 0 && !req.isUserInRole("admin.structure")) ||
                    !pageSecurity.checkAccess(tgt, req, 'w') ||
                    !pageSecurity.checkAccess(src, req, 'd')) {
                throw new ForbiddenException();
            }

            update("prepareMove", "nav_cached_path", getPageIDsPath(src));
            String pageIDsPath = getPageIDsPath(tgt != 0 ? tgt : null);
            update("movePage",
                   "id", src,
                   "nav_cached_path", pageIDsPath,
                   "nav_parent_id", (tgt != 0) ? tgt : null,
                   "lang", getPageLang(pageIDsPath));

            while (update("finishMove") > 0) ;
            ebus.fireOnSuccessCommit(
                    new AsmModifiedEvent(this, srcPage.getId(), req)
                            .hint("page", true)
                            .hint("moveTargetId", (tgtPage != null ? tgtPage.getId() : 0)));
        } finally {
            if (!alreadyLocked) {
                unlockPage(req, src, true);
            }
        }
    }

    @GET
    @Path("/referrers/{guid}")
    public JsonNode getPageReferrers(@PathParam("guid") String guid) {
        ArrayNode res = mapper.createArrayNode();
        List<Map<String, ?>> referrers = select("selectPagesDependentOn", guid);
        for (Map<String, ?> referrer : referrers) {
            CachedPage cp = getCachedPage((Long) referrer.get("asmid"), true);
            if (cp == null) {
                continue;
            }
            res.addObject()
               .put("name", (String) referrer.get("name"))
               .put("path", ArrayUtils.stringJoin(cp.<String[]>fetchNavPaths().get(PATH_TYPE.LABEL), "/"))
               .put("icon", Converters.toBoolean(referrer.get("published")) ? "" : "ncms/icon/16/misc/exclamation.png")
               .put("asmid", (Long) referrer.get("asmid"));
        }
        return res;
    }

    @GET
    @Path("/referrers/count/{id}")
    public Number getPageReferrersCount(@PathParam("id") Long id) {
        Asm page = adao.asmSelectById(id);
        if (page == null) {
            throw new NotFoundException();
        }

        return count("selectCountOfPagesDependentOn", page.getName());
    }

    @GET
    @Path("/referrers/to/{id}")
    public JsonNode getPageReferrersTo(@PathParam("id") Long id) {
        ArrayNode res = mapper.createArrayNode();
        List<Map<String, ?>> referrers = select("selectPagesDependentTo", id);
        for (Map<String, ?> referrer : referrers) {
            CachedPage cp = getCachedPage((Long) referrer.get("asmid"), true);
            if (cp == null) {
                continue;
            }
            res.addObject()
               .put("name", (String) referrer.get("name"))
               .put("path", ArrayUtils.stringJoin(cp.<String[]>fetchNavPaths().get(PATH_TYPE.LABEL), "/"))
               .put("icon", Converters.toBoolean(referrer.get("published")) ? "" : "ncms/icon/16/misc/exclamation.png")
               .put("guid", (String) referrer.get("guid"));
        }
        return res;
    }

    @GET
    @Path("/referrers/to/count/{id}")
    public Number getPageReferrersCountTo(@PathParam("id") Long id) {
        return count("selectCountOfPagesDependentTo", id);
    }

    @GET
    @Path("/referrers/attributes/{guid}/{asmid}")
    public Response getPageReferrerAttributes(@PathParam("guid") String guid,
                                              @PathParam("asmid") Long asmid) {
        return Response.ok((StreamingOutput) output -> {
                               final JsonGenerator gen = new JsonFactory().createGenerator(output);
                               gen.writeStartArray();
                               List<Map<String, ?>> attrs = select("selectAttributesDependentOn", "guid", guid, "asmid", asmid);
                               for (Map<String, ?> attr : attrs) {
                                   gen.writeStartObject();
                                   gen.writeStringField("type", (String) attr.get("type"));
                                   gen.writeStringField("name", (String) attr.get("name"));
                                   gen.writeEndObject();
                               }
                               gen.writeEndArray();
                               gen.flush();
                           }
        ).type("application/json;charset=UTF-8").build();
    }

    // todo replace/review
    @GET
    @Path("/referers/orphans")
    public Response getOrphanPages() {
        return Response.ok((StreamingOutput) o -> {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(o, "UTF-8"));
            pw.println("<!DOCTYPE html>");
            pw.println("<html>");
            pw.println("<body>");
            pw.print("<h2>");
            pw.print(i18n.get("ncms.page.orphan.list"));
            pw.print("</h2>");

            pw.println("<ol>");
            select("selectOrphanPages", context -> {
                //noinspection unchecked
                Map<String, ?> row = (Map<String, ?>) context.getResultObject();
                String pguid = (String) row.get("guid");
                String name = (String) row.get("name");
                boolean published = Converters.toBoolean(row.get("published"));
                pw.println("<li><a href='" + (asmRoot + pguid) + "'>" + name + "</a> " +
                           (!published ? "(not published)</li>" : "</li>"));
            });
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
    public ObjectNode removePage(@PathParam("id") Long id,
                                 @Context HttpServletRequest req) {

        ObjectNode ret = mapper.createObjectNode();
        boolean alreadyLocked = lockPageOrFail(req, id, true);
        try {
            Asm page = adao.asmSelectById(id);
            if (page == null) {
                throw new NotFoundException();
            }
            if (!pageSecurity.checkAccessAll2(page, req, "d")) {
                throw new ForbiddenException();
            }
            if (adao.asmChildrenCount(id) > 0) {
                throw new NcmsNotificationException("ncms.page.nodel.parent", true, req);
            }
            if (count("selectNumberOfDirectChilds", id) > 0) {
                throw new NcmsNotificationException("ncms.page.nodel.children", true, req);
            }
            if (count("selectCountOfAttrsDependentOn", page.getName()) > 0) {
                ret.put("error", "ncms.page.nodel.refs.found");
                return ret;
            }

            //todo check dependent files?

            adao.asmRemove(id);
            ebus.fireOnSuccessCommit(
                    new AsmRemovedEvent(this, id, req)
                            .hint("page", true));
        } finally {
            if (!alreadyLocked) {
                unlockPage(req, id, true);
            }
        }
        return ret;
    }

    @Path("/layer")
    @GET
    public Response selectLayer(@Context final HttpServletRequest req) {
        return _selectLayer(req, null);
    }

    @Path("/layer/{path:.*}")
    @GET
    public Response selectLayer(@Context final HttpServletRequest req,
                                @PathParam("path") String path) {
        return _selectLayer(req, path);
    }

    Response _selectLayer(@Context final HttpServletRequest req,
                          @Nullable final String path) {

        return Response.ok((StreamingOutput) output -> {
            final boolean includePath = BooleanUtils.toBoolean(req.getParameter("includePath"));
            final JsonGenerator gen = new JsonFactory().createGenerator(output);
            gen.writeStartArray();
            try {
                Map<String, Object> q = createSelectLayerQ(path, req);
                q.put("user", req.getRemoteUser());
                String stmtName = q.containsKey("nav_parent_id") ? "selectChildLayer" : "selectRootLayer";
                select(stmtName, context -> {
                    //noinspection unchecked
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
                        if (!Converters.toBoolean(row.get("published"))) { //page not published
                            status |= PAGE_STATUS_NOT_PUBLISHED_FLAG;
                        }
                        if (((Number) row.get("num_parents")).intValue() > 0) {
                            status |= PAGE_STATUS_HAS_PARENTS;
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
            throw new ForbiddenException();
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
            throw new ForbiddenException();
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
            throw new ForbiddenException();
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
    @Produces("text/plain;charset=UTF-8")
    @Nullable
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
                    //noinspection unchecked
                    Map<String, ?> row = (Map<String, ?>) context.getResultObject();
                    try {
                        boolean published = Converters.toBoolean(row.get("published"));

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
        Asm page = adao.asmSelectById(pid);
        if (page == null) {
            throw new NotFoundException();
        }
        return pageSecurity.checkAccessAll2(page, req, rights);
    }

    @GET
    @Path("rights/{pid}")
    @Produces("text/plain;charset=UTF-8")
    @Transactional
    public String getAccessRights(@Context HttpServletRequest req,
                                  @PathParam("pid") Long pid) {
        Asm page = adao.asmSelectById(pid);
        if (page == null) {
            throw new NotFoundException();
        }
        return pageSecurity.getAccessRights2(page, req);
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

    @Nonnull
    public String getPageIDsPath(@Nullable Long id) {
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
                return Arrays.stream(labels).filter(Objects::nonNull).toArray(String[]::new);
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
        if (!isBlank(val)) {
            cq.withParam("name_lower", val.toLowerCase() + "%");
        }
        String type = "page%";
        val = req.getParameter("type");
        if (!isBlank(val)) {
            type = val;
        } else if (BooleanUtils.toBoolean(req.getParameter("foldersOnly"))) {
            type = "page.folder";
        }
        val = req.getParameter("parentId");
        if (!isBlank(val)) {
            cq.withParam("parentId", Long.parseLong(val));
        }
        cq.withParam("type", type);
        cq.withParam("user", req.getRemoteUser());

        val = req.getParameter("collection");
        if (!isBlank(val)) {
            cq.withParam("collection", val);
        }
        if (!count) {
            val = req.getParameter("sortAsc");
            if (!isBlank(val)) {
                if ("label".equals(val)) {
                    val = "hname";
                }
                cq.orderBy("p." + val).asc();
            }
            val = req.getParameter("sortDesc");
            if (!isBlank(val)) {
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

    @Nullable
    private Long getPathLastIdSegment(@Nullable String path) {
        if (path == null) {
            return null;
        }
        int idx = path.lastIndexOf('/');
        if (idx == -1 || idx == path.length() - 1) {
            return Long.valueOf(path);
        }
        return Long.valueOf(path.substring(idx + 1));
    }

    private Map<String, Object> createSelectLayerQ(@Nullable String path, HttpServletRequest req) {
        Long pId = getPathLastIdSegment(path);
        Map<String, Object> ret = new TinyParamMap<>();
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

    private final class PagesLRUMap<K, V> extends LRUMap<K, V> {

        private PagesLRUMap(int maxSize) {
            super(maxSize, true);
        }

        @Override
        protected boolean removeLRU(LinkEntry entry) {
            CachedPage cp = (CachedPage) entry.getValue();
            Long pid = cp.getId();
            // Index pages not evicted from cache
            synchronized (lvh2IndexPages) {
                if (indexPage2FirstLang.containsKey(pid)) {
                    return false;
                }
            }
            synchronized (pagesCache) {
                pageGuid2Cache.remove(cp.getName());
                if (cp.getAlias() != null) {
                    pageAlias2Cache.remove(cp.getAlias());
                }
            }
            return true;
        }
    }

    private class CachedPageImpl implements CachedPage {

        private final Asm asm;

        @Override
        public Asm getAsm() {
            return asm;
        }

        @Override
        public Long getId() {
            return asm.getId();
        }

        @Override
        public String getAlias() {
            return (asm.getNavAlias() != null ? asm.getNavAlias() : asm.getNavAlias2());
        }

        @Override
        public String getName() {
            return asm.getName();
        }

        @Override
        public String getHname() {
            return asm.getHname();
        }

        @Override
        public boolean isPublished() {
            return asm.isPublished();
        }

        @Override
        public Long getNavParentId() {
            return asm.getNavParentId();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<PATH_TYPE, Object> fetchNavPaths() {
            String cpath = asm.getNavCachedPath();
            Map<PATH_TYPE, Object> res = new EnumMap<>(PATH_TYPE.class);
            cpath = (cpath != null) ? StringUtils.strip(cpath, "/") : null;
            if (isBlank(cpath)) {
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

    /**
     * Represents page that has `mainpage` attribute
     */
    private class IndexPageImpl implements IndexPage {

        private final CachedPage cp;

        private JsonNode cachedOptions;

        IndexPageImpl(CachedPage cp) {
            this.cp = cp;
        }

        @Override
        @Nonnull
        public Asm getAsm() {
            return cp.getAsm();
        }

        @Override
        @Nonnull
        public Long getId() {
            return cp.getId();
        }

        @Override
        @Nullable
        public String getAlias() {
            return cp.getAlias();
        }

        @Override
        @Nonnull
        public String getName() {
            return cp.getName();
        }

        @Override
        @Nullable
        public String getHname() {
            return cp.getHname();
        }

        @Override
        public boolean isPublished() {
            return cp.isPublished();
        }

        @Override
        @Nullable
        public Long getNavParentId() {
            return cp.getNavParentId();
        }

        @Override
        @Nonnull
        public <T> Map<PATH_TYPE, T> fetchNavPaths() {
            return cp.fetchNavPaths();
        }

        /**
         * Returns `robots.txt` option value of mainpage attribute
         */
        @Nullable
        @Override
        public String getRobotsConfig() {
            return trimToNull(getOptions().path(IndexPage.ROBOTS_TXT).asText());
        }

        @Nullable
        @Override
        public String getFaviconBase64() {
            return trimToNull(getOptions().path(IndexPage.FAVICON_ICO).asText());
        }

        @Nonnull
        private JsonNode getOptions() {
            if (cachedOptions != null) {
                return cachedOptions;
            }
            Asm asm = cp.getAsm();
            AsmAttribute mainPageAttr = asm.getAttribute("mainpage");
            if (mainPageAttr == null) {
                cachedOptions = mapper.createObjectNode();
                return cachedOptions;
            }
            try {
                cachedOptions = mapper.readTree(trimToEmpty(mainPageAttr.getEffectiveValue()));
            } catch (IOException e) {
                log.error("Failed to parse mainpage attribute value", e);
                cachedOptions = mapper.createObjectNode();
            }
            return cachedOptions;
        }
    }

    @Subscribe
    @Transactional
    public void onAsmRemoved(AsmRemovedEvent ev) {
        Long pid = ev.getId();
        clearCachedPage(pid);
        if (removeFromIndexPages(pid)) {
            reloadIndexPages();
        }
        delete("deleteFileDeps", pid);
        delete("deletePageDeps", pid);
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
                pageGuid2Cache.remove(p.getName());
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

    @Override
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
                pageGuid2Cache.put(cp.getName(), cp);
                if (cp.getAlias() != null) {
                    pageAlias2Cache.put(cp.getAlias(), cp);
                }
            } else {
                cp = cp2;
            }
        }
        return cp;
    }

    @Override
    public CachedPage getCachedPage(String guidOrAlias, boolean create) {
        if (isBlank(guidOrAlias)) {
            return null;
        }
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
                boolean isGuid = GUID_REGEXP.matcher(guidOrAlias).matches();
                if (isGuid) {
                    id = adao.asmSelectIdByName(guidOrAlias);
                } else {  // try to find assembly by alias
                    id = adao.asmSelectIdByAlias(guidOrAlias);
                }
                if (id == null && !isGuid) { // try to find assembly by bare name
                    id = adao.asmSelectIdByName(guidOrAlias);
                }
                if (id != null) {
                    cp = getCachedPage(id, true);
                }
            }
        }
        return cp;
    }

    @Override
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


    @Override
    public String resolvePageLink(Long id) {
        if (id == null) {
            return null;
        }
        //todo use alias
        return asmRoot + id;
    }

    @Override
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

    @Override
    public String resolvePageGuid(String spec) {
        if (spec == null) {
            return null;
        }
        spec = spec.toLowerCase();
        if (spec.startsWith("page:")) { //Page reference
            int plen = "page:".length();
            spec = spec.substring(spec.length() > plen && spec.charAt(plen) == '/' ? plen + 1 : plen);
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

    @Override
    public String resolveResourceLink(String spec) {
        if (spec == null) {
            return null;
        }
        if (spec.charAt(0) == '#' || spec.contains("://")) {
            return spec;
        }
        Long fid = mrepo.getFileIdByResourceSpec(spec);
        if (fid != null) {
            return mrepo.resolveFileLink(fid, true);
        }
        spec = spec.toLowerCase();
        if (spec.startsWith("page:")) { //Page reference
            int plen = "page:".length();
            spec = spec.substring(spec.length() > plen && spec.charAt(plen) == '/' ? plen + 1 : plen);
            int ind = spec.indexOf('|');
            if (ind != -1) {
                spec = spec.substring(0, ind).trim();
            }
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

    private boolean removeFromIndexPages(Long pid) {
        if (pid == null) {
            return false;
        }
        boolean ret = false;
        synchronized (lvh2IndexPages) {
            if (indexPage2FirstLang.containsKey(pid)) {
                ret = true;
                indexPage2FirstLang.remove(pid);
                indexPage2SecondLang.remove(pid);
                List<String> vhosts = new ArrayList<>(10);
                for (Map<String, Long> ve : lvh2IndexPages.values()) {
                    for (Map.Entry<String, Long> e : ve.entrySet()) {
                        if (pid.equals(e.getValue())) {
                            vhosts.add(e.getKey());
                        }
                    }
                    for (String vh : vhosts) {
                        ve.remove(vh);
                    }
                }
            }
        }
        return ret;
    }

    private Long getVHostPage(String rhost, Map<String, Long> vh2i) {
        Long pid = vh2i.get(rhost);
        if (pid == null) {
            pid = vh2i.get("*");
        }
        return pid;
    }

    @Override
    public IndexPage getIndexPage(HttpServletRequest req, boolean requirePublished) {

        CachedPage p;
        Long pid = (Long) req.getAttribute(INDEX_PAGE_REQUEST_ATTR_NAME);
        if (pid != null) {
            p = getCachedPage(pid, true);
            if (p != null && (!requirePublished || p.isPublished())) {
                return new IndexPageImpl(p);
            }
            req.removeAttribute(INDEX_PAGE_REQUEST_ATTR_NAME);
            pid = null;
        }
        Locale locale = i18n.getLocale(req);
        String rlang = locale.getLanguage();
        String rhost = req.getServerName();

        synchronized (lvh2IndexPages) {
            //noinspection LoopStatementThatDoesntLoop
            do {
                Map<String, Long> vh2i = lvh2IndexPages.get(rlang);
                if (vh2i == null) {
                    vh2i = lvh2IndexPages.get("*");
                }
                if (vh2i != null) {
                    pid = getVHostPage(rhost, vh2i);
                    if (pid != null) {
                        break;
                    }
                }
                for (Map<String, Long> vh2i2 : lvh2IndexPages.values()) {
                    pid = getVHostPage(rhost, vh2i2);
                    if (pid != null) {
                        break;
                    }
                }
            } while (false);
        }
        if (pid == null) {
            return null;
        }
        p = getCachedPage(pid, true);
        if (p == null) {
            removeFromIndexPages(pid);
        } else if (requirePublished && !p.isPublished()) {
            p = null;
        }
        if (p != null) {
            req.setAttribute(INDEX_PAGE_REQUEST_ATTR_NAME, pid);
        }
        return new IndexPageImpl(p);
    }

    @Override
    @Nullable
    public String getIndexPageLanguage(HttpServletRequest req) {
        String lang = null;
        CachedPage p = getIndexPage(req, false);
        if (p != null) {
            lang = indexPage2FirstLang.get(p.getId());
            if (lang == null) {
                lang = indexPage2SecondLang.get(p.getId());
            }
        }
        return (lang == null) ? i18n.getLocale(req).getLanguage() : lang;
    }

    @Transactional
    public void reloadIndexPages() {

        // Map<String, Map<String, Long>> lvh2IndexPages; // Star `*` included in this collection
        // Map<Long, String> indexPage2FirstLang; // Star `*` lang is used as `null` in this collection

        List<Map<String, Object>> ipages =
                select("selectAttrOptions",
                       "attrType", "mainpage",
                       "pageType", "page%");

        synchronized (lvh2IndexPages) {

            lvh2IndexPages.clear();
            indexPage2FirstLang.clear();
            indexPage2SecondLang.clear();
            indexPage2Slot.clear();

            for (Map row : ipages) {
                Long pid = NumberUtils.number2Long((Number) row.get("id"), -1L);
                if (pid == -1L) {
                    continue;
                }
                KVOptions options = new KVOptions();
                options.loadOptions((String) row.get("options"));
                if (!"true".equals(options.get("enabled"))) {
                    continue;
                }
                CachedPage cp = getCachedPage(pid, true);
                if (cp == null) {
                    continue;
                }
                String lp = ArrayUtils.stringJoin(cp.<String[]>fetchNavPaths().get(PATH_TYPE.LABEL), "/");
                String ln = options.get("lang");
                if (isBlank(ln)) {
                    ln = "*";
                }
                String vh = options.get("vhost");
                if (isBlank(vh)) {
                    vh = "*";
                }
                String[] lcodes = ArrayUtils.split(ln, " ,;");
                String[] vhosts = ArrayUtils.split(vh, " ,;");
                indexPage2Slot.put(pid, new IndexPageSlot(lcodes, vhosts));

                for (int i = 0, c = 0; i < lcodes.length; ++i) {
                    String lc = lcodes[i].trim();
                    if (lc.isEmpty()) continue;
                    if (c++ == 0) {
                        indexPage2FirstLang.put(pid, "*".equals(lc) ? null : lc);
                    } else if (c == 1 && !"*".equals(lc)) {
                        indexPage2SecondLang.put(pid, lc);
                    }
                    for (int j = 0; j < vhosts.length; ++j) {
                        vh = vhosts[j].trim();
                        if (vh.isEmpty()) continue;
                        Map<String, Long> vh2i = lvh2IndexPages.computeIfAbsent(lc, k -> new LinkedHashMap<>());
                        log.info("BIND MAIN PAGE lang:{} vhost:{} TO {}", lc, vh, lp);
                        vh2i.put(vh, pid);
                    }
                }
            }
            if (indexPage2FirstLang.isEmpty()) {
                log.warn("No main pages found!");
            }
        }
    }

    private static class IndexPageSlot {
        private String[] langCodes;
        private String[] virtualHosts;

        private IndexPageSlot(String[] langCodes, String[] virtualHosts) {
            this.langCodes = langCodes;
            this.virtualHosts = virtualHosts;
        }
    }

    @Transactional
    @Scheduled("* * * * *")
    public void cleanupOldLockedPages() {
        long maxIdleMins = env.xcfg().getInt("pages.userlock-max-idle-sec", 60 * 60 * 1); // 1 hour
        List<Long> ids = select("selectOldLockedPages", maxIdleMins);
        if (ids.isEmpty()) {
            return;
        }
        log.warn("Found page locks inactive more than {} seconds. " +
                 "Pages will be unlocked", maxIdleMins);
        for (Long id : ids) {
            log.warn("Unlocking page: {}", id);
            adao.asmUnlock(id);
        }
    }

    @Start(order = 90, parallel = true)
    public void start() {
        reloadIndexPages();
    }
}
