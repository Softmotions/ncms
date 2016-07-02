package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.RichRef;

/**
 * Page reference attribute manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmPageRefAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmFileRefAM.class);

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
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        String link = attr.getEffectiveValue().trim();
        String rawLink = link;
        String name = null;
        int ind = link.indexOf('|');
        if (ind != -1) {
            if (ind < link.length() - 1) {
                name = link.substring(ind + 1).trim();
            }
            link = link.substring(0, ind).trim();
        }
        link = pageService.resolveResourceLink(link);
        return new RichRef(name, link, rawLink);
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
        ctx.clearPageDeps(attr);
        String location = val.hasNonNull("value") ? val.get("value").asText().trim() : null;
        String guid = pageService.resolvePageGuid(location);
        if (guid != null) {
            ctx.registerPageDependency(attr, guid);
        }
        attr.setEffectiveValue(location);
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr,
                                   JsonNode val, JsonNode opts) throws Exception {

    }
}
