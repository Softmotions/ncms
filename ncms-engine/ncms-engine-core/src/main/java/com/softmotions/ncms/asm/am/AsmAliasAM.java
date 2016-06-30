package com.softmotions.ncms.asm.am;

import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;

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
        return ctx.getAsm().getNavAlias();
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null);
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        String alias = val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null;
        while (alias != null && !alias.isEmpty() && alias.charAt(0) == '/') {
            alias = alias.substring(1);
        }
        attr.setEffectiveValue(alias);
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {
        String alias;
        if (val != null) {
            alias = val.hasNonNull("value") ? StringUtils.trimToNull(val.get("value").asText()) : null;
        } else if (opts != null) {
            alias = opts.hasNonNull("value") ? StringUtils.trimToNull(opts.get("value").asText()) : null;
        } else {
            return;
        }
        if (alias != null) {
            if (!ALIAS_PATTERN.matcher(alias).matches()) {
                throw new NcmsMessageException(messages.get("ncms.asm.alias.non.allowed.symbols"), true);
            }
            while (!alias.isEmpty() && alias.charAt(0) == '/') {
                alias = alias.substring(1);
            }
            if (!adao.asmIsUniqueAlias(alias, ctx.getAsmId())) {
                throw new NcmsMessageException(messages.get("ncms.asm.alias.non.unique"), true);
            }
        }
        adao.asmUpdateAlias(ctx.getAsmId(), alias);
    }
}
