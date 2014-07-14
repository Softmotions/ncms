package com.softmotions.ncms.events;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class BasicEvent {

    final Object source;

    final String type;

    public BasicEvent(Object source) {
        this.source = source;
        this.type = null;
    }

    public BasicEvent(Object source, String type) {
        this.source = source;
        this.type = type;
    }

    public Object getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("{type='").append(type).append('\'');
        sb.append(", source=").append(source);
        sb.append('}');
        return sb.toString();
    }
}
