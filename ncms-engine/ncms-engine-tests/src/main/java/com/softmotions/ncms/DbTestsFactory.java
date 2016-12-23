package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.BooleanUtils;
import org.testng.annotations.Factory;

import com.softmotions.commons.cont.ArrayUtils;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public abstract class DbTestsFactory {

    public Collection<String> getDatabases() {
        boolean all = BooleanUtils.toBoolean(System.getProperty("testAllDB"));
        boolean testDb2 = all || BooleanUtils.toBoolean(System.getProperty("testDB2"));
        boolean testPostgres = all || BooleanUtils.toBoolean(System.getProperty("testPostgres"));
        List<String> tests = new ArrayList<>();
        if (testPostgres) {
            tests.add("postgres");
        }
        if (testDb2) {
            tests.add("db2");
        }
        if (tests.isEmpty()) {
            tests.add("postgres");
        }
        return tests;
    }

    public abstract Object[] createTests(@Nonnull String db);

    @Factory
    public Object[] createInstances() {
        Object[] instances = getDatabases().stream().flatMap(db -> Arrays.stream(createTests(db))).toArray();
        System.err.println("Test instances: " +
                           ArrayUtils.stringJoin(Arrays.stream(instances).map(o -> o.getClass().getName()).toArray(), ", "));
        return instances;
    }
}
