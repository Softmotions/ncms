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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class AsmAliasAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmAliasAM.class);

    public static final String[] TYPES = new String[]{"alias"};

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[0-9a-zA-Z\\._\\-/]+$");

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
        return ctx.getAsm().getNavAlias();
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null);
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String alias = val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null;
        while (alias != null && alias.length() > 0 && alias.charAt(0) == '/') {
            alias = alias.substring(1);
        }
        attr.setEffectiveValue(alias);
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String alias = val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null;
        if (alias != null) {
            if (!ALIAS_PATTERN.matcher(alias).matches()) {
                throw new NcmsMessageException(messages.get("ncms.asm.alias.non.allowed.symbols"), true);
            }
            while (alias.length() > 0 && alias.charAt(0) == '/') {
                alias = alias.substring(1);
            }
            if (!adao.asmCheckUniqueAlias(alias, ctx.getAsmId())) {
                throw new NcmsMessageException(messages.get("ncms.asm.alias.non.unique"), true);
            }
        }
        adao.asmUpdateAlias(ctx.getAsmId(), alias);
    }
}
