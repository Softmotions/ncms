package com.softmotions.ncms.mhttl;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.collections.IteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Simple tree container
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class Tree implements Iterable<Tree> {

    private Long id;

    private String name;

    private String type;

    private String extra;

    private String icon;

    @JsonProperty(required = true)
    private ArrayList<Tree> children;

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

    public ArrayList<Tree> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(ArrayList<Tree> children) {
        this.children = children;
    }

    public Iterator<Tree> iterator() {
        if (children == null) {
            return IteratorUtils.EMPTY_ITERATOR;
        }
        return children.iterator();
    }

    public boolean isHasChildren() {
        return (children != null && !children.isEmpty());
    }
}
