package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.testng.annotations.Factory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class DbTestsFactory {

    public Collection<String> getDatabases() {
        boolean testDb2 = BooleanUtils.toBoolean(System.getProperty("TestDB2"));
        List<String> tests = new ArrayList<>();
        tests.add("postgres");
        if (testDb2) {
            tests.add("db2");
        }
        return tests;
    }

    public abstract Object createTest(String db);

    @Factory
    public Object[] createInstances() {
         return getDatabases().stream().map(this::createTest).toArray();
    }
}
