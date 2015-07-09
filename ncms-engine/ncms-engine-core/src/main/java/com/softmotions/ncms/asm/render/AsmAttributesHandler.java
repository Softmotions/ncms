package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;

import java.util.Collection;

/**
 * Modify the assembly attribute set
 * loaded in admin edit page.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributesHandler {

    Collection<AsmAttribute> onLoadedAttributes(Asm asm, Collection<AsmAttribute> attrs) throws Exception;

}
