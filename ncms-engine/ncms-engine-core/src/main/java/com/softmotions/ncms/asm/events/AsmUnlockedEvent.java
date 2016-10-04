package com.softmotions.ncms.asm.events;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmUnlockedEvent extends BasicEvent {

    final Long id;

    public Long getId() {
        return id;
    }

    public AsmUnlockedEvent(Object source, Long id) {
        super(source, AsmUnlockedEvent.class.getSimpleName());
        this.id = id;
    }
}
