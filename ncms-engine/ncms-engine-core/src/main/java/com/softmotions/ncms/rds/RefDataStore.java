package com.softmotions.ncms.rds;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.num.NumberUtils;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class RefDataStore extends MBDAOSupport {

    @Inject
    public RefDataStore(SqlSession sess) {
        super(RefDataStore.class, sess);
    }

    @Transactional
    public void getData(String ref, RefDataAcceptor callback) throws Exception {
        if (callback == null) {
            throw new IllegalArgumentException("callback");
        }
        Map<String, Object> row = selectOne("getData", ref);
        if (row == null) {
            callback.data(null, null);
            return;
        }
        String type = (String) row.get("content_type");
        try (InputStream data = (InputStream) row.get("data")) {
            callback.data(type, data);
        }
    }

    @Transactional
    public boolean isDataExists(String ref) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        return (NumberUtils.number2Long(selectOne("checkDataExists",
                                                  StringUtils.trim(ref)), 0) > 0);
    }

    @Transactional
    public void saveData(String ref, InputStream data, String type) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("type");
        }
        update("saveData",
               "ref", ref,
               "data", data,
               "content_type", type);
    }

    @Transactional
    public void removeData(String ref) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        update("removeData", StringUtils.trim(ref));
    }

}
