package com.softmotions.ncms;

import javax.annotation.Nonnull;

import com.google.inject.AbstractModule;

/**
 * Descriptor for external nCMS module.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface NcmsModuleDescriptor {

    /**
     * Get main module guice class
     */
    @Nonnull
    Class<? extends AbstractModule> getModuleClass();

    ///////////////////////////////////////////////////////////////////////////
    //                            Database                                   //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * List of class path resources
     * of liquibase database changelog files
     */
    @Nonnull
    String[] liquibaseChangeSets();

    /**
     * List of class path resources
     * of mybatis mappers
     */
    @Nonnull
    String[] mybatisExtraMappers();

    ///////////////////////////////////////////////////////////////////////////
    //                             HTTL                                      //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Class names of httl method modules.
     */
    @Nonnull
    String[] httlMethodClasses();

    /**
     * List of import packages for httl modules.
     */
    @Nonnull
    String[] httlImportPackages();

    /**
     * Class names of httl template filters
     */
    @Nonnull
    String[] httlTemplateFilters();

    ///////////////////////////////////////////////////////////////////////////
    //                             JS                                        //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set of JS resources loaded in nCMS admin zone.
     */
    @Nonnull
    String[] adminScripts();

}
