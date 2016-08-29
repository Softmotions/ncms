package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

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

    public abstract Object[] createTest(@Nonnull String db);

    @Factory
    public Object[] createInstances() {
        return getDatabases().stream().flatMap(db -> Arrays.stream(createTest(db))).toArray();
    }
}
