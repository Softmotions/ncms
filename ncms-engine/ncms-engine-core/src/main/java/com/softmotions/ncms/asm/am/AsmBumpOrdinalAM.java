package com.softmotions.ncms.asm.am;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmDAO;


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmBumpOrdinalAM extends AsmBooleanAM {

    public static final String[] TYPES = new String[]{"bump"};

    private final AsmDAO adao;

    @Inject
    public AsmBumpOrdinalAM(AsmDAO adao) {
        this.adao = adao;
    }

    @Override
    public boolean isUniqueAttribute() {
        return true;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
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
