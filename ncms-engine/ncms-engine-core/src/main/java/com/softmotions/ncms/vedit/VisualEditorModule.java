package com.softmotions.ncms.vedit;

import javax.annotation.Nonnull;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.ncms.NcmsModuleDescriptor;
import com.softmotions.ncms.NcmsModuleDescriptorSupport;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class VisualEditorModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder.newSetBinder(binder(), NcmsModuleDescriptor.class)
                   .addBinding().toInstance(new NcmsModuleDescriptorSupport() {

            @Override
            @Nonnull
            public Class<? extends AbstractModule> getModuleClass() {
                return VisualEditorModule.class;
            }

            @Override
            public String[] httlMethodClasses() {
                return new String[]{HttlVisualEditorMethods.class.getName()};
            }

            @Override
            public String[] httlTemplateFilters() {
                return new String[]{HttlVisualEditorFilter.class.getName()};
            }
        });
    }
}
