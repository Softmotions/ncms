package com.softmotions.ncms.db;

import org.testng.annotations.Factory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class TestNcmsDBs {

    @Factory
    public Object[] createInstances() {
        return new Object[]{
                new NcmsDB1("postgresql")
        };
    }
}
