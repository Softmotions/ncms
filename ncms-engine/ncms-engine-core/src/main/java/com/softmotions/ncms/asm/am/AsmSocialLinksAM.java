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
import com.google.inject.Inject;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
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

    final ObjectMapper mapper;

    @Inject
    public AsmSocialLinksAM(ObjectMapper mapper) {
        this.mapper = mapper;
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
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr != null ? attr.getEffectiveValue() : null;
        if (StringUtils.isBlank(value)) {
            return null;
        }
        SocialLinks res = new SocialLinks();
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            String key;
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return null;
            }
            while (parser.nextValue() != null && (key = parser.getCurrentName()) != null) {
                switch (key) {
                    case "facebook":
                        res.setFacebook(parser.getValueAsString());
                        break;
                    case "twitter":
                        res.setTwitter(parser.getValueAsString());
                        break;
                    case "vkontakte":
                        res.setVkontakte(parser.getValueAsString());
                        break;
                    case "buttonFacebook":
                        res.setButtonFacebook(parser.getValueAsBoolean());
                        break;
                    case "buttonTwitter":
                        res.setButtonTwitter(parser.getValueAsBoolean());
                        break;
                    case "buttonVkontakte":
                        res.setButtonVkontakte(parser.getValueAsBoolean());
                        break;
                    case "buttonOdnoklassniki":
                        res.setButtonOdnoklassniki(parser.getValueAsBoolean());
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        applyAttributeValue(ctx, attr, val);
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        JsonNode value = val.get("value");
        if (value != null) {
            attr.setEffectiveValue(value.toString());
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
