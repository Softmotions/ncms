package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.Table;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class AsmTableAM extends AsmAttributeManagerSupport {

    public static final String[] TYPES = new String[]{"table"};

    private final ObjectMapper mapper;

    @Inject
    public AsmTableAM(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx,
                                     String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || StringUtils.isBlank(attr.getEffectiveValue())) {
            return new Table();
        }
        try (JsonParser parser = mapper.getFactory().createParser(attr.getEffectiveValue())) {
            return new Table(parser);
        } catch (IOException e) {
            throw new AsmRenderingException(e);
        }
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                              AsmAttribute attr,
                                              JsonNode val) throws Exception {
        attr.setValue(mapper.writeValueAsString(val));
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr,
                                            JsonNode val) throws Exception {
        return applyAttributeOptions(ctx, attr, val);
    }
}
