package com.softmotions.ncms.ds;

import com.softmotions.commons.cont.Pair;
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
    public Pair<String, InputStream> getData(String ref) throws Exception {
        Map<String, Object> row = selectOne("getData", ref);
        if (row == null) {
            return null;
        }

        String type = (String) row.get("content_type");
        InputStream data = (InputStream) row.get("data");

        // TODO: export data by callback

        return type == null || data == null ? null : new Pair(type, data);
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
}
