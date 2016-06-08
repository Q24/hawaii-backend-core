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

public interface CacheService {

    /**
     * Returns a previously-stored object, or null if key not found.
     *
     * @param key
     *            the key of the cache entry
     * @param type
     *            the type to cast the object to
     * @return the previously-stored object, or null if key not found
     * @throws CacheServiceException
     *             in case of any error
     * @throws ClassCastException
     *             in case object cannot be cast to type T
     */
    public Object get(String key) throws CacheServiceException, ClassCastException;

    /**
     * Stores an object into the cache using the given key. Uses default
     * expiration time.
     *
     * @param key
     *            the key for the new cache entry
     * @param object
     *            the object to be stored
     * @throws CacheServiceException
     *             in case of any error
     */
    public void put(String key, Object value) throws CacheServiceException;

    /**
     * Stores an object into the cache using the given key.
     *
     * The expiration may either be Unix time (number of seconds since January
     * 1, 1970, as a 32-bit value), or a number of seconds starting from current
     * time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a
     * client is larger than that, the server will consider it to be real Unix
     * time value rather than an offset from current time.
     *
     * @param key
     *            the key for the new cache entry
     * @param expiration
     *            the expiration of the object
     * @param object
     *            the object to be stored
     * @throws CacheServiceException
     *             in case of any error
     */
    public void put(String key, int expiration, Object object) throws CacheServiceException;

    /**
     * Removes the key from the cache.
     *
     * @param key
     *            the key of the cache entry
     * @param type
     *            the type of the cache entry
     * @throws CacheServiceException
     *             in case of any error
     */
    public void delete(String key) throws CacheServiceException;

    /**
     * Flushes the cache (removing all items).
     *
     * @throws CacheServiceException
     *             in case of any error
     */
    public void flush() throws CacheServiceException;
}
