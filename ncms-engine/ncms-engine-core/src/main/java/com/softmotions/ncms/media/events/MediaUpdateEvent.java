package com.softmotions.ncms.media.events;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaUpdateEvent extends BasicEvent {

    final Long id;

    final String path;

    final boolean isFolder;

    public MediaUpdateEvent(Object source, boolean isFolder, Number id, String path) {
        super(source, MediaUpdateEvent.class.getSimpleName());
        this.id = id != null ? id.longValue() : null;
        this.path = path;
        this.isFolder = isFolder;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
