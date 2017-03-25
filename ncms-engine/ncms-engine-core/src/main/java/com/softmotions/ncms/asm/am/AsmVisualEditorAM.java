package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.jaxrs.BadRequestException;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@Path("adm/am/ve")
@Produces("application/json;charset=UTF-8")
public class AsmVisualEditorAM extends AsmAttributeManagerSupport {

    public static final String TYPE = "ve";

    private static final Logger log = LoggerFactory.getLogger(AsmVisualEditorAM.class);

    private static final String[] TYPES = new String[]{TYPE};

    private static final String CACHED_SECTIONS_UD_KEY = "AsmVisualEditorAM.sections";

    private final ObjectMapper mapper;

    private final AsmDAO adao;

    private final PageService pageService;

    private final NcmsEventBus ebus;


    @Inject
    public AsmVisualEditorAM(ObjectMapper mapper,
                             PageService pageService,
                             AsmDAO adao,
                             NcmsEventBus ebus) {
        this.mapper = mapper;
        this.pageService = pageService;
        this.adao = adao;
        this.ebus = ebus;
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


    @Nullable
    public String getSection(AsmRendererContext ctx, AsmAttribute attr, String sectionName) {
        if (!TYPE.equals(attr.getType())) {
            throw new IllegalArgumentException("Passed assembly attribute must be of 've' type");
        }
        Map<String, String> cachedSections = ctx.getUserData(CACHED_SECTIONS_UD_KEY);
        if (cachedSections != null) {
            return cachedSections.get(sectionName);
        }
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            return null;
        }
        cachedSections = new HashMap<>();
        ctx.setUserData(CACHED_SECTIONS_UD_KEY, cachedSections);
        try (JsonParser parser = mapper.getFactory().createParser(attr.getEffectiveValue())) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                return null;
            }
            while (parser.nextToken() == JsonToken.START_ARRAY) {
                String sname = parser.nextTextValue();
                String sval = parser.nextTextValue();
                if (sname != null && sval != null) {
                    cachedSections.put(sname, sval);
                }
                parser.nextToken(); //should be END_ARRAY
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return cachedSections.get(sectionName);
    }

    @PUT
    @Path("/save")
    @Consumes("application/json")
    @Transactional
    public void saveSection(@Context HttpServletRequest req,
                            ObjectNode spec) throws Exception {

        String sectionName = spec.path("section").asText(null);
        if (sectionName == null) {
            throw new BadRequestException();
        }
        int ind = sectionName.indexOf(':');
        if (ind == -1) {
            throw new BadRequestException();
        }
        long asmId = Long.parseLong(sectionName.substring(0, ind));
        ebus.unlockOnTxFinish(Asm.acquireLock(asmId));
        sectionName = sectionName.substring(ind + 1);
        String html = spec.path("html").asText().trim();

        Asm asm = adao.asmSelectById(asmId);
        if (asm == null) {
            throw new NotFoundException();
        }
        if (!pageService.getPageSecurityService().canEdit2(asm, req)) {
            throw new UnauthorizedException();
        }
        AsmAttribute attr = asm.getUniqueAttributeByType(TYPE);
        if (attr == null) {
            AsmAttribute eattr = asm.getUniqueEffectiveAttributeByType(TYPE);
            if (eattr == null) {
                log.info("Saving visual section on assembly with missing 've' attribute. " +
                         "Asm: {}, Section: {}", asmId, sectionName);
                attr = new AsmAttribute(UUID.randomUUID().toString(), "ve", null);
            } else {
                attr = eattr.cloneDeep();
                attr.setValue(null);
                //noinspection ConstantConditions
                attr.setId(null);
            }
        }
        attr.setAsmId(asmId);
        if (StringUtils.isBlank(attr.getEffectiveValue())
            || attr.getEffectiveValue().trim().charAt(0) != '[') {
            attr.setEffectiveValue("[]");
        }
        ArrayNode snode = null;
        ArrayNode sections = (ArrayNode) mapper.readTree(attr.getEffectiveValue());
        for (int i = 0; i < sections.size(); ++i) {
            ArrayNode s = (ArrayNode) sections.get(i);
            if (sectionName.equals(s.path(0).asText())) {
                snode = s;
                break;
            }
        }
        if (snode == null) {
            snode = mapper.createArrayNode();
            sections.add(snode);
        }
        snode.removeAll();
        snode.add(sectionName);
        snode.add(html);
        attr.setEffectiveValue(mapper.writeValueAsString(sections));
        adao.asmUpsertAttribute(attr);
        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, asmId, req)
                                         .hint("veditor", true));
    }
}
