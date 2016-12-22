package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface AsmAttributeManager {

    @Nonnull
    String[] getSupportedAttributeTypes();

    /**
     * Return `true` if assembly can have only one or zero
     * attributes of this type.
     */
    boolean isUniqueAttribute();

    @Nonnull
    AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     Asm page,
                                     Asm template,
                                     @Nullable AsmAttribute tmplAttr,
                                     AsmAttribute attr) throws Exception;

    @Nullable
    Object[] fetchFTSData(AsmAttribute attr);

    @Nullable
    Object renderAsmAttribute(AsmRendererContext ctx,
                              String attrname,
                              Map<String, String> options) throws AsmRenderingException;


    @Nonnull
    AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                       AsmAttribute attr,
                                       JsonNode val) throws Exception;

    @Nonnull
    AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                     AsmAttribute attr,
                                     JsonNode val) throws Exception;

    /**
     * Change assembly attribute
     * If assembly cloned and page
     * files are copied to another place
     *
     * @param ctx
     * @param attr
     * @param fmap `old file id => new file id` mapping
     * @throws Exception
     */
    @Nonnull
    AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                      AsmAttribute attr,
                                      Map<Long, Long> fmap) throws Exception;


    void attributePersisted(AsmAttributeManagerContext ctx,
                            AsmAttribute attr,
                            @Nullable JsonNode val,
                            @Nullable JsonNode opts) throws Exception;
}
