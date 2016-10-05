package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.weboot.i18n.I18n;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@Path("adm/am/ve")
@Produces("application/json;charset=UTF-8")
public class AsmVisualEditorAM extends AsmAttributeManagerSupport {

    public static final String TYPE = "ve";

    private static final String[] TYPES = new String[]{TYPE};

    private final ObjectMapper mapper;

    private final PageService pageService;

    private final AsmDAO adao;

    private final I18n i18n;

    @Inject
    public AsmVisualEditorAM(ObjectMapper mapper,
                             PageService pageService,
                             AsmDAO adao,
                             I18n i18n) {
        this.mapper = mapper;
        this.pageService = pageService;
        this.adao = adao;
        this.i18n = i18n;
    }

    @Override
    public boolean isUniqueAttribute() {
        return true;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Nullable
    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx,
                                     String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        return null;
    }


    public String getSection(AsmRendererContext ctx, String sectionName) {
        // todo
        return null;
    }


    @PUT
    @Path("/save")
    @Consumes("application/json")
    public void saveSection(@Context HttpServletRequest req,
                            ObjectNode spec) {

        // todo
    }



}
