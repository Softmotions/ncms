package com.softmotions.ncms.asm.render.httl;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.ncms.asm.render.AsmTemplateEngineAdapter;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmTemplateEngineHttlModule extends AbstractModule {

    protected void configure() {
        Multibinder<AsmTemplateEngineAdapter> teBinder =
                Multibinder.newSetBinder(binder(), AsmTemplateEngineAdapter.class);
        teBinder.addBinding().to(AsmTemplateEngineHttlAdapter.class);
    }
}
