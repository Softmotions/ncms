package com.softmotions.ncms.asm;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmCriteria extends CriteriaBase<AsmCriteria> {

    public AsmCriteria(AsmDAO dao, String namespace) {
        super(dao, namespace);
    }

    @Override
    public AsmCriteria onAsm() {
        prefixedBy(null);
        return this;
    }
}
