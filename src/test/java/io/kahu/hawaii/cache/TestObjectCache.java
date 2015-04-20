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

import io.kahu.hawaii.cache.Cache;
import io.kahu.hawaii.cache.CacheServiceException;

public class TestObjectCache implements Cache<Object> {

    public TestObjectCache() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object get(String key) throws CacheServiceException, ClassCastException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public <E> E get(String key, Class<E> type) throws CacheServiceException, ClassCastException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put(String key, Object value) throws CacheServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void put(String key, int expiration, Object object) throws CacheServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(String key) throws CacheServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

}
