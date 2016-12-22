package com.softmotions.ncms.mhttl;

import java.io.Serializable;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public final class SelectNode implements Serializable {

    final String key;

    final String value;

    final boolean selected;

    public SelectNode(String key, String value, boolean selected) {
        this.key = key;
        this.value = value;
        this.selected = selected;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }
}
