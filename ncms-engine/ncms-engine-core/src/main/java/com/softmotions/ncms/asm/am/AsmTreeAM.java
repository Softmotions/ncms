package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRendererContextFactory;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.ncms.mhttl.Tree;

/**
 * Tree strucrure attribute manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@SuppressWarnings("unchecked")
@Singleton
@Path("adm/am/tree")
@Produces("application/json;charset=UTF-8")
public class AsmTreeAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmTreeAM.class);

    private static final String[] TYPES = new String[]{"tree"};

    private static final Tree EMPTY_TREE = new Tree("root");

    private final ObjectMapper mapper;

    private final AsmRichRefAM richRefAM;

    private final PageService pageService;

    private final AsmDAO adao;

    private final NcmsMessages messages;

    private final AsmRendererContextFactory rendererContextFactory;


    @Inject
    public AsmTreeAM(ObjectMapper mapper,
                     AsmRichRefAM richRefAM,
                     PageService pageService,
                     AsmDAO adao,
                     NcmsMessages messages,
                     AsmRendererContextFactory rendererContextFactory) {
        this.mapper = mapper;
        this.richRefAM = richRefAM;
        this.pageService = pageService;
        this.adao = adao;
        this.messages = messages;
        this.rendererContextFactory = rendererContextFactory;
    }


    @PUT
    @Path("/sync")
    @Consumes("application/json")
    public ObjectNode syncWith(@Context HttpServletRequest req,
                               @Context HttpServletResponse resp,
                               ObjectNode spec) {
        Long srcId;
        Long tgtId;
        String attrName;
        JsonNode n = spec.get("src");
        srcId = (n != null && n.isNumber()) ? n.longValue() : null;
        n = spec.get("tgt");
        tgtId = (n != null && n.isNumber()) ? n.longValue() : null;
        n = spec.get("attr");
        attrName = n.asText(null);
        if (srcId == null || tgtId == null || attrName == null) {
            throw new BadRequestException("");
        }
        if (srcId.equals(tgtId)) {
            return mapper.createObjectNode();
        }
        Asm srcAsm = adao.asmSelectById(srcId);
        Asm tgtAsm = adao.asmSelectById(tgtId);
        if (srcAsm == null || tgtAsm == null) {
            log.warn("One of assembly not found for spec: {}", spec);
            throw new BadRequestException("");
        }
        AsmAttribute tgtAttr = tgtAsm.getEffectiveAttribute(attrName);
        if (tgtAttr == null) {
            log.warn("Target attribute not found in: {}", tgtAsm);
            throw new BadRequestException("");
        }
        AsmAttribute srcAttr = srcAsm.getEffectiveAttribute(attrName);
        if (srcAttr == null) {
            throw new NcmsMessageException(
                    messages.get("ncms.page.attr.notFound",
                                 req, tgtAsm.getHname(), attrName),
                    true);
        }

        int syncIndex;
        Long syncId;
        Asm syncAsm;
        AsmAttribute syncAttr;
        String opts = srcAttr.getOptions();
        Set<Long> syncAsmIds = new HashSet<>();
        syncAsmIds.add(tgtId);

        while ((syncIndex = opts.indexOf("syncWith")) >= 0) {
            syncId = Long.decode(opts.substring(syncIndex + 9, opts.indexOf(',', syncIndex)));

            if (!syncAsmIds.add(syncId)) {
                throw new NcmsMessageException(
                        messages.get("ncms.page.sync.cycle", req),
                        true);
            }

            syncAsm = adao.asmSelectById(syncId);
            if (syncAsm == null) {
                log.warn("Assembly not found for id: {}", syncId);
                throw new BadRequestException("");
            }

            syncAttr = syncAsm.getEffectiveAttribute(attrName);
            if (syncAttr == null) {
                throw new NcmsMessageException(
                        messages.get("ncms.page.attr.notFound",
                                     req, syncAsm.getHname(), attrName),
                        true);
            }
            opts = syncAttr.getOptions();
        }

        AsmRendererContext ctx = rendererContextFactory.createStandalone(req, resp, tgtId);
        Tree stree = getSyncTree(srcId, ctx, tgtAttr);
        spec.putPOJO("tree", stree);
        return spec;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {

        KVOptions opts = new KVOptions();
        opts.loadOptions(StringUtils.isBlank(attr.getOptions()) ? tmplAttr.getOptions() : attr.getOptions());
        Long syncWith = opts.getLongObject("syncWith", null);
        if (syncWith != null) {
            AsmRendererContext ctx = rendererContextFactory.createStandalone(req, resp, page.getId());
            Tree stree = getSyncTree(syncWith, ctx, attr);
            if (stree != null) {
                attr.setEffectiveValue(mapper.writeValueAsString(stree));
                return attr;
            }
        }
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            attr.setEffectiveValue(mapper.writeValueAsString(new Tree(attr.getLabel() != null ? attr.getLabel() : "root")));
        } else {
            //todo sync?
        }
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Tree tree = EMPTY_TREE;
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || StringUtils.isBlank(attr.getEffectiveValue())) {
            return tree;
        }
        KVOptions opts = new KVOptions();
        opts.loadOptions(attr.getOptions());
        tree = getSyncTree(opts.getLongObject("syncWith", null), ctx, attr);
        try {
            if (tree == null) {
                tree = mapper.readerFor(Tree.class).readValue(attr.getEffectiveValue());
            }
            if (!"true".equals(options.get("noinit"))) {
                tree = initTree(tree, ctx);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tree;
    }

    private Tree getSyncTree(Long syncPageId, AsmRendererContext ctx, AsmAttribute attr) throws AsmRenderingException {
        if (syncPageId == null) {
            return null;
        }
        CachedPage syncPage = pageService.getCachedPage(syncPageId, true);
        if (syncPage == null) {
            log.warn("Failed to find referenced page with id: {}", syncPageId);
            return null;
        }
        Asm syncAsm = syncPage.getAsm();
        if (syncAsm.getId().equals(ctx.getAsm().getId())) { //avoid recursion
            log.warn("Recursive attribute sychronization. Asm: {} attr: {}",
                     syncAsm.getName(), attr.getName());
            return null;
        }
        AsmAttribute syncAttr = syncAsm.getEffectiveAttribute(attr.getName());
        if (syncAttr == null || !attr.getType().equals(syncAttr.getType())) {
            log.warn("Found incompatible sync attributes. Source asm: {} attr name: {} sync attr: {}",
                     syncPageId, attr.getName(), syncAttr);
            return null;
        }
        KVOptions opts = new KVOptions();
        opts.put("noinit", "true");
        return (Tree) ctx.renderAttribute(syncAsm, attr.getName(), opts);
    }

    private Tree initTree(Tree tree, AsmRendererContext ctx) throws IOException {
        tree.setLink(ctx.getPageService().resolveResourceLink(tree.getLink()));
        if (!StringUtils.isBlank(tree.getNam())) {
            JsonNode namSpec = mapper.readTree(tree.getNam());
            if (!namSpec.hasNonNull("naClass")) {
                return tree;
            }
            String naClass = namSpec.get("naClass").asText();
            if (!"ncms.asm.am.RichRefAM".equals(naClass)) {
                return tree;
            }
            tree.setRichRef(richRefAM.renderAsmAttribute(ctx, (ObjectNode) namSpec));
        }
        if (tree.isHasChildren()) {
            for (Tree c : tree.getChildren()) {
                initTree(c, ctx);
            }
        }
        return tree;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts);
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        ctx.clearPageDeps(attr);
        ctx.clearFileDeps(attr);

        ObjectNode tree = (ObjectNode) val;
        if (tree == null) {
            attr.setEffectiveValue(null);
            return attr;
        }
        KVOptions opts = new KVOptions();
        opts.loadOptions(attr.getOptions());
        JsonNode n = tree.get("syncWith");
        boolean inSync = (n != null && n.isNumber());
        if (inSync) {
            Tree stree = getSyncTree(n.asLong(),
                                     rendererContextFactory.createStandalone(ctx.getRequest(), ctx.getResponse(), attr.getAsmId()),
                                     attr);
            if (stree == null) {
                opts.remove("syncWith");
            } else {
                opts.put("syncWith", n.asLong());
                String guid = adao.asmSelectNameById(n.asLong());
                if (guid != null) {
                    ctx.registerPageDependency(attr, guid);
                }
            }
        } else {
            opts.remove("syncWith");
        }
        tree.remove("syncWith");
        tree.remove("syncWithId"); //legacy

        attr.setOptions(opts.toString());
        try {
            saveTree(ctx, attr, tree, inSync);
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
        attr.setEffectiveValue(tree.toString());
        return attr;
    }

    private void saveTree(AsmAttributeManagerContext ctx, AsmAttribute attr, ObjectNode tree, boolean inSync) throws IOException {
        String type = tree.hasNonNull("type") ? tree.get("type").asText() : null;
        JsonNode node = tree.get("id");
        JsonNode linkNode = tree.get("link");
        Long id = null;
        if (node != null) {
            if (node.isNumber()) {
                id = node.asLong();
            } else {
                tree.putNull("id");
            }
        }

        if (!inSync) {
            if ("file".equals(type) && id != null) {
                ctx.registerMediaFileDependency(attr, id);
            } else if ("page".equals(type) && linkNode.isTextual()) {
                String guid = pageService.resolvePageGuid(linkNode.asText());
                if (guid != null) {
                    ctx.registerPageDependency(attr, guid);
                }
            }
        }

        JsonNode val = tree.get("nam");
        if (val != null && val.isTextual()) {
            JsonNode naSpec = mapper.readTree(val.asText());
            String naClass = naSpec.hasNonNull("naClass") ? naSpec.get("naClass").asText() : null;
            if ("ncms.asm.am.RichRefAM".equals(naClass)) {
                richRefAM.applyJSONAttributeValue(ctx, attr, naSpec);
                tree.set("nam", tree.textNode(naSpec.toString()));
            }
        }
        val = tree.get("children");
        if (val instanceof ArrayNode) {
            for (JsonNode n : val) {
                if (n instanceof ObjectNode) {
                    saveTree(ctx, attr, (ObjectNode) n, inSync);
                }
            }
        }
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
