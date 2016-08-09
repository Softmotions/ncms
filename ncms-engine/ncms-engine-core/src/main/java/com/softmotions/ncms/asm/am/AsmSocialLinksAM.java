package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.SocialLinks;

/**
 * Social links controller
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
public class AsmSocialLinksAM implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"soclinks"};

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
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        AsmOptions opts = new AsmOptions();
        if (attr.getOptions() != null) {
            opts.loadOptions(attr.getOptions());
        } else {
            return null;
        }

        SocialLinks res = new SocialLinks();
        if (opts.getString("facebook") != null) {
            res.setFacebook(opts.getString("facebook"));
        }
        if (opts.getString("twitter") != null) {
            res.setTwitter(opts.getString("twitter"));
        }
        if (opts.getString("vkontakte") != null) {
            res.setVkontakte(opts.getString("vkontakte"));
        }
        return res;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "facebook", "twitter", "vkontakte");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return null;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
