package com.softmotions.ncms.mtt;

import javax.annotation.Nonnull;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.ncms.NcmsModuleDescriptor;
import com.softmotions.ncms.NcmsModuleDescriptorSupport;
import com.softmotions.ncms.mhttl.HttlMttMethods;
import com.softmotions.ncms.mtt.http.MttHttpModule;
import com.softmotions.ncms.mtt.tp.MttTpRS;
import com.softmotions.ncms.mtt.tp.MttTpService;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MttModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MttRulesRS.class).in(Singleton.class);
        bind(MttTpRS.class).in(Singleton.class);
        bind(MttTpService.class).in(Singleton.class);
        install(new MttHttpModule());


        Multibinder.newSetBinder(binder(), NcmsModuleDescriptor.class)
                   .addBinding().toInstance(new NcmsModuleDescriptorSupport() {
            @Override
            @Nonnull
            public Class<? extends AbstractModule> getModuleClass() {
                return MttModule.class;
            }

            @Override
            public String[] httlMethodClasses() {
                return new String[]{HttlMttMethods.class.getName()};
            }
        });
    }
}
