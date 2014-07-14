package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.KVOptions;

import java.util.Map;

/**
 * Parsed assembly options.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmOptions extends KVOptions {

    public AsmOptions() {
    }

    public AsmOptions(Map map) {
        super(map);
    }

    public AsmOptions(String spec) {
        loadOptions(spec);
    }
}



