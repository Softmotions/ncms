package com.softmotions.ncms.rds;

import java.io.InputStream;
import javax.annotation.Nullable;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface RefDataAcceptor {
    void data(@Nullable String type, @Nullable InputStream data);
}
