package com.softmotions.ncms.asm;

import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmResourceResolver;
import com.softmotions.ncms.asm.render.ClasspathAsmResourceResolver;
import com.softmotions.ncms.asm.render.DefaultAsmRenderer;

import com.google.inject.AbstractModule;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmGuiceModule extends AbstractModule {

    protected void configure() {
        bind(AsmDAO.class);
        bind(AsmRenderer.class).to(DefaultAsmRenderer.class);
        bind(AsmResourceResolver.class).to(ClasspathAsmResourceResolver.class);
    }
}
