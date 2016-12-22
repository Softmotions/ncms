package com.softmotions.ncms.db;

import java.util.concurrent.locks.ReadWriteLock;
import javax.annotation.Nullable;

import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MybatisEhcacheCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(MybatisEhcacheCache.class);

    /**
     * The cache manager reference.
     */
    private static final CacheManager CACHE_MANAGER = CacheManager.create();

    /**
     * The cache id (namespace)
     */
    private final String id;

    /**
     * The cache instance
     */
    private final Ehcache cache;

    /**
     * @param id
     */
    public MybatisEhcacheCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        if (!CACHE_MANAGER.cacheExists(id)) {
            CACHE_MANAGER.addCache(id);
        }
        this.cache = CACHE_MANAGER.getCache(id);
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        cache.removeAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Object getObject(Object key) {
        Element cachedElement = cache.get(key);
        if (cachedElement == null) {
            return null;
        }
        return cachedElement.getObjectValue();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return cache.getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putObject(Object key, Object value) {
        cache.put(new Element(key, value));
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Object removeObject(Object key) {
        Object obj = getObject(key);
        cache.remove(key);
        return obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Cache)) {
            return false;
        }

        Cache otherCache = (Cache) obj;
        return id.equals(otherCache.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "EHCache {"
               + id
               + "}";
    }

    // DYNAMIC PROPERTIES

    /**
     * Sets the time to idle for an element before it expires. Is only used if the element is not eternal.
     *
     * @param timeToIdleSeconds the default amount of time to live for an element from its last accessed or modified date
     */
    public void setTimeToIdleSeconds(long timeToIdleSeconds) {
        log.info("timeToIdleSeconds={}", timeToIdleSeconds);
        cache.getCacheConfiguration().setTimeToIdleSeconds(timeToIdleSeconds);
    }

    /**
     * Sets the time to idle for an element before it expires. Is only used if the element is not eternal.
     *
     * @param timeToLiveSeconds the default amount of time to live for an element from its creation date
     */
    public void setTimeToLiveSeconds(long timeToLiveSeconds) {
        log.info("timeToLiveSeconds={}", timeToLiveSeconds);
        cache.getCacheConfiguration().setTimeToLiveSeconds(timeToLiveSeconds);
    }

    /**
     * Sets the maximum objects to be held in memory (0 = no limit).
     *
     * @param maxElementsInMemory The maximum number of elements in memory, before they are evicted (0 == no limit)
     */
    public void setMaxEntriesLocalHeap(long maxEntriesLocalHeap) {
        log.info("maxEntriesLocalHeap={}", maxEntriesLocalHeap);
        cache.getCacheConfiguration().setMaxEntriesLocalHeap(maxEntriesLocalHeap);
    }

    /**
     * Sets the maximum number elements on Disk. 0 means unlimited.
     *
     * @param maxElementsOnDisk the maximum number of Elements to allow on the disk. 0 means unlimited.
     */
    public void setMaxEntriesLocalDisk(long maxEntriesLocalDisk) {
        log.info("maxEntriesLocalDisk={}", maxEntriesLocalDisk);
        cache.getCacheConfiguration().setMaxEntriesLocalDisk(maxEntriesLocalDisk);
    }

    /**
     * Sets the eviction policy. An invalid argument will set it to null.
     *
     * @param memoryStoreEvictionPolicy a String representation of the policy. One of "LRU", "LFU" or "FIFO".
     */
    public void setMemoryStoreEvictionPolicy(String memoryStoreEvictionPolicy) {
        log.info("memoryStoreEvictionPolicy={}", memoryStoreEvictionPolicy);
        cache.getCacheConfiguration().setMemoryStoreEvictionPolicy(memoryStoreEvictionPolicy);
    }

    public void setPeristenceStrategy(String peristenceStrategy) {
        log.info("peristenceStrategy={}", peristenceStrategy);
        cache.getCacheConfiguration().getPersistenceConfiguration().setStrategy(peristenceStrategy);
    }

    public void setMaxBytesLocalHeap(long maxBytesLocalHeap) {
        log.info("maxBytesLocalHeap={}", maxBytesLocalHeap);
        cache.getCacheConfiguration().setMaxEntriesLocalHeap(0);
        cache.getCacheConfiguration().setMaxBytesLocalHeap(maxBytesLocalHeap);
    }

}
