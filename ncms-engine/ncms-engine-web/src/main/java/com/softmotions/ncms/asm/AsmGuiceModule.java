package com.softmotions.ncms.asm;

import com.softmotions.ncms.asm.render.AsmAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmLoader;
import com.softmotions.ncms.asm.render.AsmRefAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmStringAttributeRenderer;
import com.softmotions.ncms.asm.render.DefaultAsmRenderer;
import com.softmotions.ncms.asm.render.ldrs.AsmClasspathLoader;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmGuiceModule extends AbstractModule {

    protected void configure() {
        bind(AsmDAO.class);
        bind(AsmRenderer.class).to(DefaultAsmRenderer.class);

        Multibinder<AsmAttributeRenderer> attrBinder =
                Multibinder.newSetBinder(binder(), AsmAttributeRenderer.class);
        attrBinder.addBinding().to(AsmStringAttributeRenderer.class);
        attrBinder.addBinding().to(AsmRefAttributeRenderer.class);

        //Resource loaders
        bind(AsmLoader.class).to(AsmClasspathLoader.class);
    }
}
