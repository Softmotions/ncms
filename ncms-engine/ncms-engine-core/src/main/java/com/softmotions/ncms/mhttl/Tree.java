package com.softmotions.ncms.mhttl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * Simple tree container
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
public final class Tree implements Iterable<Tree>, Serializable {

    private Long id;

    private String name;

    private String type;

    private String extra;

    private String icon;

    private String link;

    private String nam; //Nested attribute manager serialized JSON model

    private RichRef richRef;

    private Map<String, Object> attributes;


    @JsonProperty(required = true)
    private List<Tree> children;


    public Tree() {
    }

    public Tree(String name) {
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

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getIcon() {
        return icon != null ? icon : "default";
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Nullable
    public String getLink() {
        if (link == null && "file".equals(type) && id != null) {
            return "media:/" + id;
        } else {
            return link;
        }
    }

    public void setLink(@Nullable String link) {
        this.link = link;
    }

    @Nullable
    public String getNam() {
        return nam;
    }

    public void setNam(@Nullable String nam) {
        this.nam = nam;
    }

    @JsonIgnore
    public RichRef getRichRef() {
        return richRef;
    }

    public void setRichRef(RichRef richRef) {
        this.richRef = richRef;
    }

    public List<Tree> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<Tree> children) {
        this.children = children;
    }

    @Override
    @Nonnull
    public Iterator<Tree> iterator() {
        if (children == null) {
            return IteratorUtils.EMPTY_ITERATOR;
        }
        return children.iterator();
    }

    @JsonIgnore
    public boolean isHasChildren() {
        return (children != null && !children.isEmpty());
    }

    public int size() {
        return (children != null ? children.size() : 0);
    }

    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    @Nullable
    public <T> T getAttribute(String name) {
        return (attributes != null ? (T) attributes.get(name) : null);
    }

    public void setAttribute(String name, Object val) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(name, val);
    }


    @Nullable
    public String toHtmlLink() {
        return toHtmlLink(null);
    }

    @Nullable
    public String toHtmlLink(@Nullable Map<String, ?> amap) {
        if (link == null) {
            return richRef != null ? richRef.toHtmlLink(amap) : null;
        }
        if (name == null) {
            name = link;
        }
        StringBuilder attrs = null;
        if (amap != null && !amap.isEmpty()) {
            attrs = new StringBuilder();
            for (Map.Entry<String, ?> e : amap.entrySet()) {
                attrs.append(' ').append(e.getKey()).append('=').append(e.getValue());
            }
        }
        return String.format("<a href=\"%s\"%s>%s</a>",
                             link,
                             attrs != null ? attrs : "",
                             StringEscapeUtils.escapeHtml4(name));
    }


    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("name", name)
                          .add("type", type)
                          .add("extra", extra)
                          .add("icon", icon)
                          .add("link", link)
                          .add("nam", nam)
                          .add("richRef", richRef)
                          .add("attributes", attributes)
                          .add("children", children)
                          .toString();
    }
}
