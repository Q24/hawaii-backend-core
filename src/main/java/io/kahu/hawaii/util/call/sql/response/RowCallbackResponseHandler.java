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
package io.kahu.hawaii.util.call.sql.response;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import org.springframework.jdbc.core.RowCallbackHandler;

public class RowCallbackResponseHandler implements ResponseHandler<ResultSet, Void> {
    private RowCallbackHandler rowCallbackHandler;

    public RowCallbackResponseHandler(RowCallbackHandler rowCallbackHandler) {
        this.rowCallbackHandler = rowCallbackHandler;
    }

    @Override
    public void addToResponse(ResultSet payload, Response<Void> response) throws ServerException {
        try {
            while (payload.next()) {
                rowCallbackHandler.processRow(payload);
            }
            response.set(null);
        } catch (SQLException e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }
    }
}
