package com.softmotions.ncms.asm.am;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmBumpOrdinalAM extends AsmBooleanAM {

    private static final Logger log = LoggerFactory.getLogger(AsmBumpOrdinalAM.class);

    public static final String[] TYPES = new String[]{"bump"};

    private final AsmDAO adao;

    @Inject
    public AsmBumpOrdinalAM(AsmDAO adao) {
        this.adao = adao;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }


    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        JsonNode bval = val.get("value");
        if (bval != null && bval.asBoolean()) {
            adao.bumpAsmOrdinal(ctx.getAsmId());
        }
        return attr;
    }
}
