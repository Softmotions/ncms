package com.softmotions.ncms.mtt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * MTT rule.
 *
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
public class MttRule implements Serializable {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private Long ordinal;

    @JsonProperty
    private Date cdate;

    @JsonProperty
    private Date mdate;

    @JsonProperty
    private boolean enabled = true;

    @JsonProperty
    private long flags;

    private List<MttRuleFilter> filters;
    private List<MttRuleAction> actions;

    public MttRule() {
    }

    public MttRule(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public MttRule(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Long ordinal) {
        this.ordinal = ordinal;
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

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    @NotNull
    public List<MttRuleFilter> getFilters() {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        return filters;
    }

    public void setFilters(List<MttRuleFilter> filters) {
        this.filters = filters;
    }

    @Nonnull
    public List<MttRuleAction> getActions() {
        if (actions == null) {
            actions = new ArrayList<>();
        }
        return actions;
    }

    public void setActions(List<MttRuleAction> actions) {
        this.actions = actions;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MttRule mttRule = (MttRule) o;
        return Objects.equals(id, mttRule.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("ordinal", ordinal)
                          .add("name", name)
                          .add("filters", filters)
                          .add("actions", actions)
                          .toString();
    }
}
