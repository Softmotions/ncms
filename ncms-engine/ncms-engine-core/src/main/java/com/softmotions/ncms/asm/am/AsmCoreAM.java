package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

/**
 * Page assembly core manager.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmCoreAM extends AsmFileAttributeManagerSupport {

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
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        return "";
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, @Nullable JsonNode opts) throws Exception {
        String location = null;
        if (val != null) {
            location = val.path("value").asText(null);
        } else if (opts != null) {
            location = opts.path("value").asText(null);
        }
        location = getRawLocation(location);
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

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {

        Asm asm = adao.asmSelectById(ctx.getAsmId());
        if (asm == null) {
            return attr;
        }
        AsmCore core = asm.getCore();
        if (core == null) {
            return attr;
        }
        String nlocation = translateClonedFile(reader, core.getLocation(), fmap);
        if (nlocation == null) {
            return attr;
        }
        ObjectNode node = ctx.getMapper().createObjectNode();
        node.put("value", nlocation);
        attributePersisted(ctx, attr, node, null);
        return attr;
    }
}
