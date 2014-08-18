package com.softmotions.ncms.asm.events;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmCreatedEvent extends BasicEvent {

    final Long id;


    public Long getId() {
        return id;
    }

    public AsmCreatedEvent(Object source, Long id) {
        super(source);
        this.id = id;
    }
}
