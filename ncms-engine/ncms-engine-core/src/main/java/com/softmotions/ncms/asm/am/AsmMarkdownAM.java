package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * Markdown attribute manager.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmMarkdownAM extends AsmAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmMarkdownAM.class);

    public static final String[] TYPES = {"markdown"};

    private final ObjectMapper mapper;

    private final PageService pageService;

    private final NcmsEnvironment env;

    @Inject
    public AsmMarkdownAM(ObjectMapper mapper,
                         PageService pageService,
                         NcmsEnvironment env) {
        this.mapper = mapper;
        this.pageService = pageService;
        this.env = env;
    }

    @Override
    @Nonnull
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    @Nullable
    public Object renderAsmAttribute(AsmRendererContext ctx,
                                     String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        return null;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                              AsmAttribute attr,
                                              JsonNode val) throws Exception {


        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                            AsmAttribute attr,
                                            JsonNode val) throws Exception {

        return attr;
    }


    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {

        return attr;
    }
}
