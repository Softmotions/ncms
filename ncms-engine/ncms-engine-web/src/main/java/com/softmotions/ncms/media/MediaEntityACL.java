package com.softmotions.ncms.media;

import java.io.Serializable;

/**
 * Access list for media entity.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaEntityACL implements Serializable {

    Long id;

    String sortedACL;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSortedACL() {
        return sortedACL;
    }

    public void setSortedACL(String sortedACL) {
        this.sortedACL = sortedACL;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntityACL that = (MediaEntityACL) o;
        return !(sortedACL != null ? !sortedACL.equals(that.sortedACL) : that.sortedACL != null);
    }

    public int hashCode() {
        return sortedACL != null ? sortedACL.hashCode() : 0;
    }
}
