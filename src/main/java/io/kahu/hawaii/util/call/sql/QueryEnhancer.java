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
package io.kahu.hawaii.util.call.sql;


import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface QueryEnhancer {
    default <T> DbRequestBuilder<T> enhance(DbRequestBuilder<T> original, Pageable pageable) {
       return original.withSql(enhance(original.getSql(), pageable));
    }

    String enhance(String original, Pageable pageable);

    default void addPaging(Map<String, Object> params,  Pageable pageable) {
        params.put("start_index", pageable.getPageNumber() * pageable.getPageSize() + 1);
        params.put("end_index", (pageable.getPageNumber() + 1) * pageable.getPageSize());
    }
}
