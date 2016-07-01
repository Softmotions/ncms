package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmFileRefAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmFileRefAM.class);

    public static final String[] TYPES = new String[]{"fileref"};

    private final MediaReader reader;

    private final NcmsMessages messages;

    @Inject
    public AsmFileRefAM(MediaReader reader, NcmsMessages messages) {
        this.reader = reader;
        this.messages = messages;
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
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        AsmOptions opts = new AsmOptions();
        if (attr.getOptions() != null) {
            opts.loadOptions(attr.getOptions());
        }
        String location = attr.getEffectiveValue();
        if (BooleanUtils.toBoolean(opts.getString("asLocation"))) {
            return location;
        }
        boolean asTemplate = BooleanUtils.toBoolean(opts.getString("asTemplate"));
        if (asTemplate) {
            AsmRenderer renderer = ctx.getRenderer();
            StringWriter out = new StringWriter(1024);
            try {
                renderer.renderTemplate(location, ctx, out);
            } catch (IOException e) {
                throw new AsmRenderingException("Failed to render template: '" + location + '\'' +
                                                " asm: " + ctx.getAsm().getName() + " attribute: " + attrname, e);
            }
            return out.toString();
        }

        MediaResource resource = reader.findMediaResource(location, ctx.getLocale());
        if (resource == null) {
            return null;
        }
        if (!CTypeUtils.isTextualContentType(resource.getContentType())) {
            log.warn("Only textual resources allowed. Location: {} content-type: {} asm: {} attribute: {}",
                     location, resource.getContentType(), asm.getName(), attrname);
            return null;
        }
        StringWriter sw = new StringWriter(1024);
        try {
            resource.writeTo(sw);
        } catch (IOException e) {
            throw new AsmRenderingException("Failed to load resource: '" + location + '\'' +
                                            " asm: " + ctx.getAsm().getName() + " attribute: " + attrname, e);
        }
        return sw.toString();
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions opts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, opts,
                                        "asLocation",
                                        "asTemplate");
        attr.setOptions(opts.toString());
        applyAttributeValue(ctx, attr, val);
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String location = val.hasNonNull("value") ? val.get("value").asText().trim() : null;
        attr.setEffectiveValue(location);
        if (!StringUtils.isBlank(location)) {
            MediaResource resource = reader.findMediaResource(location, messages.getLocale(ctx.getRequest()));
            if (resource != null) {
                ctx.registerMediaFileDependency(attr, resource.getId());
            }
        }
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
