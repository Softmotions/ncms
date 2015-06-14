package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.PageRS;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

/**
 * Main(index) page marker template attribute.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmMainPageAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmMainPageAM.class);

    public static final String[] TYPES = new String[]{"mainpage"};

    private PageRS pageRS;

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Inject
    public AsmMainPageAM(PageRS pageRS) {
        this.pageRS = pageRS;
    }

    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {
        return attr;
    }

    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        return null;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions old = new AsmOptions(attr.getOptions());
        AsmOptions opts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, opts, "lang", "enabled");
        attr.setOptions(opts.toString());
        if (!Objects.equals(old.get("lang"), opts.get("lang")) ||
            !Objects.equals(old.get("enabled"), opts.get("enabled"))) {
            ctx.setUserData("reload", Boolean.TRUE);
        }
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {
        //noinspection ObjectEquality
        if (ctx.getUserData("reload") == Boolean.TRUE) {
            log.info("Trigger index pages reloading");
            pageRS.reloadIndexPages();
        }
    }
}
