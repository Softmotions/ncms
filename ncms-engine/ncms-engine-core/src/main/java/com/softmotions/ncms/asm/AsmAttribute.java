package com.softmotions.ncms.asm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Date;

/**
 * Assembly attribute.
 * This class is not thread safe for concurrent updating.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@JsonRootName("attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class AsmAttribute implements Serializable, Comparable<AsmAttribute> {

    Long id;

    @JsonProperty
    long asmId;

    @JsonProperty
    String name;

    @JsonProperty
    String type;

    @JsonProperty
    String value;

    @JsonProperty
    String options;

    @JsonProperty
    String label;

    @JsonView(Asm.ViewLarge.class)
    String largeValue;

    @JsonIgnore
    transient
    AsmAttribute overridenParent;

    @JsonView(Asm.ViewFull.class)
    long ordinal;

    @JsonView(Asm.ViewFull.class)
    boolean required;

    Date mdate;

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

    public AsmAttribute(String name, String label, String type, String value) {
        this.name = name;
        this.label = label;
        this.value = value;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAsmId() {
        return asmId;
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

    public long getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(long ordinal) {
        this.ordinal = ordinal;
    }

    @JsonView(Asm.ViewFull.class)
    public boolean isOverriden() {
        return (this.overridenParent != null);
    }

    AsmAttribute getOverridenParent() {
        return overridenParent;
    }

    void setOverridenParent(AsmAttribute overridenParent) {
        this.overridenParent = overridenParent;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    @JsonIgnore
    public String getEffectiveValue() {
        return (largeValue != null) ? largeValue : value;
    }

    public void setEffectiveValue(String val) {
        if (val == null) {
            setValue(null);
            setLargeValue(null);
            return;
        }
        if (val.length() <= 1024) {
            setValue(val);
            setLargeValue(null);
        } else {
            setValue(null);
            setLargeValue(val);
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty(required = true)
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
        attr.required = required;
        return attr;
    }

    public int compareTo(AsmAttribute o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(ordinal, o.ordinal);
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
