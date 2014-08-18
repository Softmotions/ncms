package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.Tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmBreadCrumbsAM implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"breadcrumbs"};

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
        return attr;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Tree bctree = new Tree();
        return bctree;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        return attr;
    }
}
