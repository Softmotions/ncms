package com.softmotions.ncms.asm.am;

import java.util.Collection;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributeManagersRegistry {

     <T extends AsmAttributeManager> T getByType(String type);

    Collection<AsmAttributeManager> getAll();

}
