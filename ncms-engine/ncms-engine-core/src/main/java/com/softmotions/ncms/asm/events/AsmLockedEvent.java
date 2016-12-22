package com.softmotions.ncms.asm.events;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmLockedEvent extends BasicEvent {

    private long id;

    public Long getId() {
        return id;
    }

    public AsmLockedEvent(Object source, long id, String user) {
        super(source, AsmLockedEvent.class.getSimpleName(), user);
        this.id = id;
    }

    public AsmLockedEvent(Object source, long id, HttpServletRequest req) {
        super(source, AsmLockedEvent.class.getSimpleName(), req);
        this.id = id;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .toString();
    }
}
