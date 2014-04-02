package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.AbstractIndexedCollection;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

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

    public Asm() {
    }

    public Asm(Long id, String name) {
        this.id = id;
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

    public List<Asm> getParents() {
        return parents;
    }

    public void setParents(List<Asm> parents) {
        this.parents = parents;
    }

    public AsmAttribute getAttribute(String name) {
        return attributes != null ? attributes.getIndex().get(name) : null;
    }

    public Collection<AsmAttribute> getAttributes() {
        return attributes;
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
        sb.append(", core=").append(core);
        sb.append(", description='").append(description).append('\'');
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }
}
