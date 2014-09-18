package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class AsmAliasAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmAliasAM.class);

    public static final String[] TYPES = new String[]{"alias"};

    private final NcmsMessages messages;

    private final AsmDAO adao;

    @Inject
    public AsmAliasAM(NcmsMessages messages, AsmDAO adao) {
        this.messages = messages;
        this.adao = adao;
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
        return attr == null || attr.getEffectiveValue() == null ? null : attr.getEffectiveValue();
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null);
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null);
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String alias = attr.getEffectiveValue();
        if (alias != null) {
            if (!alias.matches("^[0-9a-zA-Z\\._-]+$")) {
                throw new NcmsMessageException(messages.get("ncms.asm.alias.non.allowed.symbols"), true);
            }
            synchronized (AsmAliasAM.class) {
                if (!adao.asmCheckUniqueAlias(attr.getId(), alias)) {
                    throw new NcmsMessageException(messages.get("ncms.asm.alias.non.unique"), true);
                }
            }
        }
    }
}
