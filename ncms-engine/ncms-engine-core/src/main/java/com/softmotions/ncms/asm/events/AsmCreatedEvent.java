package com.softmotions.ncms.asm.events;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.events.BasicEvent;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmCreatedEvent extends BasicEvent {

    private final Long id;

    private final Long navParentId;

    private final String name;

    private final String hname;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHname() {
        return hname;
    }

    public Long getNavParentId() {
        return navParentId;
    }

    public AsmCreatedEvent(Object source,
                           Asm asm,
                           HttpServletRequest req) {
        super(source, AsmCreatedEvent.class.getSimpleName(), req);
        this.id = asm.getId();
        this.name = asm.getName();
        this.hname = asm.getHname();
        this.navParentId = asm.getNavParentId();
    }

    public AsmCreatedEvent(Object source,
                           Long id,
                           Long navParent,
                           String name,
                           String hname,
                           HttpServletRequest req) {
        super(source, AsmCreatedEvent.class.getSimpleName(), req);
        this.id = id;
        this.name = name;
        this.hname = hname;
        this.navParentId = navParent;
    }


    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("navParentId", name)
                          .add("name", name)
                          .add("hname", hname)
                          .toString();
    }
}
