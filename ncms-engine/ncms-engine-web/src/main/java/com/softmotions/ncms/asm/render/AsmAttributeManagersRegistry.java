package com.softmotions.ncms.asm.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmAttributeManagersRegistry {


    /**
     * Set of attribute renderers
     */
    final Set<AsmAttributeManager> attributeManagers;

    /**
     * Set type => AsmAttributeManager
     */
    final Map<String, AsmAttributeManager> typeAttributeManagersMap;


    @Inject
    public AsmAttributeManagersRegistry(Set<AsmAttributeManager> attributeManagers) {
        this.attributeManagers = attributeManagers;
        this.typeAttributeManagersMap = new HashMap<>();
        for (final AsmAttributeManager ar : attributeManagers) {
            for (final String atype : ar.getSupportedAttributeTypes()) {
                typeAttributeManagersMap.put(atype, ar);
            }
        }
    }

    public AsmAttributeManager getByType(String type) {
        return typeAttributeManagersMap.get(type);
    }

    public Collection<AsmAttributeManager> getAll() {
        return attributeManagers;
    }
}
