package com.softmotions.ncms.db;

import com.softmotions.ncms.DbTestsFactory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class TestNcmsDBs extends DbTestsFactory {

    @Override
    public Object createTest(String db) {
        return new NcmsDB1(db);
    }
}
