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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import io.kahu.hawaii.cache.ConcurrentMapCacheService;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ConcurrentMapCacheServiceTest {

    private ConcurrentMapCacheService cacheService = null;

    @Before
    public void setUp() {
        cacheService = new ConcurrentMapCacheService();
    }

    @Test
    public void testPutGet() throws Exception {
        String key = "key";
        Object object = "object";

        // store object in cache
        cacheService.put(key, object);

        // assert same object returned from cache
        assertThat(cacheService.get(key), is(sameInstance(object)));
    }

    @Test
    public void testPutGetMultiple() throws Exception {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "namespaced-key:with.dots.key3";
        Object object1 = "object1";
        Object object2 = new Integer(2);
        Object object3 = new Date();

        // store objects in cache
        cacheService.put(key1, object1);
        cacheService.put(key2, object2);
        cacheService.put(key3, object3);

        // assert same objects returned from cache
        assertThat(cacheService.get(key1), is(sameInstance(object1)));
        assertThat(cacheService.get(key2), is(sameInstance(object2)));
        assertThat(cacheService.get(key3), is(sameInstance(object3)));
    }

    @Test
    public void testPutGetDelete() throws Exception {
        String key = "key";
        Object object = "object";

        // store object in cache
        cacheService.put(key, object);

        // assert same object returned from cache
        assertThat(cacheService.get(key), is(sameInstance(object)));

        // delete object from cache
        cacheService.delete(key);

        // assert null value returned from cache
        assertThat(cacheService.get(key), is(nullValue()));
    }

    @Test
    public void testGetWithAutomaticCast() throws Exception {
        String key = "key";
        String object = "object";

        // store object in cache
        cacheService.put(key, object);

        // assert same object returned from cache
        assertThat(cacheService.get(key), is(sameInstance((Object) object)));
    }

    @Test
    public void testGetReturnsNullValueWhenKeyNotFound() throws Exception {
        // assert null value returned from cache
        assertThat(cacheService.get("non.existing.key"), is(nullValue()));
    }

    @Test
    public void testPutWithExpiration() throws Exception {
        String key = "key";
        Object object = "object";

        // store object in cache with expiration of 1 second
        cacheService.put(key, 1, object);

        // assert same object returned from cache
        assertThat(cacheService.get(key), is(sameInstance(object)));

        // wait 2 seconds to force expiration of object
        int millis = 2 * 1000;
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait(millis);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // assert null value returned from cache
        assertThat(cacheService.get(key), is(nullValue()));
    }
    
    @Test
    public void testPutWithExpirationUnlimited() throws Exception {
		String key = "key";
		Object object = "object";

		// store object in cache with expiration of 0 second = unlimited
		cacheService.put(key, 0, object);

		// assert expiration is 0 (unlimited)
		assertThat(cacheService.getExpiration(key), is((long) 0));

		// assert same object returned from cache
		assertThat(cacheService.get(key), is(sameInstance(object)));	
    }
    
    @Test
    public void testPutWithExpirationBiggerThanThirtyDays() throws Exception {
    	String key = "key";
		Object object = "object";

		// store object in cache with expiration of 2 times thirty days
		cacheService.put(key,2 * 60 * 60 * 24 * 30, object);
		
		// assert expiration is 0 (unlimited)
		assertThat(cacheService.getExpiration(key), is((long)2 * 60 * 60 * 24 * 30));
    }
    
    @Test
    public void testPutWithExpirationSmallerThanThirtyDays() throws Exception {
    	String key = "key";
		Object object = "object";

		int seconds = 10;
		// store object in cache with expiration of seconds
		cacheService.put(key, seconds, object);
		
		assertThat(cacheService.getExpiration(key), is(greaterThan((long) seconds)));
		assertThat(cacheService.getExpiration(key), lessThanOrEqualTo(System.currentTimeMillis() + seconds * 1000));
    }
}
