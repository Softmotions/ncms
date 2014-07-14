package com.softmotions.ncms.asm.events;

import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class PageDroppedEvent extends BasicEvent {

    final Long id;

    final String guid;


    public Long getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public PageDroppedEvent(Object source, Long id, String guid) {
        super(source);
        this.id = id;
        this.guid = guid;
    }
}
