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

import io.kahu.hawaii.util.logger.LogManager;
import org.junit.Assert;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class HybridMemcachedCacheServiceTest {

    private MemcachedClient memcachedClient = null;
    private Map<String, Object> backingCache = null;
    private HybridMemcachedCacheService cacheService = null;

    // Junit way to easily check an expected exception, so you can check
    // messages, etc.
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        memcachedClient = mock(MemcachedClient.class);
        backingCache = mock(Map.class);
        cacheService = new HybridMemcachedCacheService(memcachedClient, mock(LogManager.class));
        cacheService.setBackingCache(backingCache);
        cacheService.setMaxTries(3);
    }

    @Test
    public void testGet() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);

        when(memcachedClient.gets(versionKey)).thenReturn(null);
        when(backingCache.get(versionKey)).thenReturn(null);

        Object result = cacheService.get(key);

        verify(memcachedClient).gets(versionKey);
        verify(backingCache).get(versionKey);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
        Assert.assertNull(result);
    }

    @Test
    public void testGetForObjectOnlyInMemcached() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenReturn(new CASValue<Object>(0L, 0L));
        when(memcachedClient.gets(key)).thenReturn(new CASValue<>(0L, object));
        when(backingCache.get(versionKey)).thenReturn(null);
        when(backingCache.get(key)).thenReturn(object);

        Object result = cacheService.get(key);

        verify(memcachedClient).gets(versionKey);
        verify(backingCache).get(versionKey);
        verify(memcachedClient).gets(key);
        verify(backingCache).put(versionKey, 0L);
        verify(backingCache).put(key, object);
        verify(backingCache).get(key);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
        Assert.assertEquals(object, result);
    }

    @Test
    public void testGetForObjectOutdatedInMemcached() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenReturn(new CASValue<Object>(0L, 0L));
        when(backingCache.get(versionKey)).thenReturn(1L);
        when(backingCache.get(key)).thenReturn(object);

        Object result = cacheService.get(key);

        verify(memcachedClient).gets(versionKey);
        verify(backingCache).get(versionKey);
        verify(memcachedClient).set(versionKey, 3600, 1L);
        verify(memcachedClient).set(key, 3600, object);
        verify(backingCache, times(2)).get(key);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
        Assert.assertEquals(object, result);
    }

    @Test
    public void testGetForObjectOutdatedInBackingCache() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenReturn(new CASValue<Object>(0L, 1L));
        when(memcachedClient.gets(key)).thenReturn(new CASValue<>(0L, object));
        when(backingCache.get(versionKey)).thenReturn(0L);
        when(backingCache.get(key)).thenReturn(object);

        Object result = cacheService.get(key);

        verify(memcachedClient).gets(versionKey);
        verify(backingCache).get(versionKey);
        verify(memcachedClient).gets(key);
        verify(backingCache).put(versionKey, 1L);
        verify(backingCache).put(key, object);
        verify(backingCache).get(key);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
        Assert.assertEquals(object, result);
    }

    @Test
    public void testGetForObjectInBothCaches() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenReturn(new CASValue<Object>(0L, 3L));
        when(backingCache.get(versionKey)).thenReturn(3L);
        when(backingCache.get(key)).thenReturn(object);

        Object result = cacheService.get(key);

        verify(memcachedClient).gets(versionKey);
        verify(backingCache).get(versionKey);
        verify(backingCache).get(key);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
        Assert.assertEquals(object, result);
    }

    @Test
    public void testGetThrowsCacheServiceExceptionInCaseOfFailure() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);

        when(memcachedClient.gets(versionKey)).thenThrow(new IllegalStateException("ERROR"));
        thrown.expect(CacheServiceException.class);

        cacheService.get(key);
    }

    @Test
    public void testPut() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        // store object in cache
        cacheService.put(key, object);

        // verify mock
        verify(memcachedClient).gets(versionKey);
        verify(memcachedClient).set(versionKey, 3600, 0L);
        verify(memcachedClient).set(key, 3600, object);
        verify(backingCache).get(versionKey);
        verify(backingCache).put(versionKey, 0L);
        verify(backingCache).put(key, object);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
    }

    @Test
    public void testPutForObjectAlreadyInMemcached() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenReturn(new CASValue<Object>(0L, 0L));
        when(backingCache.get(versionKey)).thenReturn(0L);
        when(memcachedClient.cas(anyString(), anyLong(), anyObject())).thenReturn(CASResponse.OK);

        cacheService.put(key, object);

        verify(memcachedClient).gets(versionKey);
        verify(backingCache).get(versionKey);
        verify(memcachedClient).cas(versionKey, 0L, 1L);
        verify(memcachedClient).set(key, 3600, object);
        verify(backingCache).put(versionKey, 1L);
        verify(backingCache).put(key, object);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
    }

    @Test
    public void testPutForObjectWithHigherVersionAlreadyInMemcached() throws Exception {
        String key = "key";
        Object object = "object";

        when(memcachedClient.gets(anyString())).thenReturn(new CASValue<Object>(0L, 2L));
        when(backingCache.get(anyString())).thenReturn(1L);
        thrown.expect(CacheServiceException.class);
        thrown.expectMessage("Memcached was updated by someone else");

        cacheService.put(key, object);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
    }

    @Test
    public void testPutWhereCASFails() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenReturn(new CASValue<Object>(0L, 0L));
        when(backingCache.get(versionKey)).thenReturn(0L);
        when(memcachedClient.cas(anyString(), anyLong(), anyObject())).thenReturn(CASResponse.EXISTS);
        thrown.expect(CacheServiceException.class);
        thrown.expectMessage("Could not update Memcached");

        cacheService.put(key, object);
        verify(memcachedClient, times(3)).cas(versionKey, 0L, 1L);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
    }

    @Test
    public void testPutThrowsCacheServiceExceptionInCaseOfFailure() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);
        Object object = "object";

        when(memcachedClient.gets(versionKey)).thenThrow(new IllegalStateException("ERROR"));
        thrown.expect(CacheServiceException.class);

        cacheService.put(key, object);
    }

    @Test
    public void testDelete() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);

        cacheService.delete(key);

        verify(memcachedClient).delete(versionKey);
        verify(memcachedClient).delete(key);
        verify(backingCache).remove(versionKey);
        verify(backingCache).remove(key);
        verifyNoMoreInteractions(memcachedClient);
        verifyNoMoreInteractions(backingCache);
    }

    @Test
    public void testDeleteThrowsCacheServiceExceptionInCaseOfFailure() throws Exception {
        String key = "key";
        String versionKey = getVersionKey(key);

        when(memcachedClient.delete(versionKey)).thenThrow(new IllegalStateException("ERROR"));
        thrown.expect(CacheServiceException.class);

        cacheService.delete(key);
    }

    private String getVersionKey(String key) {
        return key + ".version";
    }

}
