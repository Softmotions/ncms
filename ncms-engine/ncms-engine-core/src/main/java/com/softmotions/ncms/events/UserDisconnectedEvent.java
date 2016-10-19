package com.softmotions.ncms.events;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class UserDisconnectedEvent extends BasicEvent {

    private String user;

    public UserDisconnectedEvent(String user, Object source) {
        super(source, "disconnected");
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
