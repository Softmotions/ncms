package com.softmotions.ncms.asm.am;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
@SuppressWarnings("unchecked")
public class DefaultAsmAttributeManagersRegistry implements AsmAttributeManagersRegistry {

    /**
     * Set of attribute renderers
     */
    final Set<AsmAttributeManager> attributeManagers;

    /**
     * Set type => AsmAttributeManager
     */
    final Map<String, AsmAttributeManager> typeAttributeManagersMap;


    @Inject
    public DefaultAsmAttributeManagersRegistry(Set<AsmAttributeManager> attributeManagers) {
        this.attributeManagers = attributeManagers;
        this.typeAttributeManagersMap = new HashMap<>();
        for (final AsmAttributeManager ar : attributeManagers) {
            for (final String atype : ar.getSupportedAttributeTypes()) {
                typeAttributeManagersMap.put(atype, ar);
            }
        }
    }

    @Override
    public <T extends AsmAttributeManager> T getByType(String type) {
        if (type == null) {
            return null;
        }
        //noinspection unchecked
        return (T) typeAttributeManagersMap.get(type);
    }

    @Override
    public Collection<AsmAttributeManager> getAll() {
        return Collections.unmodifiableCollection(attributeManagers);
    }
}
