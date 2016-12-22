package com.softmotions.ncms.asm.am;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public abstract class AsmAttributeManagerSupport implements AsmAttributeManager {

    @Override
    public boolean isUniqueAttribute() {
        return false;
    }

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
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
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }


    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx,
                                   AsmAttribute attr,
                                   JsonNode val,
                                   JsonNode opts) throws Exception {

    }
}
