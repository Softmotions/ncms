package com.softmotions.ncms.rds;

import java.io.InputStream;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface RefDataAcceptor {
    void data(String type, InputStream data);
}
