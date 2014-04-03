package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.AbstractIndexedCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembly.
 * This class is not thread safe for concurrent updating.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Asm implements Serializable {

    Long id;

    String name;

    String description;

    AsmCore core;

    String options;

    AsmOptions parsedOptions;

    List<Asm> parents;

    AttrsList attributes;

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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
        this.parsedOptions = null;
    }

    public AsmOptions getParsedOptions() {
        String opts = this.options;
        if (opts == null) {
            return null;
        }
        this.parsedOptions = new AsmOptions(opts);
        return parsedOptions;
    }

    public AsmCore getCore() {
        return core;
    }

    public void setCore(AsmCore core) {
        this.core = core;
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
        return (attributes != null) ? attributes.getIndex().get(name) : null;
    }

    public Collection<AsmAttribute> getAttributes() {
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

        public AttrsList() {
        }

        protected AttrsList(int size) {
            super(size);
        }

        public AttrsList cloneShallow() {
            AttrsList nlist = new AttrsList(size());
            for (AsmAttribute attr : this) {
                nlist.add(attr.cloneDeep());
            }
            return nlist;
        }
    }

    /**
     * Perform deep clone of this assembly.
     * Cloned parents are cached in <c></c>.
     */
    public Asm cloneDeep(Map<String, Asm> cloneContext) {
        Asm asm = new Asm();
        asm.id = id;
        asm.name = name;
        asm.description = description;
        asm.options = options;
        asm.core = (core != null) ? core.cloneDeep() : null;
        asm.attributes = (asm.attributes != null) ? asm.attributes.cloneShallow() : null;
        if (getParents() != null) {
            asm.parents = new ArrayList<>(getParents().size());
            for (Asm parent : getParents()) {
                Asm clonedParent = cloneContext.get(parent.name);
                if (clonedParent == null) {
                    clonedParent = parent.cloneDeep(cloneContext);
                    cloneContext.put(clonedParent.getName(), clonedParent);
                }
                asm.parents.add(clonedParent);
            }
        }
        return asm;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asm asm = (Asm) o;
        return name.equals(asm.name);
    }

    public int hashCode() {
        return name.hashCode();
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
