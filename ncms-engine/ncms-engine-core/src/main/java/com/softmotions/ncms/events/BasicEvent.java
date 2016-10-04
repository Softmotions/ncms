package com.softmotions.ncms.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.base.MoreObjects;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@JsonRootName("event")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class BasicEvent {

    final Object source;

    final String type;

    public BasicEvent(Object source, String type) {
        this.source = source;
        this.type = type;
    }

    @JsonIgnore
    public Object getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("type", type)
                          .add("source", source)
                          .toString();
    }
}
