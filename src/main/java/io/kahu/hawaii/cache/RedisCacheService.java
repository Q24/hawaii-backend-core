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
 *
 * @author Wouter Eerdekens
 * @since 0.4.17
 */
package io.kahu.hawaii.cache;

import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * {@link CacheService} implementation using Redis.
 */
public class RedisCacheService implements CacheService {

    /**
     * Default value for {@code defaultExpiration}.
     */
    private static final int DEFAULT_DEFAULT_EXPIRATION = 3600;

    /**
     * Hawaii {@link LogManager} used by this class.
     */
    private final LogManager logManager;
    /**
     * The {@link RedisTemplate} used by this class.
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * Default expiration for items if no explicit expiration is provided when storing them.
     */
    private final int defaultExpiration;

    /**
     * Constructor.
     *
     * @param logManager the logManager
     * @param redisTemplate the redisTemplate
     */
    public RedisCacheService(final LogManager logManager,
            final RedisTemplate<String, Object> redisTemplate) {
        this (logManager, redisTemplate, DEFAULT_DEFAULT_EXPIRATION);
    }

    /**
     * Constructor.
     *
     * @param logManager the logManager
     * @param redisTemplate the redisTemplate
     * @param defaultExpiration the default expiration
     */
    public RedisCacheService(final LogManager logManager,
            final RedisTemplate<String, Object> redisTemplate, final int defaultExpiration) {
        this.logManager = logManager;
        this.redisTemplate = redisTemplate;
        this.defaultExpiration = defaultExpiration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final String key) throws CacheServiceException, ClassCastException {
        long start = System.nanoTime();
        try {
            return redisTemplate.boundValueOps(key).get();
        } catch (Exception e) {
            throw new CacheServiceException("Error retrieving object with key '" + key + "' from Redis", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.get( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final String key, final Object object) throws CacheServiceException {
        put(key, defaultExpiration, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final String key, final int expiration, final Object object) throws CacheServiceException {
        long start = System.nanoTime();
        try {
            logManager.trace(CoreLoggers.CACHE, "Put '" + key + "', expiry '" + expiration + "': " + object);
            redisTemplate.boundValueOps(key).set(object, expiration, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new CacheServiceException("Error storing object with key '" + key + "' in Redis", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.set( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String key) throws CacheServiceException {
        long start = System.nanoTime();
        try {
            logManager.trace(CoreLoggers.CACHE, "Delete '" + key + "'.");
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new CacheServiceException("Error deleting object with key '" + key + "' from Redis", e);
        } finally {
            long end = System.nanoTime();
            logManager.debug(CoreLoggers.CACHE, "cache.delete( '" + key + "' ) took '" + (end - start) / 1E6 + "' msec.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws CacheServiceException {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            throw new CacheServiceException("Error flushing Redis cache", e);
        }
    }
}
