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

import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.RequestPrototype;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.log.CallLogger;
import org.apache.http.annotation.NotThreadSafe;

import javax.sql.DataSource;
import java.sql.ResultSet;

@NotThreadSafe
public class DbRequestPrototype<T> extends RequestPrototype<ResultSet, T> {
    private final DataSource dataSource;

    private String sql;

    private final DbCallType callType;

    public DbRequestPrototype(RequestDispatcher requestDispatcher, RequestContext<T> context, ResponseHandler<ResultSet, T> responseHandler, CallLogger<T> logger,  DataSource dataSource, DbCallType callType, String sql) {
        this(requestDispatcher, context, responseHandler, logger, dataSource, callType);
        this.sql = sql;
    }

    public DbRequestPrototype(RequestDispatcher requestDispatcher, RequestContext<T> context, ResponseHandler<ResultSet, T> responseHandler, CallLogger<T> logger,  DataSource dataSource, DbCallType callType) {
        super(requestDispatcher, context, responseHandler, logger);
        this.dataSource = dataSource;
        this.callType = callType;
    }

    public DbRequestPrototype(DbRequestPrototype<T> prototype) {
        super(prototype);
        this.dataSource = prototype.dataSource;
        this.callType = prototype.callType;
        this.sql = prototype.sql;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public DbCallType getCallType() {
        return callType;
    }

    public String getSql() {
        return sql;
    }

}
