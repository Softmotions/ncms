package com.softmotions.ncms.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class RandomUUID {

    private RandomUUID() {
    }

    public static String createCompactUUID() {
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }
}
