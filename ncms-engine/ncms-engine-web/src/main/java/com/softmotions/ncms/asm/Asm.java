package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.AbstractIndexedCollection;

import java.io.Serializable;
import java.util.Collection;

/**
 * Assembly object.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Asm implements Serializable {

    long id;

    String name;

    String description;

    AttrsList attributes;

    public Asm() {
    }

    public Asm(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
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
}
