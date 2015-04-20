/**
 * Copyright 2015 Q24
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
import net.spy.memcached.MemcachedClient;

/**
 * {@link CacheService} implementation using distributed Memcached system.
 */
public class MemcachedCacheService implements CacheService {
    private final LogManager logManager;

    private final MemcachedClient memcachedClient;
    private final int defaultExpiration;

    public MemcachedCacheService(MemcachedClient memcachedClient, LogManager logManager) {
        this(memcachedClient, 3600, logManager); // 3600 seconds == 1 hour
    }

    public MemcachedCacheService(MemcachedClient memcachedClient, int defaultExpiration, LogManager logManager) {
        this.memcachedClient = memcachedClient;
        this.defaultExpiration = defaultExpiration;
        this.logManager = logManager;
    }

    @Override
    public Object get(String key) throws CacheServiceException {
        long start = System.nanoTime();
        try {
            return memcachedClient.get(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error retrieving object with key '" + key + "' from Memcached", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.get( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    @Override
    public void put(String key, Object object) throws CacheServiceException {
        put(key, defaultExpiration, object);
    }

    @Override
    public void put(String key, int expiration, Object object) throws CacheServiceException {
        long start = System.nanoTime();
        try {
            logManager.trace(CoreLoggers.CACHE, "Put '" + key + "', expiry '" + expiration + "': " + object);
            memcachedClient.set(key, expiration, object);
        } catch (Exception e) {
            throw new CacheServiceException("Error storing object with key '" + key + "' to Memcached", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.set( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    @Override
    public void delete(String key) throws CacheServiceException {
        long start = System.nanoTime();
        try {
            logManager.trace(CoreLoggers.CACHE, "Delete '" + key + "'.");
            memcachedClient.delete(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error deleting object with key '" + key + "' from Memcached", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.delete( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }
}
