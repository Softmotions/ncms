package com.softmotions.ncms.asm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Assembly attribute.
 * This class is not thread safe for concurrent updating.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@JsonRootName("attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class AsmAttribute implements Serializable {

    @JsonProperty
    long asmId;

    @JsonProperty
    String name;

    @JsonProperty
    String type;

    @JsonProperty
    String value;

    String largeValue;

    @JsonProperty
    String options;

    @JsonProperty
    String label;

    public AsmAttribute() {
    }

    public AsmAttribute(String name, String value) {
        this.name = name;
        this.value = value;
        this.type = "string";
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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getEffectiveValue() {
        return (largeValue != null) ? largeValue : value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty
    public boolean isHasLargeValue() {
        return (getLargeValue() != null);
    }

    /**
     * String representation of attribute
     * in the context of specified assembyly.
     */
    public String toString(Asm asm) {
        return asm.getName() + '#' + name;
    }

    public AsmAttribute cloneDeep() {
        AsmAttribute attr = new AsmAttribute();
        attr.asmId = asmId;
        attr.name = name;
        attr.type = type;
        attr.value = value;
        attr.largeValue = largeValue;
        attr.options = options;
        attr.label = label;
        return attr;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("AsmAttribute{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", options='").append(options).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsmAttribute that = (AsmAttribute) o;
        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
