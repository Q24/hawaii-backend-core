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
import net.spy.memcached.MemcachedClient;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class MemcachedCacheServiceTest {

    private MemcachedClient memcachedClient = null;
    private MemcachedCacheService cacheService = null;

    @Before
    public void setUp() {
        memcachedClient = mock(MemcachedClient.class);
        cacheService = new MemcachedCacheService(memcachedClient, mock(LogManager.class));
    }

    @Test
    public void testPut() throws Exception {
        String key = "key";
        Object object = "object";

        // store object in cache
        cacheService.put(key, object);

        // verify mock
        verify(memcachedClient).set(key, 3600, object);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void testPutWithExpiration() throws Exception {
        String key = "key";
        Object object = "object";

        int expiration = 300;

        // store object in cache
        cacheService.put(key, expiration, object);

        // verify mock
        verify(memcachedClient).set(key, expiration, object);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test(expected = CacheServiceException.class)
    public void testPutThrowsCacheServiceExceptionInCaseOfError() throws Exception {
        String key = "key";
        Object object = "object";

        int expiration = 300;

        // mock exception
        when(memcachedClient.set(key, expiration, object)).thenThrow(new IllegalStateException("Error"));

        // store object in cache
        cacheService.put(key, expiration, object);

        // verify mock
        verify(memcachedClient).set(key, expiration, object);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void testGet() throws Exception {
        String key = "key";
        Object object = "object";

        // mock retrieve object
        when(memcachedClient.get(key)).thenReturn(object);

        // assert objects returned from cache
        assertThat(cacheService.get(key), is(sameInstance(object)));

        // verify mock
        verify(memcachedClient).get(key);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void testGetReturnsNullValueWhenKeyNotFound() throws Exception {
        String key = "key";

        // mock retrieve object
        when(memcachedClient.get(key)).thenReturn(null);

        // assert objects returned from cache
        assertThat(cacheService.get(key), is(nullValue()));

        // verify mock
        verify(memcachedClient).get(key);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void testGetWithAutomaticCast() throws Exception {
        String key = "key";
        String object = "object";

        // mock retrieve object
        when(memcachedClient.get(key)).thenReturn(object);

        // assert objects returned from cache
        assertThat(cacheService.get(key), is(sameInstance((Object) object)));

        // verify mock
        verify(memcachedClient).get(key);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test(expected = CacheServiceException.class)
    public void testGetThrowsCacheServiceExceptionInCaseOfError() throws Exception {
        String key = "key";

        // mock exception
        when(memcachedClient.get(key)).thenThrow(new IllegalStateException("Error"));

        // store object in cache
        cacheService.get(key);

        // verify mock
        verify(memcachedClient).get(key);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test
    public void testDelete() throws Exception {
        String key = "key";

        // mock retrieve object
        when(memcachedClient.delete(key)).thenReturn(null);

        // call delete
        cacheService.delete(key);

        // verify mock
        verify(memcachedClient).delete(key);
        verifyNoMoreInteractions(memcachedClient);
    }

    @Test(expected = CacheServiceException.class)
    public void testDeleteThrowsCacheServiceExceptionInCaseOfError() throws Exception {
        String key = "key";

        // mock exception
        when(memcachedClient.delete(key)).thenThrow(new IllegalStateException("Error"));

        // store object in cache
        cacheService.delete(key);

        // verify mock
        verify(memcachedClient).delete(key);
        verifyNoMoreInteractions(memcachedClient);
    }
}
