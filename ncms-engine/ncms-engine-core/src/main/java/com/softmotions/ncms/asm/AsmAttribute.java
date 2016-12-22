package com.softmotions.ncms.asm;

import java.io.Serializable;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Assembly attribute.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@NotThreadSafe
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
    transient AsmAttribute overriddenParent;

    @JsonView(Asm.ViewFull.class)
    long ordinal;

    @JsonView(Asm.ViewFull.class)
    boolean required;

    Date mdate;

    public AsmAttribute() {
    }

    public AsmAttribute(String name, String value) {
        this.name = name;
        this.type = "string";
        setEffectiveValue(value);
    }

    public AsmAttribute(String name, String type, @Nullable String value) {
        this.name = name;
        this.type = type;
        setEffectiveValue(value);
    }

    public AsmAttribute(String name, String label, String type, String value) {
        this.name = name;
        this.label = label;
        this.type = type;
        setEffectiveValue(value);
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

    public void setAsmId(long asmId) {
        this.asmId = asmId;
    }

    @Nonnull
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

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    @Nullable
    public String getLargeValue() {
        return largeValue;
    }

    public void setLargeValue(@Nullable String largeValue) {
        this.largeValue = largeValue;
    }

    @Nullable
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
    public boolean isOverridden() {
        return (this.overriddenParent != null);
    }

    @Nullable
    AsmAttribute getOverriddenParent() {
        return overriddenParent;
    }

    void setOverriddenParent(AsmAttribute overriddenParent) {
        this.overriddenParent = overriddenParent;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Nonnull
    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    @JsonIgnore
    @Nullable
    public String getEffectiveValue() {
        return (largeValue != null) ? largeValue : value;
    }

    public void setEffectiveValue(@Nullable String val) {
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

    @Nullable
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
     * in the context of specified assembly.
     */
    public String toString(Asm asm) {
        return asm.getName() + '#' + name;
    }

    @Nonnull
    public AsmAttribute cloneDeep() {
        AsmAttribute attr = new AsmAttribute();
        attr.id = id;
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

    @Override
    public int compareTo(AsmAttribute o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(ordinal, o.ordinal);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("AsmAttribute{");
        sb.append("name='").append(name).append('\'');
        sb.append(", id='").append(id).append('\'');
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
