package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.weboot.liquibase.WBLiquibaseExtraConfigSupplier;

/**
 * nCMS integration with softmotions weboot components (https://github.com/Softmotions/softmotions-java-commons).
 * Extra configs for liquibase, mybatis, etc..
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsWBIntegrationModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsWBIntegrationModule.class);

    @Override
    protected void configure() {

        // Liquibase extra configs
        Multibinder.newSetBinder(binder(), WBLiquibaseExtraConfigSupplier.class)
                   .addBinding().to(NcmsWBLiquibaseExtraConfigSupplier.class);
    }


    public static class NcmsWBLiquibaseExtraConfigSupplier implements WBLiquibaseExtraConfigSupplier {

        final Set<NcmsModuleDescriptor> moduleDescriptors;

        @Inject
        public NcmsWBLiquibaseExtraConfigSupplier(Set<NcmsModuleDescriptor> moduleDescriptors) {
            this.moduleDescriptors = moduleDescriptors;
        }

        @Override
        public ConfigSpec[] getConfigSpecs() {
            if (moduleDescriptors.isEmpty()) {
                //noinspection ZeroLengthArrayAllocation
                return new ConfigSpec[0];
            }
            List<ConfigSpec> specList = new ArrayList<>();
            for (NcmsModuleDescriptor md : moduleDescriptors) {
                for (String csFile : md.liquibaseChangeSets()) {
                    specList.add(new ConfigSpec(csFile));
                }
            }
            return specList.toArray(new ConfigSpec[specList.size()]);
        }
    }
}
