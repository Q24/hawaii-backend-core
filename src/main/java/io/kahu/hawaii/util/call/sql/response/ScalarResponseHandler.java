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
package io.kahu.hawaii.util.call.sql.response;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.exception.ServerException;
import org.apache.http.annotation.ThreadSafe;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@ThreadSafe
public class ScalarResponseHandler<R extends ResultSet, T> implements ResponseHandler<ResultSet, T> {
    private RowMapper<T> rowMapper;

    public ScalarResponseHandler(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }
    @Override
    public void addToResponse(ResultSet resultSet, Response<T> response) throws ServerException {
        try {
            if (!resultSet.isAfterLast()) {
                resultSet.next();
            }
            if (resultSet.isBeforeFirst()) {
                // Cry bloody murder!
            }
            response.set(rowMapper.mapRow(resultSet, 0));
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }
    }
}
