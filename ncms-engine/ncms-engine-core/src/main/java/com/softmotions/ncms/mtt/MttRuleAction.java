package com.softmotions.ncms.mtt;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class MttRuleAction implements Serializable {
    @JsonProperty
    private Long id;
    @JsonProperty
    private Long ruleId;
    @JsonProperty
    private Long ordinal;
    @JsonProperty
    private String type;
    @JsonProperty
    private String description;
    @JsonProperty
    private String spec;
    @JsonProperty
    private Date cdate;
    @JsonProperty
    private Date mdate;
    @JsonProperty
    private boolean enabled = true;
    @JsonProperty
    private Long groupId;
    @JsonProperty
    private Integer groupWeight;

    public MttRuleAction() {
    }

    public MttRuleAction(Long ruleId, String type) {
        this.ruleId = ruleId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Long ordinal) {
        this.ordinal = ordinal;
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

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupWeight() {
        return groupWeight;
    }

    public void setGroupWeight(Integer groupWeight) {
        this.groupWeight = groupWeight;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MttRuleAction that = (MttRuleAction) o;
        return Objects.equals(id, that.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("ruleId", ruleId)
                          .add("ordinal", ordinal)
                          .add("type", type)
                          .add("spec", spec)
                          .add("groupId", groupId)
                          .add("groupWeight", groupWeight)
                          .add("enabled", enabled)
                          .toString();
    }
}
