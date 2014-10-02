package com.softmotions.ncms.ds;

import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class GeneralDataStore extends MBDAOSupport {

    @Inject
    public GeneralDataStore(SqlSession sess) {
        super(GeneralDataStore.class.getName(), sess);
    }

    @Transactional
    public void getData(String ref, DataApplyCallback callback) throws Exception {
        if (callback == null) {
            throw new IllegalArgumentException("callback");
        }

        Map<String, Object> row = selectOne("getData", ref);
        if (row == null) {
            callback.apply(null, null);
            return;
        }

        String type = (String) row.get("content_type");
        try (InputStream data = (InputStream) row.get("data")) {
            callback.apply(type, data);
        }
    }

    @Transactional
    public boolean isDataExists(String ref) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        return ((Long) selectOne("checkDataExists", StringUtils.trim(ref))) > 0;
    }

    @Transactional
    public void saveData(String ref, InputStream data, String type) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("type");
        }

        update("saveData", "ref", ref, "data", data, "content_type", type);
    }

    @Transactional
    public void removeData(String ref) {
        if (StringUtils.isBlank(ref)) {
            throw new IllegalArgumentException("ref");
        }
        update("removeData", StringUtils.trim(ref));
    }

    public interface DataApplyCallback {
        void apply(String type, InputStream data);
    }
}
