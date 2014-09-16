package com.softmotions.ncms.asm.am;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

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

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
        return attr;
    }

    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

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
            //ctx.setNextEscapeSkipping(!BooleanUtils.toBoolean(opts.getString("escape")));
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
            log.warn("Only textual resources allowed. Location: " +
                     location + " content-type: " + resource.getContentType() +
                     " asm: " + asm.getName() + " attribute: " + attrname);
            return null;
        }
        StringWriter sw = new StringWriter(1024);
        try {
            resource.writeTo(sw);
        } catch (IOException e) {
            throw new AsmRenderingException("Failed to load resource: '" + location + '\'' +
                                            " asm: " + ctx.getAsm().getName() + " attribute: " + attrname, e);
        }
        //ctx.setNextEscapeSkipping(!BooleanUtils.toBoolean(opts.getString("escape")));
        return sw.toString();
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions opts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, opts,
                                        "asLocation",
                                        "asTemplate");
        attr.setOptions(opts.toString());
        attr.setEffectiveValue(val.has("value") ? val.get("value").asText() : null);
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String location = val.hasNonNull("value") ? val.get("value").asText().trim() : null;
        attr.setEffectiveValue(location);
        if (location != null) {
            MediaResource resource = reader.findMediaResource(location, messages.getLocale(ctx.getRequest()));
            if (resource != null) {
                ctx.registerMediaFileDependency(attr, resource.getId());
            }
        }
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }
}
