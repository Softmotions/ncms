package com.softmotions.ncms.asm;

import java.io.Serializable;

/**
 * Assembly object.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Asm implements Serializable {

    long id;

    String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
