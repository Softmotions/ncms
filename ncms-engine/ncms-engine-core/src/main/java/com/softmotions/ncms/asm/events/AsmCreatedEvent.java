package com.softmotions.ncms.asm.events;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmCreatedEvent extends BasicEvent {

    final Long id;

    public Long getId() {
        return id;
    }

    public AsmCreatedEvent(Object source, @Nullable  Long id, HttpServletRequest req) {
        super(source, AsmCreatedEvent.class.getSimpleName(), req);
        this.id = id;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .toString();
    }
}
