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

public class NamespacedCache<T> implements Cache<T> {
    private final CacheService cacheService;
    private final String namespace;
    private int namespaceVersion = 0;
    private final Class<T> type;
    private String prefix;

    public NamespacedCache(String namespace, CacheService cacheService, Class<T> type) {
        this.namespace = namespace;
        this.cacheService = cacheService;
        this.type = type;
        makePrefix();
    }

    @Override
    public T get(String key) throws CacheServiceException, ClassCastException {
        return type.cast(cacheService.get(prefix + key));
    }

    @Override public <E> E get(String key, Class<E> type) throws CacheServiceException, ClassCastException {
        return type.cast(cacheService.get(prefix + key));
    }

    @Override
    public void put(String key, T object) throws CacheServiceException {
        cacheService.put(prefix + key, object);
    }

    @Override
    public void put(String key, int expiration, Object object) throws CacheServiceException {
        cacheService.put(prefix + key, expiration, object);
    }

    @Override
    public void delete(String key) throws CacheServiceException {
        cacheService.delete(prefix + key);
    }

    @Override
    public void clear() {
        namespaceVersion++;
        makePrefix();
    }

    private void makePrefix() {
        StringBuilder prefix = new StringBuilder(namespace);
        prefix.append(Integer.toString(namespaceVersion)).append(":");
        this.prefix = prefix.toString();
    }

}
