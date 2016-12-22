package com.softmotions.ncms.media.events;

import javax.servlet.http.HttpServletRequest;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MediaUpdateEvent extends BasicEvent {

    final long id;

    final String path;

    final boolean isFolder;

    public MediaUpdateEvent(Object source, boolean isFolder, Number id, String path, HttpServletRequest req) {
        super(source, MediaUpdateEvent.class.getSimpleName(), req);
        this.id = id != null ? id.longValue() : 0L;
        this.path = path;
        this.isFolder = isFolder;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
