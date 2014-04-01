package com.softmotions.ncms.asm;

import com.softmotions.commons.weboot.mb.MBCriteriaQuery;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class ASMCriteriaQuery extends MBCriteriaQuery<ASMCriteriaQuery> {

    public ASMCriteriaQuery() {
    }

    public ASMCriteriaQuery(Map<String, Object> params) {
        super(params);
    }

    public ASMCriteriaQuery onAsm() {
        prefixedBy(null);
        return this;
    }

    public ASMCriteriaQuery onAsmAttribute() {
        prefixedBy("ATTR_");
        return this;
    }

}
