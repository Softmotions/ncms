package com.softmotions.ncms.db;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MBDebugCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(MBDebugCache.class);

    private String id;

    private Map<Object, Object> cache = new HashMap<Object, Object>();

    public MBDebugCache(String id) {
        log.info("NEW CACHE ID=" + id);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return cache.size();
    }

    public void putObject(Object key, Object value) {
        log.info("PUT key=" + key + ", val=" + value);
        cache.put(key, value);
    }

    public Object getObject(Object key) {
        Object res = cache.get(key);
        log.info("GET key=" + key + ", res=" + res);
        return res;
    }

    public Object removeObject(Object key) {
        log.info("REMOVE key=" + key);
        return cache.remove(key);
    }

    public void clear() {
        log.info("CACHE CLEAR");
        cache.clear();
    }

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
