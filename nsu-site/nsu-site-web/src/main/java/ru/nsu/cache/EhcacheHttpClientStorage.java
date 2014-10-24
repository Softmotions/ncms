package ru.nsu.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.client.cache.HttpCacheUpdateException;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class EhcacheHttpClientStorage implements HttpCacheStorage {

    private static final Logger log = LoggerFactory.getLogger(EhcacheHttpClientStorage.class);

    final EhcacheHttpCacheStorage es;

    public void putEntry(String key, HttpCacheEntry entry) throws IOException {
        es.putEntry(key, entry);
    }

    public HttpCacheEntry getEntry(String key) throws IOException {
        return es.getEntry(key);
    }

    public void removeEntry(String key) throws IOException {
        es.removeEntry(key);
    }

    public void updateEntry(String key, HttpCacheUpdateCallback callback) throws IOException, HttpCacheUpdateException {
        es.updateEntry(key, callback);
    }

    public EhcacheHttpClientStorage(Map params, CacheConfig cc) {
        log.info("Instantiating EhcacheHttpClientStorage");
        String cmName = (String) params.get("ehcacheManager");
        CacheManager cm = CacheManager.getCacheManager(cmName);
        if (cm == null) {
            throw new RuntimeException("No cache managers found");
        }
        String cacheName = (String) params.get("ehcacheCache");
        Cache cache = (cacheName != null) ? cm.getCache(cacheName) : null;
        if (cache == null) {
            throw new RuntimeException("Cache: " + cacheName + " not found");
        }
        es = new EhcacheHttpCacheStorage(cache, cc);
        log.info("Using cache: " + cache);
    }
}
