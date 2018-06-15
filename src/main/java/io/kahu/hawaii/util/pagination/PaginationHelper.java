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
package io.kahu.hawaii.util.pagination;

import org.springframework.data.domain.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaginationHelper<E> {

    Integer maxRows;

    public PaginationHelper() {
    }

    public PaginationHelper(Integer maxRows) {
        this.maxRows = maxRows;
    }

    public Page<E> fetchPage(final NamedParameterJdbcOperations jt, final String sqlCountRows, final String sqlFetchRows, final Map<String, Object> paramMap,
            final Pageable pageable, final RowMapper<E> rowMapper) {

        // first execute select count (needed for paging metadata)
        final int rowCount = jt.queryForObject(sqlCountRows, paramMap, Integer.class);

        if (maxRows != null && (rowCount > maxRows)) {
            // Threshold defined and total number of records is higher
            return new PageImpl<E>(new ArrayList<>(), new PageRequest(0, 1), rowCount);
        }
        // now fetch the actual objects (content)
        final List<E> pageItems = jt.query(sqlFetchRows, paramMap, (resultSet, rowNum) -> {
            return rowMapper.mapRow(resultSet, rowNum);
        });

        // return the page
        return new PageImpl<E>(pageItems, pageable, rowCount);
    }

    /**
     * Calculates the start index from the pageable object. As rownums start with 1 we add 1 to the result of page number * page size.
     */
    public static int getStartIndex(final Pageable pageable) {
        return (pageable.getPageNumber() * pageable.getPageSize()) + 1;
    }

    /**
     * Calculates the end index from the pageable object.
     */
    public static int getEndIndex(final Pageable pageable) {
        return (pageable.getPageNumber() + 1) * pageable.getPageSize();
    }
}
