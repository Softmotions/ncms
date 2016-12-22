package com.softmotions.ncms.asm.render;

import java.util.Collection;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;

/**
 * Modify the assembly attribute set
 * loaded in admin edit page.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface AsmAttributesHandler {

    Collection<AsmAttribute> onLoadedAttributes(Asm asm, Collection<AsmAttribute> attrs) throws Exception;

}
