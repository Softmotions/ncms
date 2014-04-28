package com.softmotions.ncms.media;

import java.io.Serializable;
import java.util.List;

/**
 * Media entity: folder or file.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaEntry implements Serializable {

    Long id;

    String name;

    String folder;

    String contentType;

    String description;

    List<MediaEntryTag> tags;

    Long primaryACL;

    Long secondaryACL;

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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MediaEntryTag> getTags() {
        return tags;
    }

    public void setTags(List<MediaEntryTag> tags) {
        this.tags = tags;
    }

    public Long getPrimaryACL() {
        return primaryACL;
    }

    public void setPrimaryACL(Long primaryACL) {
        this.primaryACL = primaryACL;
    }

    public Long getSecondaryACL() {
        return secondaryACL;
    }

    public void setSecondaryACL(Long secondaryACL) {
        this.secondaryACL = secondaryACL;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaEntry that = (MediaEntry) o;
        if (folder != null ? !folder.equals(that.folder) : that.folder != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return true;
    }

    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }
}
