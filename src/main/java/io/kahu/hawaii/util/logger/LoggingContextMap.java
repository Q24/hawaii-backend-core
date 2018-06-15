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
package io.kahu.hawaii.util.logger;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A simple {@link Map} implementation that keeps track of the order keys were
 * added to the map. It also stores a timestamp to keep track of when the
 * instance was created, which is used to derive the total duration of a
 * request.
 * </p>
 * 
 * <p>
 * Iterating over the entrySet of this Map will return entries in the order they
 * were added. If an entry is updated after being added to the map, its position
 * in the list is not changed, i.e. it is not bumped to the end of the entrySet.
 * </p>
 * 
 * <p>
 * This implementation explicitly does not allow null keys.
 * </p>
 * 
 * <p>
 * The clone method performs a shallow copy, as does the copy-constructor. Both
 * will add entries to the new map in whatever order is used by the source Map's
 * entrySet iterator.
 * </p>
 * 
 * <p>
 * Internally, this is a minimalist extension of AbstractMap which keeps its
 * entry set as a List.
 * </p>
 * 
 * @author ErnstJan.Plugge
 */
public class LoggingContextMap extends AbstractMap<String, Object> {
    private EntrySet entrySet = new EntrySet();
    private long created = System.nanoTime();

    public class EntrySet extends AbstractSet<Map.Entry<String, Object>> {
        private List<Map.Entry<String, Object>> entries = new ArrayList<>();

        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return entries.iterator();
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public boolean add(Map.Entry<String, Object> e) {
            if (entries.contains(e)) {
                return false;
            }
            entries.add(e);
            return true;
        }
    }

    public LoggingContextMap() {
        super();
    }

    public LoggingContextMap(LoggingContextMap map) {
        super();
        if (map != null) {
            this.putAll(map);
        }
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return entrySet;
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (Map.Entry<String, Object> entry : entrySet) {
            if (key.equals(entry.getKey())) {
                Object oldValue = entry.getValue();
                entry.setValue(value);
                return oldValue;
            }
        }
        entrySet.add(new AbstractMap.SimpleEntry<String, Object>(key, value));
        return null;
    }

    @Override
    protected LoggingContextMap clone() {
        return new LoggingContextMap(this);
    }

    /**
     * The timestamp this instance was created.
     * 
     * @return the timestamp in JVM nano time.
     */
    public long getCreated() {
        return created;
    }
}
