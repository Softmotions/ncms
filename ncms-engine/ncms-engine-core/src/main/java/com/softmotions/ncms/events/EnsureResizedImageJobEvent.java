package com.softmotions.ncms.events;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class EnsureResizedImageJobEvent {
    private final long id;
    private final Integer width;
    private final Integer height;
    private final int flags;

    public long getId() {
        return id;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public int getFlags() {
        return flags;
    }

    public EnsureResizedImageJobEvent(long id, Integer width, Integer height, int flags) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.flags = flags;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("ResizeImageJobEvent{");
        sb.append("id=").append(id);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", flags=").append(flags);
        sb.append('}');
        return sb.toString();
    }
}
