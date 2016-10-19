package com.softmotions.ncms.atm;

import com.softmotions.ncms.events.BasicEvent;

/**
 * Fired if UI user disconnected from server.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class UIUserDisconnectedEvent extends BasicEvent {

    private String user;

    public UIUserDisconnectedEvent(String user, Object source) {
        super(source, UIUserDisconnectedEvent.class.getSimpleName());
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
