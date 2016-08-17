package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributeManager {

    String[] getSupportedAttributeTypes();

    AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     Asm page,
                                     Asm template,
                                     AsmAttribute tmplAttr,
                                     AsmAttribute attr) throws Exception;

    Object[] fetchFTSData(AsmAttribute attr);

    Object renderAsmAttribute(AsmRendererContext ctx,
                              String attrname,
                              Map<String, String> options) throws AsmRenderingException;

    AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                       AsmAttribute attr,
                                       JsonNode val) throws Exception;

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
    AsmAttribute handleAssemblyCloned(
            AsmAttributeManagerContext ctx,
            AsmAttribute attr,
            Map<Long, Long> fmap) throws Exception;


    void attributePersisted(AsmAttributeManagerContext ctx,
                            AsmAttribute attr,
                            JsonNode val,
                            JsonNode opts) throws Exception;

}
