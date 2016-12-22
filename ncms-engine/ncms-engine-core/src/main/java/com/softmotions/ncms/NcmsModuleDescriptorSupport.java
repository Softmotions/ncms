package com.softmotions.ncms;

import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

import com.google.common.base.MoreObjects;

/**
 * {@link NcmsModuleDescriptor} basic implementation.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public abstract class NcmsModuleDescriptorSupport implements NcmsModuleDescriptor {

    @Override
    public String[] liquibaseChangeSets() {
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] mybatisExtraMappers() {
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] httlMethodClasses() {
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] httlImportPackages() {
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] httlTemplateFilters() {
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] adminScripts() {
        return EMPTY_STRING_ARRAY;
    }

    public String toString() {
        return MoreObjects
                .toStringHelper(getModuleClass().getName().toString())
                .add("liquibaseChangeSets", Arrays.asList(liquibaseChangeSets()))
                .add("mybatisExtraMappers", Arrays.asList(mybatisExtraMappers()))
                .add("httlMethodClasses", Arrays.asList(httlMethodClasses()))
                .add("httlImportPackages", Arrays.asList(httlImportPackages()))
                .add("httlTemplateFilters", Arrays.asList(httlTemplateFilters()))
                .add("adminScripts", Arrays.asList(adminScripts()))
                .toString();
    }
}
