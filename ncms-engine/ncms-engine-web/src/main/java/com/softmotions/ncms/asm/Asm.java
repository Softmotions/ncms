package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.AbstractIndexedCollection;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Assembly object.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Asm implements Serializable {

    Long id;

    String name;

    String description;

    AsmCore core;

    List<Asm> parents;

    AttrsList attributes;

    String options;

    public Asm() {
    }

    public Asm(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Asm(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AsmCore getCore() {
        return core;
    }

    public void setCore(AsmCore core) {
        this.core = core;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public AsmCore getEffectiveCore() {
        AsmCore c = getCore();
        if (c != null || getParents() == null) {
            return c;
        }
        for (final Asm p : getParents()) {
            c = p.getEffectiveCore();
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    public List<Asm> getParents() {
        return parents;
    }

    public void setParents(List<Asm> parents) {
        this.parents = parents;
    }

    public AsmAttribute getEffectiveAttribute(String name) {
        AsmAttribute attr = getAttribute(name);
        if (attr != null || getParents() == null) {
            return attr;
        }
        for (final Asm p : getParents()) {
            attr = p.getEffectiveAttribute(name);
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }

    public AsmAttribute getAttribute(String name) {
        return attributes != null ? attributes.getIndex().get(name) : null;
    }

    public Collection<AsmAttribute> getAttributes() {
        if (attributes == null) {
            attributes = new AttrsList();
        }
        return attributes;
    }

    public Collection<String> getEffectiveAttributeNames() {
        final Set<String> anames = new HashSet<>();
        if (attributes != null) {
            for (final AsmAttribute a : attributes) {
                anames.add(a.getName());
            }
        }
        if (getParents() != null) {
            for (final Asm p : getParents()) {
                anames.addAll(p.getEffectiveAttributeNames());
            }
        }
        return anames;
    }

    public Collection<AsmAttribute> getEffectiveAttributes() {
        final Set<AsmAttribute> attrs = new HashSet<>();
        if (attributes != null) {
            for (final AsmAttribute a : attributes) {
                attrs.add(a);
            }
        }
        if (getParents() != null) {
            for (final Asm p : getParents()) {
                attrs.addAll(p.getEffectiveAttributes());
            }
        }
        return attrs;
    }

    public static class AttrsList extends AbstractIndexedCollection<String, AsmAttribute> {
        protected String getElementKey(AsmAttribute el) {
            return el.getName();
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Asm{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", options='").append(options).append('\'');
        sb.append(", core=").append(core);
        sb.append(", description='").append(description).append('\'');
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }
}
