package com.softmotions.ncms.asm.events;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmLockedEvent extends BasicEvent {

    private Long id;

    private String user;

    public Long getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public AsmLockedEvent(Object source, Long id, String user) {
        super(source, AsmLockedEvent.class.getSimpleName());
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("user", user)
                          .toString();
    }
}
