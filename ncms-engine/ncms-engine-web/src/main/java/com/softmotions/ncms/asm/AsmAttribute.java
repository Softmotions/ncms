package com.softmotions.ncms.asm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Assembly attribute.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class AsmAttribute implements AsmRenderable, Serializable {

    protected final String name;

    protected final String type;

    protected final String value;

    protected final Map<String, String> options;

    protected final Asm owner;

    protected AsmAttribute(Asm owner, String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.options = new HashMap<>();
        this.owner = owner;
    }

    public Asm getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsmAttribute that = (AsmAttribute) o;
        if (!name.equals(that.name)) return false;
        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
