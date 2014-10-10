package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.Tree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

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

    private final ObjectMapper mapper;

    private final AsmRichRefAM richRefAM;

    private final PageService pageService;



    @Inject
    public AsmTreeAM(ObjectMapper mapper,
                     AsmRichRefAM richRefAM,
                     PageService pageService) {
        this.mapper = mapper;
        this.richRefAM = richRefAM;
        this.pageService = pageService;
    }


    @PUT
    @Path("/sync")
    @Consumes("application/json")
    public ObjectNode syncWith(ObjectNode spec) {
        ObjectNode tree = mapper.createObjectNode();
        Long srcPage;
        Long tgtPage;
        String attrname;
        JsonNode n = spec.get("srcPage");
        srcPage = (n != null && n.isNumber()) ? n.longValue() : null;
        n = spec.get("tgtPage");
        tgtPage = (n != null && n.isNumber()) ? n.longValue() : null;
        n = spec.get("attrname");
        attrname = n.asText(null);
        if (srcPage == null || tgtPage == null || attrname == null) {
            throw new BadRequestException("");
        }
        //Asm srcAsm =




        return tree;
    }


    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            try {
                attr.setEffectiveValue(mapper.writeValueAsString(new Tree(attr.getLabel() != null ? attr.getLabel() : "root")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            //todo sync?
        }
        return attr;
    }

    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return new Tree("root");
        }
        Tree tree;
        try {
            tree = initTree(mapper.reader(Tree.class).readValue(attr.getEffectiveValue()), ctx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Long syncWithId = tree.getSyncWithId();
        if (syncWithId != null) {
            CachedPage syncPage = pageService.getCachedPage(syncWithId, true);
            if (syncPage == null) {
                log.warn("Failed to find referenced page with id: " + syncWithId);
                return tree;
            }
            Asm syncAsm = syncPage.getAsm();
            AsmAttribute syncAttr = syncAsm.getEffectiveAttribute(attrname);
            if (syncAttr == null || !attr.getType().equals(syncAttr.getType())) {
                log.warn("Found incompatible sync attributes. " +
                         "Source asm: " + syncWithId +
                         " attr name: " + attrname +
                         " sync attr: " + syncAttr);
                return tree;
            }
            tree = (Tree) ctx.renderAttribute(syncAsm, attrname, Collections.EMPTY_MAP);
        }
        return tree;
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

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts);
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        ObjectNode tree = (ObjectNode) val;
        if (tree != null) {
            try {
                saveTree(ctx, attr, tree);
            } catch (IOException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
            attr.setEffectiveValue(tree.toString());
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    private void saveTree(AsmAttributeManagerContext ctx, AsmAttribute attr, ObjectNode tree) throws IOException {
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
        if ("file".equals(type) && id != null) {
            ctx.registerMediaFileDependency(attr, id);
        } else if ("page".equals(type) && linkNode.isTextual()) {
            String guid = pageService.resolvePageGuid(linkNode.asText());
            if (guid != null) {
                ctx.registerPageDependency(attr, guid);
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
                    saveTree(ctx, attr, (ObjectNode) n);
                }
            }
        }
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }
}
