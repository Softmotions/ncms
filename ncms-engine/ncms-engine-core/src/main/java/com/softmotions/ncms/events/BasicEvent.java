package com.softmotions.ncms.events;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.collections4.map.Flat3Map;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.base.MoreObjects;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@JsonRootName("event")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class BasicEvent {

    final Object source;

    final String type;

    final String user;

    private final Map<String, Object> hints = new Flat3Map<>();

    public BasicEvent(Object source, String type, @Nullable String user) {
        this.source = source;
        this.type = type;
        this.user = user;
    }

    public BasicEvent(Object source, String type, @Nullable HttpServletRequest req) {
        this.source = source;
        this.type = type;
        if (req != null && req.getUserPrincipal() != null) {
            this.user = req.getUserPrincipal().getName();
            String val = req.getParameter("__app");
            if (!StringUtils.isBlank(val)) {
                this.hint("app", val);
            }
            val = req.getParameter("__ui");
            if (!StringUtils.isBlank(val)) {
                this.hint("ui", val);
            }
        } else {
            this.user = null;
        }
    }

    public BasicEvent hint(String key, Object val) {
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
                          .add("hints", hints)
                          .add("source", source)
                          .toString();
    }
}
