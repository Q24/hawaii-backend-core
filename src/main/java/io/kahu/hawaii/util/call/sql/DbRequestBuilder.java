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

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.NotThreadSafe;
import org.springframework.data.domain.Pageable;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.RequestBuilder;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.ResponseCallback;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

@NotThreadSafe
public class DbRequestBuilder<T> implements RequestBuilder<T> {
    @SuppressWarnings("rawtypes")
    private Constructor<DbRequestBuilder> constructor;
    private boolean active = false;

    private DbRequestPrototype<T> prototype;
    private ResponseCallback<T> callback;
    private Map<String, Object> params;
    private String sql;
    private QueryEnhancer queryEnhancer;
    private Pageable pageable;

    private String idColumn;
    private ResponseHandler<ResultSet, T> responseHandler;

    public DbRequestBuilder(DbRequestPrototype<T> prototype) throws ServerException {
        this.prototype = prototype;
        setConstructor();
    }

    private void setConstructor() throws ServerException {
        try {
            constructor = DbRequestBuilder.class.getConstructor(DbRequestPrototype.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }
    }

    private DbRequestBuilder<T> activate() {
        this.active = true;
        return this;
    }

    @Override
    public DbRequestBuilder<T> newInstance() throws ServerException {
        try {
            return constructor.newInstance(prototype).activate();
        } catch (Exception e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }
    }

    public DbRequestBuilder<T> withParams(Map<String, Object> params) {
        assert active : "Not active.";
        this.params = params;
        return this;
    }

    public DbRequestBuilder<T> withCallback(ResponseCallback<T> callback) {
        assert active : "Not active.";
        this.callback = callback;
        return this;
    }

    public DbRequestBuilder<T> withSql(String sql) {
        assert active : "Not active";
        this.sql = sql;
        return this;
    }

    public DbRequestBuilder<T> withId(String idColumn) {
        this.idColumn = idColumn;
        return this;
    }

    public DbRequestBuilder<T> withResponseHandler(ResponseHandler<ResultSet, T> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }
    
    public DbRequestBuilder<T> withQueryEnhancer(QueryEnhancer queryEnhancer) {
        this.queryEnhancer = queryEnhancer;
        return this;
    }
    
    public DbRequestBuilder<T> withPageable(Pageable pageable) {
        this.pageable = pageable;
        return this;
    }
    
    @Override
    public Request<T> build() throws ServerException {
        assert active : "Not active.";
    
        if (queryEnhancer != null) {
            //Enhance the query. 
            //if pageable is null, the queryEnhancer should be able to handle that.
            withSql(queryEnhancer.enhance(getSql(), pageable));
        }
        AbortableQuery<T> request = null;
        if (StringUtils.isNotBlank(sql)) {
            request = new AbortableQuery<>(prototype, responseHandler, sql, params);
        } else {
            request = new AbortableQuery<>(prototype, responseHandler, params);
        }

        if (StringUtils.isNotBlank(idColumn)) {
            request.setIdColumn(idColumn);
        }
        request.setCallback(callback);

        return request;
    }

    @Override
    public RequestContext<T> getRequestContext() {
        return  prototype.getContext();
    }

    public String getSql() {
        if (sql == null) {
            return prototype.getSql();
        }
        return sql;
    }
}
