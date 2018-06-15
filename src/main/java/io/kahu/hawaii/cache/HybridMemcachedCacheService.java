/**
 * Copyright 2014-2018 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.cache;

import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;

import java.util.HashMap;
import java.util.Map;

public class HybridMemcachedCacheService implements CacheService {

    private static final String VERSION_SUFFIX = ".version";

    private Map<String, Object> backingCache;
    private final MemcachedClient memcachedClient;
    private final int defaultExpiration;
    private final LogManager logManager;
    private int maxTries = 10;

    public HybridMemcachedCacheService(MemcachedClient memcachedClient, LogManager logManager) {
        this(memcachedClient, 3600, logManager);
    }

    public HybridMemcachedCacheService(MemcachedClient memcachedClient, int defaultExpiration, LogManager logManager) {
        this.backingCache = new HashMap<>();
        this.memcachedClient = memcachedClient;
        this.defaultExpiration = defaultExpiration;
        this.logManager = logManager;
    }

    void setBackingCache(Map<String, Object> backingCache) {
        this.backingCache = backingCache;
    }

    void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    @Override
    public Object get(String key) throws CacheServiceException, ClassCastException {
        if (memcachedClient == null) {
            return backingCache.get(key);
        }

        long start = System.nanoTime();
        String versionKey = getVersionKey(key);

        try {
            CASValue<Object> casValue = memcachedClient.gets(versionKey);
            Long internalVersion = (Long) backingCache.get(versionKey);

            if (casValue == null) {
                if (internalVersion == null) {
                    logManager.debug(CoreLoggers.CACHE, "Object with key '" + key + "' not found in caches");
                    return null;
                } else {
                    logManager.debug(CoreLoggers.CACHE, "Object with key '" + key + "' not found in Memcached but found in internal cache. Updating Memcached");
                    Object value = backingCache.get(key);
                    updateMemcached(key, internalVersion, value);
                    return value;
                }
            } else {
                Long casVersion = (Long) casValue.getValue();
                if (internalVersion == null) {
                    logManager.debug(CoreLoggers.CACHE,
                            "Object with key '" + key + "' not found in internal cache but found in Memcached. Updating internal cache");
                    casValue = memcachedClient.gets(key);
                    updateBackingCache(key, casVersion, casValue.getValue());
                } else if (casVersion.equals(internalVersion)) {
                    logManager.debug(CoreLoggers.CACHE,
                            "Object with key '" + key + "' found in caches and version is the same, returning value from internal cache");
                } else if (casVersion > internalVersion) {
                    logManager.debug(CoreLoggers.CACHE,
                            "Object with key '" + key + "' found in caches and version in Memcached is newer, updating internal cache");
                    casValue = memcachedClient.gets(key);
                    updateBackingCache(key, casVersion, casValue.getValue());
                } else {
                    logManager.debug(CoreLoggers.CACHE,
                            "Object with key '" + key + "' found in caches and internal version is newer than Memcached. Updating Memcached");
                    updateMemcached(key, internalVersion, backingCache.get(key));
                }
                return backingCache.get(key);
            }
        } catch (Exception e) {
            throw new CacheServiceException("Error retrieving object with key '" + key + "' from cache", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.get( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    @Override
    public void put(String key, Object value) throws CacheServiceException {
        if (memcachedClient == null) {
            backingCache.put(key, value);
            return;
        }

        long start = System.nanoTime();
        String versionKey = getVersionKey(key);

        try {
            CASValue<Object> casValue = memcachedClient.gets(versionKey);
            Long internalVersion = (Long) backingCache.get(versionKey);
            if (internalVersion == null) {
                internalVersion = 0L;
            }
            if (casValue == null) {
                updateBackingCache(key, internalVersion, value);
                updateMemcached(key, internalVersion, value);
            } else {
                Long casVersion = (Long) casValue.getValue();
                if (internalVersion < casVersion) {
                    throw new CacheServiceException("Memcached was updated by someone else");
                } else {
                    internalVersion++;
                    boolean isMemcachedUpdated = false;
                    for (int i = 0; i < maxTries; i++) {
                        CASResponse response = memcachedClient.cas(versionKey, casValue.getCas(), internalVersion);
                        if (response == CASResponse.OK) {
                            memcachedClient.set(key, defaultExpiration, value);
                            isMemcachedUpdated = true;
                            break;
                        }
                        casValue = memcachedClient.gets(versionKey);
                    }
                    if (!isMemcachedUpdated) {
                        throw new CacheServiceException("Could not update Memcached");
                    }
                    updateBackingCache(key, internalVersion, value);
                }
            }
        } catch (Exception e) {
            if (e instanceof CacheServiceException) {
                throw (CacheServiceException) e;
            } else {
                throw new CacheServiceException("Error storing object with key '" + key + "' to cache", e);
            }
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.put( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    @Override
    public void put(String key, int expiration, Object object) throws CacheServiceException {
        throw new IllegalArgumentException("Not implemented!");
    }

    @Override
    public void delete(String key) throws CacheServiceException {
        if (memcachedClient == null) {
            backingCache.remove(key);
            return;
        }

        long start = System.nanoTime();
        String versionKey = getVersionKey(key);
        try {
            memcachedClient.delete(versionKey);
            memcachedClient.delete(key);
            removeFromBackingCache(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error deleting object with key '" + key + "' from Memcached", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.delete( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    @Override
    public void flush() throws CacheServiceException {
        this.backingCache.clear();
        this.memcachedClient.flush();
    }

    private String getVersionKey(String key) {
        return key + VERSION_SUFFIX;
    }

    private synchronized void updateBackingCache(String key, Long version, Object value) {
        backingCache.put(getVersionKey(key), version);
        backingCache.put(key, value);
    }

    private synchronized void removeFromBackingCache(String key) {
        backingCache.remove(getVersionKey(key));
        backingCache.remove(key);
    }

    private void updateMemcached(String key, Long version, Object value) {
        memcachedClient.set(getVersionKey(key), defaultExpiration, version);
        memcachedClient.set(key, defaultExpiration, value);
    }

}
