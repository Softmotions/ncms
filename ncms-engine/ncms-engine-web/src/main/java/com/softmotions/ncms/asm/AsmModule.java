package com.softmotions.ncms.asm;

import com.softmotions.ncms.asm.render.AsmAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmRefAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmResourceAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.asm.render.AsmStringAttributeRenderer;
import com.softmotions.ncms.asm.render.DefaultAsmRenderer;
import com.softmotions.ncms.asm.render.ldrs.AsmClasspathResourceLoader;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmModule extends AbstractModule {

    protected void configure() {
        bind(AsmDAO.class);
        bind(AsmRenderer.class).to(DefaultAsmRenderer.class);

        Multibinder<AsmAttributeRenderer> attrBinder =
                Multibinder.newSetBinder(binder(), AsmAttributeRenderer.class);
        attrBinder.addBinding().to(AsmStringAttributeRenderer.class);
        attrBinder.addBinding().to(AsmRefAttributeRenderer.class);
        attrBinder.addBinding().to(AsmResourceAttributeRenderer.class);

        //Resource loaders
        bind(AsmResourceLoader.class).to(AsmClasspathResourceLoader.class);


        //Asm REST
        bind(AsmRS.class).in(Singleton.class);
    }
}
