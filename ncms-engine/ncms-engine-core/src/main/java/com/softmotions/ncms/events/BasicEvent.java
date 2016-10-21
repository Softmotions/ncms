package com.softmotions.ncms.events;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.collections4.map.Flat3Map;

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

    final String user;

    private Map<String, Object> hints;

    public BasicEvent(Object source, String type, String user) {
        this.source = source;
        this.type = type;
        this.user = user;
    }

    public BasicEvent(Object source, String type, HttpServletRequest req) {
        this.source = source;
        this.type = type;
        if (req != null && req.getUserPrincipal() != null) {
            this.user = req.getUserPrincipal().getName();
        } else {
            this.user = null;
        }
    }

    public BasicEvent hint(String key, Object val) {
        if (hints == null) {
            hints = new Flat3Map<>();
        }
        hints.put(key, val);
        return this;
    }

    public Map<String, Object> hints() {
        if (hints == null) {
            return Collections.emptyMap();
        }
        return hints;
    }

    public String getUser() {
        return user;
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
