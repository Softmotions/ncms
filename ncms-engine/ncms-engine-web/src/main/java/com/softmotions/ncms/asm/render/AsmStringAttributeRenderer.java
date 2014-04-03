package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;

import com.google.inject.Singleton;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmStringAttributeRenderer implements AsmAttributeRenderer {

    public void render(AsmAttributeRendererContext ctx, Writer out) throws AsmRenderingException {
        Asm asm = ctx.getContextAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(ctx.getAttributeName());
        if (attr == null || attr.getEffectiveValue() == null) {
            return;
        }
        try {
            out.write(attr.getEffectiveValue());
        } catch (IOException e) {
            throw new AsmRenderingException(attr.toString(asm), e);
        }
    }
}
