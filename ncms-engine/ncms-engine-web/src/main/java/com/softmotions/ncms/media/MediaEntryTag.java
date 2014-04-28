package com.softmotions.ncms.media;

import java.io.Serializable;

/**
 * Media entity tag.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaEntryTag implements Serializable {

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntryTag that = (MediaEntryTag) o;
        if (!name.equals(that.name)) return false;
        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
