package com.softmotions.ncms.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MBDebugCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(MBDebugCache.class);

    private String id;

    private Map<Object, Object> cache = new HashMap<Object, Object>();

    public MBDebugCache(String id) {
        log.info("NEW CACHE ID={}", id);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getSize() {
        return cache.size();
    }

    @Override
    public void putObject(Object key, Object value) {
        log.info("PUT key={}, val={}", key, value);
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        Object res = cache.get(key);
        log.info("GET key={}, res={}", key, res);
        return res;
    }

    @Override
    public Object removeObject(Object key) {
        log.info("REMOVE key={}", key);
        return cache.remove(key);
    }

    @Override
    public void clear() {
        log.info("CACHE CLEAR");
        cache.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    public boolean equals(Object o) {
        if (getId() == null) throw new CacheException("Cache instances require an ID.");
        if (this == o) return true;
        if (!(o instanceof Cache)) return false;

        Cache otherCache = (Cache) o;
        return getId().equals(otherCache.getId());
    }

    public int hashCode() {
        if (getId() == null) throw new CacheException("Cache instances require an ID.");
        return getId().hashCode();
    }

}
