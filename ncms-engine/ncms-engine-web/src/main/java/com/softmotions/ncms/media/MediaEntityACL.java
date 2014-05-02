package com.softmotions.ncms.media;

import java.io.Serializable;

/**
 * Access list for media entity.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaEntityACL implements Serializable {

    Long ownerId;

    String acl;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntityACL that = (MediaEntityACL) o;
        return !(acl != null ? !acl.equals(that.acl) : that.acl != null);
    }

    public int hashCode() {
        return acl != null ? acl.hashCode() : 0;
    }
}
