package com.softmotions.ncms.media;

import com.softmotions.ncms.events.BasicEvent;

/**
 * Fired if media item was moved.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaMoveEvent extends BasicEvent {

    final Long id;

    final boolean isFolder;

    final String oldPath;

    final String newPath;

    public MediaMoveEvent(Object source,
                          Long id, boolean isFolder,
                          String oldPath, String newPath) {
        super(source);
        this.id = id;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.isFolder = isFolder;
    }

    public Long getId() {
        return id;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getOldPath() {
        return oldPath;
    }

    public String getNewPath() {
        return newPath;
    }
}
