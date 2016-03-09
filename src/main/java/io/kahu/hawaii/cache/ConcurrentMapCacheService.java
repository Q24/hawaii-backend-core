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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link CacheService} implementation using an in-memory concurrent map. Just
 * as in Memcache, a 0 value as expiration date is used to express unlimited
 * expiration.
 */
public class ConcurrentMapCacheService implements CacheService {

    private static final int UNLIMITED_EXPIRATION = 0;
    /**
     * number of seconds in 30 days
     */
    public static final int THIRTY_DAYS = 60 * 60 * 24 * 30;

    private final ConcurrentMap<String, Object> store;
    private final ConcurrentMap<String, Long> expirations;
    private final int defaultExpiration;

    public ConcurrentMapCacheService() {
        this(3600); // 3600 seconds == 1 hour
    }

    public ConcurrentMapCacheService(int defaultExpiration) {
        this.store = new ConcurrentHashMap<String, Object>();
        this.expirations = new ConcurrentHashMap<String, Long>();
        this.defaultExpiration = defaultExpiration;
    }

    @Override
    public Object get(String key) throws CacheServiceException, ClassCastException {
        try {
            Long expiration = expirations.get(key);
            if (expiration != null && expiration != UNLIMITED_EXPIRATION && expiration < System.currentTimeMillis()) {
                delete(key);
                return null;
            }
            return store.get(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error retrieving object with key '" + key + "' from ConcurrentMap", e);
        }
    }

    public Long getExpiration(String key) throws CacheServiceException {
        try {
            return expirations.get(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error retrieving object with key '" + key + "' from ConcurrentMap", e);
        }
    }

    @Override
    public void put(String key, Object object) throws CacheServiceException {
        put(key, defaultExpiration, object);
    }

    @Override
    public void put(String key, int expiration, Object object) throws CacheServiceException {
        try {
            store.put(key, object);
            // see
            // http://dustin.sallings.org/java-memcached-client/apidocs/net/spy/memcached/MemcachedClient.html#set(java.lang.String,
            // int, java.lang.Object) for logic behind expiration
            long calculatedExpiration;
            if (expiration == UNLIMITED_EXPIRATION || expiration > THIRTY_DAYS) {
                calculatedExpiration = expiration;
            } else {
                calculatedExpiration = System.currentTimeMillis() + (expiration * 1000);
            }
            expirations.put(key, calculatedExpiration);
        } catch (Exception e) {
            throw new CacheServiceException("Error storing object with key '" + key + "' in ConcurrentMap", e);
        }
    }

    @Override
    public void delete(String key) throws CacheServiceException {
        try {
            store.remove(key);
            expirations.remove(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error deleting object with key '" + key + "' from ConcurrentMap", e);
        }
    }

}
