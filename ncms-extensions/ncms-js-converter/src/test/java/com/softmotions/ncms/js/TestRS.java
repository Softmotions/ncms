package com.softmotions.ncms.js;

import javax.annotation.Nonnull;

import com.softmotions.ncms.DbTestsFactory;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class TestRS extends DbTestsFactory {

    @Override
    public Object[] createTests(@Nonnull String db) {
        return new Object[]{new _TestJsServiceRS(db)};
    }
}
