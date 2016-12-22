package com.softmotions.ncms.asm.events;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmModifiedEvent extends BasicEvent {

    private final Long id;

    /**
     * Assembly ID.
     * Cannot be {@code null}
     */
    public Long getId() {
        return id;
    }

    public AsmModifiedEvent(Object source, Long id, HttpServletRequest req) {
        super(source, AsmModifiedEvent.class.getSimpleName(), req);
        this.id = id;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .toString();
    }
}