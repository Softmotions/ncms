package com.softmotions.ncms.asm.events;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmModifiedEvent extends BasicEvent {

    final Long id;

    /**
     * Assembly ID.
     * Cannot be {@code null}
     */
    public Long getId() {
        return id;
    }


    public AsmModifiedEvent(Object source, Long id) {
        super(source);
        this.id = id;
    }
}