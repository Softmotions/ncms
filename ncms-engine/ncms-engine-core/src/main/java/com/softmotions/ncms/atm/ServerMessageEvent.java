package com.softmotions.ncms.atm;

import javax.annotation.Nullable;

import com.softmotions.ncms.events.BasicEvent;

/**
 * Async server message, reported
 * to all connected users.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class ServerMessageEvent extends BasicEvent {

    String message;

    boolean error;

    boolean persistent;

    /**
     * Message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * True this is an error message
     */
    public boolean isError() {
        return error;
    }

    /**
     * Message/error will be persisted in admin UI
     */
    public boolean isPersistent() {
        return persistent;
    }

    public ServerMessageEvent(Object source,
                              String message,
                              @Nullable String user) {
        this(source, message, false, false, user);
    }

    public ServerMessageEvent(Object source,
                              String message,
                              boolean error,
                              boolean persistent,
                              @Nullable String user) {
        super(source, ServerMessageEvent.class.getSimpleName(), user);
        this.message = message;
        this.error = error;
        this.persistent = persistent;
    }
}
