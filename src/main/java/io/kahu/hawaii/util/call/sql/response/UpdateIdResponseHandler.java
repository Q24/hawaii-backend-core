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
import io.kahu.hawaii.util.call.ResponseStatus;
import io.kahu.hawaii.util.exception.ServerException;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateIdResponseHandler implements ResponseHandler<PreparedStatement, Long> {
    @Override
    public void addToResponse(PreparedStatement payload, Response<Long> response) throws ServerException {
        try {
            ResultSet keys  = payload.getGeneratedKeys();
            if (keys != null) {
                try {
                    keys.next();
                    Long keyValue = keys.getLong(1);
                    response.set(keyValue);
                }
                finally {
                    JdbcUtils.closeResultSet(keys);
                }
            }
        } catch (SQLException e) {
            response.setStatus(ResponseStatus.BACKEND_FAILURE, e);
        }
    }
}
