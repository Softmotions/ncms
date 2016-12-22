package com.softmotions.ncms.asm;

import java.util.Map;
import javax.annotation.Nullable;

import com.softmotions.commons.cont.KVOptions;

/**
 * Parsed assembly options.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmOptions extends KVOptions {

    public AsmOptions() {
    }

    public AsmOptions(Map map) {
        super(map);
    }

    public AsmOptions(@Nullable String spec) {
        loadOptions(spec);
    }
}



