package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.RichRef;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmRichRefAM extends AsmAttributeManagerSupport {

    private static final String[] TYPES = new String[]{"richref"};

    private final AsmImageAM imageAM;

    private final ObjectMapper mapper;

    private final MediaReader reader;

    private final PageService pageService;

    @Inject
    public AsmRichRefAM(AsmImageAM imageAM,
                        ObjectMapper mapper,
                        MediaReader reader,
                        PageService pageService) {
        this.imageAM = imageAM;
        this.mapper = mapper;
        this.reader = reader;
        this.pageService = pageService;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public RichRef renderAsmAttribute(AsmRendererContext ctx, ObjectNode node) throws AsmRenderingException {
        JsonNode n;
        String link = null;
        String rawLink = null;
        String name = null;
        String description = null;
        String style = null;
        String style2 = null;
        String style3 = null;
        Image image = null;
        if (node.hasNonNull("image")) {
            image = imageAM.renderAsmAttribute(ctx, (ObjectNode) node.get("image"));
        }
        n = node.get("link");
        if (n != null && n.isTextual()) {
            link = n.asText().trim();
            rawLink = link;
            int ind = link.indexOf('|');
            if (ind != -1) {
                if (ind < link.length() - 1) {
                    name = link.substring(ind + 1).trim();
                }
                link = link.substring(0, ind).trim();
            }
            link = pageService.resolveResourceLink(link);
        }
        if (node.hasNonNull("description")) {
            description = node.get("description").asText();
        }
        if (node.hasNonNull("style")) {
            style = node.get("style").asText();
        }
        if (node.hasNonNull("style2")) {
            style2 = node.get("style2").asText();
        }
        if (node.hasNonNull("style3")) {
            style3 = node.get("style3").asText();
        }
        //noinspection ConstantConditions
        return new RichRef(name, link, rawLink,
                           description, image, style,
                           style2, style3);
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx,
                                     String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        // {
        // "image":{"id":561,"options":{"restrict":"false","width":"693","skipSmall":"false","resize":"true"}},
        // "link":"page:9036a644c27a6479490ba1fef54f86af | Контакты","description":"testf"
        // }
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr != null ? attr.getEffectiveValue() : null;
        if (StringUtils.isBlank(value)) {
            return null;
        }
        RichRef res;
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(value);
            res = renderAsmAttribute(ctx, node);
        } catch (IOException e) {
            throw new AsmRenderingException(e);
        }
        return res;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                              AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts);
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr, JsonNode val) throws Exception {
        if (val == null) {
            attr.setEffectiveValue(null);
            return attr;
        }
        attr.setEffectiveValue(mapper.writeValueAsString(applyJSONAttributeValue(ctx, attr, val)));
        return attr;
    }

    public JsonNode applyJSONAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr, JsonNode val) {
        return applyJSONAttributeValue(ctx, attr, val, false);
    }

    public JsonNode applyJSONAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr, JsonNode val,
                                            boolean nested) {
        JsonNode n = val.get("image");
        if (n != null) {
            imageAM.applyJSONAttributeValue(ctx, attr, (ObjectNode) n);
        }
        n = val.get("link");
        if (n != null && n.isTextual()) {
            Long fid = reader.getFileIdByResourceSpec(n.asText());
            if (fid != null) {
                ctx.registerFileDependency(attr, fid);
            } else {
                String guid = pageService.resolvePageGuid(n.asText());
                if (guid != null) {
                    ctx.registerPageDependency(attr, guid);
                }
            }
        }
        return val;
    }

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {
        // {
        // "image":{"id":561,"options":{"restrict":"false","width":"693","skipSmall":"false","resize":"true"}},
        // "link":"page:9036a644c27a6479490ba1fef54f86af | Контакты","description":"testf"
        // }
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            return attr;
        }
        ObjectNode node = (ObjectNode) mapper.readTree(attr.getEffectiveValue());
        ObjectNode image = (ObjectNode) node.get("image");
        JsonNode link = node.get("link");

        if (image != null) {
            AsmAttribute iattr = attr.cloneDeep();
            iattr.setEffectiveValue(mapper.writeValueAsString(image));
            imageAM.handleAssemblyCloned(ctx, iattr, fmap);
            node.set("image", mapper.readTree(iattr.getEffectiveValue()));
        }

        if (link != null && link.isTextual()) {
            Long fid = reader.getFileIdByResourceSpec(link.asText());
            if (fid != null) {
                ctx.registerFileDependency(attr, fid);
            } else {
                String guid = pageService.resolvePageGuid(link.asText());
                if (guid != null) {
                    ctx.registerPageDependency(attr, guid);
                }
            }
        }

        attr.setEffectiveValue(mapper.writeValueAsString(node));
        return attr;
    }
}
