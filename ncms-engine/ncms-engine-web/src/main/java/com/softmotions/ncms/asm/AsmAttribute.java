package com.softmotions.ncms.asm;

import java.io.Serializable;

/**
 * Assembly attribute.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmAttribute implements Serializable {

    long asmId;

    String name;

    String type;

    String value;

    String largeValue;

    public AsmAttribute() {
    }

    public AsmAttribute(String name, String type, String value) {
        this.name = name;
        this.value = value;
        this.type = type;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLargeValue() {
        return largeValue;
    }

    public void setLargeValue(String largeValue) {
        this.largeValue = largeValue;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("AsmAttribute{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsmAttribute that = (AsmAttribute) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return true;
    }

    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
