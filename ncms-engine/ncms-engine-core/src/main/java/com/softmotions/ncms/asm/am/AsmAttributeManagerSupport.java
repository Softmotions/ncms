package com.softmotions.ncms.asm.am;

import java.util.Map;

import com.softmotions.ncms.asm.AsmAttribute;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class AsmAttributeManagerSupport implements AsmAttributeManager {

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {
        return attr;
    }
}
