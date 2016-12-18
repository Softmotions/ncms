package com.softmotions.ncms.js;

import javax.annotation.Nonnull;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.ncms.NcmsModuleDescriptor;
import com.softmotions.ncms.NcmsModuleDescriptorSupport;

/**
 * Scripts concatenation and minification in HTTL templates
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class JsModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder.newSetBinder(binder(), NcmsModuleDescriptor.class)
                   .addBinding().toInstance(new NcmsModuleDescriptorSupport() {

            @Override
            @Nonnull
            public Class<? extends AbstractModule> getModuleClass() {
                return JsModule.class;
            }

            @Override
            public String[] httlMethodClasses() {
                return new String[]{HttlJsMethods.class.getName()};
            }
        });

    }

    public static class NcmsJsService {

    }
}
