package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

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
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;

/**
 * File reference attribute manager.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmFileRefAM extends AsmFileAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmFileRefAM.class);

    public static final String[] TYPES = new String[]{"fileref"};

    private final MediaReader reader;

    @Inject
    public AsmFileRefAM(MediaReader reader) {
        this.reader = reader;
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
        AsmOptions opts = new AsmOptions();
        if (attr.getOptions() != null) {
            opts.loadOptions(attr.getOptions());
        }
        String location = getRawLocation(attr.getEffectiveValue());
        if (location == null || BooleanUtils.toBoolean(opts.getString("asLocation"))) {
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
        String location = StringUtils.trimToNull(val.path("value").asText(null));
        String rawLocation = getRawLocation(location);
        attr.setEffectiveValue(location);
        if (!StringUtils.isBlank(rawLocation)) {
            MediaResource resource = reader.findMediaResource(rawLocation, ctx.getLocale());
            if (resource != null && resource.getId() != null) {
                ctx.registerFileDependency(attr, resource.getId());
            }
        }
        return attr;
    }

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {

        String nlocation = translateClonedFile(reader, attr.getEffectiveValue(), fmap);
        if (nlocation == null) {
            nlocation = getRawLocation(attr.getEffectiveValue());
            if (nlocation != null) {
                MediaResource res = reader.findMediaResource(nlocation, null);
                if (res != null && res.getId() != null) {
                    ctx.registerFileDependency(attr, res.getId());
                }
            }
            return attr;
        }
        ObjectNode node = ctx.getMapper().createObjectNode();
        node.put("value", nlocation);
        return applyAttributeValue(ctx, attr, node);
    }
}
