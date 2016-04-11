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
import java.util.ArrayList;
import java.util.List;

@ThreadSafe
public class ListResponseHandler<R extends ResultSet, T> implements ResponseHandler<ResultSet, List<T>> {
    private RowMapper<T> rowMapper;

    public ListResponseHandler(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }
    @Override
    public void addToResponse(ResultSet resultSet, Response<List<T>> response) throws ServerException {
        try {
            if (resultSet.isBeforeFirst()) {
                // Cry bloody murder!
            }
            int i = 0;
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.mapRow(resultSet, i));
                i++;
            }
            response.set(list);
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }
    }
}
