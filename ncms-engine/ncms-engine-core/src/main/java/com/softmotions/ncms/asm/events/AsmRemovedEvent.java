package com.softmotions.ncms.asm.events;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmRemovedEvent extends BasicEvent {

    final Long id;

    public Long getId() {
        return id;
    }

    public AsmRemovedEvent(Object source, Long id) {
        super(source, AsmRemovedEvent.class.getSimpleName());
        this.id = id;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .toString();
    }
}
