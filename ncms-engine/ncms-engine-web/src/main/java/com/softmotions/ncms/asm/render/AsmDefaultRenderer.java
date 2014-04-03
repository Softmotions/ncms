package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmCore;

import com.google.inject.Singleton;

import java.io.Writer;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmDefaultRenderer implements AsmRenderer {


    public void render(AsmRendererContext ctx, Writer out) throws AsmRenderingException {
        Asm asm = ctx.getContextAsm();
        AsmCore core = asm.getEffectiveCore();


    }
}
