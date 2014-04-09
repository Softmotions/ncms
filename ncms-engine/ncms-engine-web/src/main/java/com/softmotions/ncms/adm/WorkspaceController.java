package com.softmotions.ncms.adm;

import ninja.Result;
import ninja.Results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Оставлен в живых для демонстрации
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 * @deprecated
 */
public class WorkspaceController {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceController.class);

    public Result workspace() {
        log.info("workspace called!!!!");
        Map<String, Object> test = new HashMap<>();
        test.put("foo", "bar");
        return Results.json().render(test);
    }
}
