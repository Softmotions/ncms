package com.softmotions.ncms.asm.am;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.RichRef;

/**
 * Page reference attribute manager.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmPageRefAM extends AsmAttributeManagerSupport {

    public static final String[] TYPES = new String[]{"pageref"};

    private final PageService pageService;

    @Inject
    public AsmPageRefAM(PageService pageService) {
        this.pageService = pageService;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        return new RichRef(attr.getEffectiveValue().trim(), pageService);
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                              AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions opts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, opts,
                                        "allowExternalLinks");
        attr.setOptions(opts.toString());
        applyAttributeValue(ctx, attr, val);
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr, JsonNode val) throws Exception {
        String location = val.hasNonNull("value") ? val.get("value").asText().trim() : null;
        String guid = pageService.resolvePageGuid(location);
        if (guid != null) {
            ctx.registerPageDependency(attr, guid);
        }
        attr.setEffectiveValue(location);
        return attr;
    }
}
