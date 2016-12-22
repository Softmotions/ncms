package com.softmotions.ncms.asm.events;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmRemovedEvent extends BasicEvent {

    final Long id;

    public Long getId() {
        return id;
    }

    public AsmRemovedEvent(Object source, Long id, HttpServletRequest req) {
        super(source, AsmRemovedEvent.class.getSimpleName(), req);
        this.id = id;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .toString();
    }
}
