package com.softmotions.ncms.mhttl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple tree container
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class Tree implements Iterable<Tree>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(Tree.class);

    private Long id;

    private String name;

    private String type;

    private String extra;

    private String icon;

    private String link;

    private String nam; //Nested attribute manager serialized JSON model

    private RichRef richRef;

    private Long syncWithId;


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

    public void setName(String name) {
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

    public String getLink() {
        if (link == null && "file".equals(type) && id != null) {
            return "media:/" + id;
        } else {
            return link;
        }
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNam() {
        return nam;
    }

    public void setNam(String nam) {
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

    public Long getSyncWithId() {
        return syncWithId;
    }

    public void setSyncWithId(Long syncWithId) {
        this.syncWithId = syncWithId;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tree{");
        sb.append("id=").append(id);
        sb.append(", syncWithId=").append(syncWithId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", extra='").append(extra).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", link='").append(link).append('\'');
        sb.append(", nam='").append(nam).append('\'');
        sb.append(", richRef='").append(richRef).append('\'');
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }
}
