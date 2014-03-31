package com.softmotions.ncms.asm;

import java.io.Serializable;

/**
 * Assembly attribute.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmAttribute implements Serializable {

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
