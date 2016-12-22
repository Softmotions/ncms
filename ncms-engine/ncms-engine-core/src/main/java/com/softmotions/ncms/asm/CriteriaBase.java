package com.softmotions.ncms.asm;

import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
class CriteriaBase<T extends CriteriaBase> extends MBCriteriaQuery<T> {

    CriteriaBase(MBDAOSupport dao, String namespace) {
        super(dao, namespace);
    }

    public T onAsm() {
        prefixedBy("ASM_");
        return (T) this;
    }

    public T onAsmAttribute() {
        prefixedBy("ATTR_");
        return (T) this;
    }

    public T onAsmCore() {
        prefixedBy("CORE_");
        return (T) this;
    }
}
