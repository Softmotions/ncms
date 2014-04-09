package com.softmotions.ncms.asm;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Parsed assembly options.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmOptions extends Flat3Map {

    public AsmOptions() {
    }

    public AsmOptions(Map map) {
        super(map);
    }

    public AsmOptions(String spec) {
        loadOptions(spec);
    }

    public void loadOptions(String spec) {
        int idx, sp1 = 0, sp2 = 0;
        int len = spec.length();
        boolean escaped = false;
        String part;
        while (sp1 < len) {
            idx = spec.indexOf(',', sp1);
            if (idx == -1) {
                sp1 = len;
            } else {
                if (idx > 0 && spec.charAt(idx - 1) == '\\') { //escaped delimeter ','
                    sp1 = idx + 1;
                    escaped = true;
                    continue;
                }
                sp1 = idx;
            }
            part = spec.substring(sp2, sp1);
            ++sp1;
            sp2 = sp1;
            idx = part.indexOf('=');
            if (idx != -1 && idx < len) {
                if (escaped) {
                    put(StringUtils.replace(part.substring(0, idx).trim(), "\\,", ","),
                        StringUtils.replace(part.substring(idx + 1).trim(), "\\,", ","));
                    escaped = false;
                } else {
                    put(part.substring(0, idx).trim(),
                        part.substring(idx + 1).trim());
                }
            }
        }
    }
}



