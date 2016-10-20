package com.softmotions.ncms.media.events;

import javax.servlet.http.HttpServletRequest;

import static com.softmotions.ncms.media.MediaRS.normalizeFolder;
import static com.softmotions.ncms.media.MediaRS.normalizePath;

import com.softmotions.ncms.events.BasicEvent;

/**
 * Fired if media item was removed.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaDeleteEvent extends BasicEvent {

    final boolean isFolder;

    final String path;

    final long id;

    public MediaDeleteEvent(Object source, Long id, boolean isFolder, String path, HttpServletRequest req) {
        super(source, MediaDeleteEvent.class.getSimpleName(), req);
        this.path = isFolder ? normalizeFolder(path) : normalizePath(path);
        this.isFolder = isFolder;
        this.id = id != null ? id : 0L;
    }

    public MediaDeleteEvent(Object source, Long id, boolean isFolder, String path, String user) {
        super(source, MediaDeleteEvent.class.getSimpleName(), user);
        this.path = isFolder ? normalizeFolder(path) : normalizePath(path);
        this.isFolder = isFolder;
        this.id = id != null ? id : 0L;
    }

    public long getId() {
        return id;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getPath() {
        return path;
    }
}