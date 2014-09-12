package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmBumpOrdinalAM extends AsmBooleanAM {

    private static final Logger log = LoggerFactory.getLogger(AsmBumpOrdinalAM.class);

    public static final String[] TYPES = new String[]{"bump"};

    private final AsmDAO adao;

    @Inject
    public AsmBumpOrdinalAM(AsmDAO adao) {
        this.adao = adao;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public String[] prepareFulltextSearchData(AsmAttribute attr) {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) {
        return attr;
    }


    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) {
        JsonNode bval = val.get("value");
        if (bval != null && bval.asBoolean()) {
            adao.bumpAsmOrdinal(ctx.getAsmId());
        }
        return attr;
    }
}
