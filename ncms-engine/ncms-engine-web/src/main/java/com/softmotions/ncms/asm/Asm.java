package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.AbstractIndexedCollection;
import com.softmotions.commons.cont.KVOptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.apache.commons.lang3.ArrayUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
@SuppressWarnings("unchecked")
@JsonRootName("asm")
@XmlAccessorType(XmlAccessType.NONE)
public class Asm implements Serializable {

    public static final String ASM_HANDLER_CLASS_ATTR_NAME = "NCMS__ASM_HANDLER_CLASS";

    @JsonProperty
    Long id;

    @JsonProperty
    String name;

    @JsonProperty
    String type;

    @JsonProperty
    String description;

    @JsonProperty
    AsmCore core;

    @JsonProperty
    String options;

    KVOptions parsedOptions;

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

    public Asm(String name, AsmCore core) {
        this.name = name;
        this.core = core;
    }

    public Asm(String name, AsmCore core, String description, String options) {
        this.name = name;
        this.core = core;
        this.description = description;
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public KVOptions getParsedOptions() {
        String opts = this.options;
        if (opts == null) {
            return null;
        }
        this.parsedOptions = new KVOptions(opts);
        return parsedOptions;
    }

    public AsmCore getCore() {
        return core;
    }

    public void setCore(AsmCore core) {
        this.core = core;
    }

    @JsonProperty()
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

    public Set<String> getCumulativeParentNames() {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        Set<String> cparents = new HashSet<>();
        for (final Asm p : getParents()) {
            cparents.add(p.getName());
            cparents.addAll(p.getCumulativeParentNames());
        }
        return cparents;

    }

    @JsonProperty()
    public String[] getParentRefs() {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] prefs = new String[plist.size()];
        for (int i = 0; i < prefs.length; ++i) {
            Asm p = parents.get(i);
            prefs[i] = p.getId() + ":" + p.getName();
        }
        return prefs;
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
        return (getAttributes() != null) ? attributes.getIndex().get(name) : null;
    }

    public Collection<AsmAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(AttrsList attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(AsmAttribute attr) {
        if (getAttributes() == null) {
            attributes = new AttrsList();
        }
        attributes.add(attr);
    }

    public void rmAttribute(String name) {
        if (getAttributes() == null || attributes.isEmpty()) {
            return;
        }
        AsmAttribute attr = attributes.getIndex().get(name);
        if (attr != null) {
            attributes.remove(attr);
        }
    }

    public Collection<String> getAttributeNames() {
        List<String> anames = new ArrayList<>(getAttributes().size());
        for (final AsmAttribute a : attributes) {
            anames.add(a.getName());
        }
        return anames;
    }


    public Collection<String> getEffectiveAttributeNames() {
        final Set<String> anames = new HashSet<>(getAttributes().size() * 2);
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

    @JsonProperty(required = true)
    public Collection<AsmAttribute> getEffectiveAttributes() {
        final Set<AsmAttribute> attrs = new HashSet<>();
        if (getAttributes() != null) {
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

    public static class AttrsList extends AbstractIndexedCollection<String, AsmAttribute> implements Serializable {

        protected String getElementKey(AsmAttribute el) {
            return el.getName();
        }

        public AttrsList() {
        }

        protected AttrsList(int size) {
            super(size);
        }

        public AttrsList cloneDeep() {
            AttrsList nlist = new AttrsList(size());
            for (AsmAttribute attr : this) {
                nlist.add(attr.cloneDeep());
            }
            return nlist;
        }
    }

    /**
     * Perform deep clone of this assembly.
     * Cloned parents are cached in <c>cloneContext</c>.
     */
    public Asm cloneDeep(Map<String, Asm> cloneContext) {
        Asm asm = cloneContext.get(name);
        if (asm != null) {
            return asm;
        }
        asm = new Asm();
        asm.id = id;
        asm.name = name;
        asm.description = description;
        asm.options = options;
        asm.core = (core != null) ? core.cloneDeep() : null;
        asm.attributes = (attributes != null) ? attributes.cloneDeep() : null;
        if (getParents() != null) {
            asm.parents = new ArrayList<>(getParents().size());
            for (Asm parent : getParents()) {
                asm.parents.add(parent.cloneDeep(cloneContext));
            }
        }
        cloneContext.put(asm.name, asm);
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
