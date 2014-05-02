package com.softmotions.ncms.media;

import java.io.Serializable;

/**
 * Media entity tag.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaEntityTag implements Serializable {

    Long ownerId;

    String name;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntityTag that = (MediaEntityTag) o;
        if (!name.equals(that.name)) return false;
        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
