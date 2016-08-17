package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

/**
 * Page assembly core manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmCoreAM extends AsmAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmAliasAM.class);

    public static final String[] TYPES = new String[]{"core"};

    private final MediaReader reader;

    private final AsmDAO adao;

    @Inject
    public AsmCoreAM(MediaReader reader, AsmDAO adao) {
        this.reader = reader;
        this.adao = adao;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    @Override
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req, HttpServletResponse resp, Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        return attr;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        return "";
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return null;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {
        String location = null;
        if (val != null) {
            location = val.path("value").asText();
        } else if (opts != null) {
            location = opts.path("value").asText();
        }
        if (StringUtils.isBlank(location)) {
            return;
        }
        MediaResource resource = reader.findMediaResource(location, null);
        if (resource == null) {
            log.error("Failed to locate assembly core resource: {}", location);
            return;
        }
        Asm asm = adao.asmSelectById(ctx.getAsmId());
        if (asm == null) {
            log.error("Unable to find assembly by id: {}", ctx.getAsmId());
            return;
        }
        AsmCore oldCore = asm.getCore();
        if (oldCore != null && location.equals(oldCore.getLocation())) {
            // not need to update assembly core
            return;
        }
        adao.asmSetCore(asm, location);
    }
}
