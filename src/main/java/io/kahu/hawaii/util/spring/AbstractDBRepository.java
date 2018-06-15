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
package io.kahu.hawaii.util.spring;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import io.kahu.hawaii.service.sql.SqlQueryService;
import io.kahu.hawaii.util.exception.ServerException;

public abstract class AbstractDBRepository<T> {
    private final NamedParameterJdbcOperations jdbcTemplate;

    private final RowMapper<T> rowMapper = new RowMapper<T>() {
        @Override
        public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return convertRow(rs);
        }
    };

    private final ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<T>() {
        @Override
        public T extractData(final ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return convertRow(rs);
            }
            return null;
        }
    };

    private final SqlQueryService queryService;
    private final String resourcePath;

    protected AbstractDBRepository(final NamedParameterJdbcOperations jdbcTemplate, final SqlQueryService queryService, final String resourcePath) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryService = queryService;
        this.resourcePath = resourcePath;
    }

    protected String getSqlQuery(final String queryId) throws ServerException {
        return queryService.getSqlQuery(resourcePath, queryId);
    }

    public NamedParameterJdbcOperations getJdbcTemplate() {
        return jdbcTemplate;
    }

    protected T query(final String sql) {
        return jdbcTemplate.query(sql, resultSetExtractor);
    }

    protected T query(final String sql, final Map<String, ?> paramMap) {
        return jdbcTemplate.query(sql, paramMap, resultSetExtractor);
    }

    protected List<T> queryList(final String sql) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    protected List<T> queryList(final String sql, final Map<String, ?> paramMap) {
        return jdbcTemplate.query(sql, paramMap, rowMapper);
    }

    protected void updateQuery(final String sql, final Map<String, ?> paramMap) {
        jdbcTemplate.update(sql, paramMap);
    }

    /**
     * Helper method which will convert a row in a ResultSet to a Java object. There cursor will already be positioned correctly and there is no need to close
     * the ResultSet after the conversion.
     */
    protected abstract T convertRow(ResultSet rs) throws SQLException;

    // Helper methods to retrieve items from a ResultSet and correctly handle
    // null values returned by the database. By default ResultSet works with
    // primitive types for numbers and booleans.

    protected Integer getInteger(final ResultSet rs, final int columnIndex) throws SQLException {
        int result = rs.getInt(columnIndex);
        return rs.wasNull() ? null : result;
    }

    protected Integer getInteger(final ResultSet rs, final String columnLabel) throws SQLException {
        int result = rs.getInt(columnLabel);
        return rs.wasNull() ? null : result;
    }

    protected Long getLong(final ResultSet rs, final int columnIndex) throws SQLException {
        long result = rs.getLong(columnIndex);
        return rs.wasNull() ? null : result;
    }

    protected Long getLong(final ResultSet rs, final String columnLabel) throws SQLException {
        long result = rs.getLong(columnLabel);
        return rs.wasNull() ? null : result;
    }

    protected Float getFloat(final ResultSet rs, final int columnIndex) throws SQLException {
        float result = rs.getFloat(columnIndex);
        return rs.wasNull() ? null : result;
    }

    protected Float getFloat(final ResultSet rs, final String columnLabel) throws SQLException {
        float result = rs.getFloat(columnLabel);
        return rs.wasNull() ? null : result;
    }

    protected Double getDouble(final ResultSet rs, final int columnIndex) throws SQLException {
        double result = rs.getDouble(columnIndex);
        return rs.wasNull() ? null : result;
    }

    protected Double getDouble(final ResultSet rs, final String columnLabel) throws SQLException {
        double result = rs.getDouble(columnLabel);
        return rs.wasNull() ? null : result;
    }

    protected Boolean getBoolean(final ResultSet rs, final int columnIndex) throws SQLException {
        boolean result = rs.getBoolean(columnIndex);
        return rs.wasNull() ? null : result;
    }

    protected Boolean getBoolean(final ResultSet rs, final String columnLabel) throws SQLException {
        boolean result = rs.getBoolean(columnLabel);
        return rs.wasNull() ? null : result;
    }

}
