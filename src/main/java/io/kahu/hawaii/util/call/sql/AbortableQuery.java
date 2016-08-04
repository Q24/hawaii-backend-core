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

import io.kahu.hawaii.util.call.*;
import io.kahu.hawaii.util.call.sql.response.UpdateIdResponseHandler;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.NotThreadSafe;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@NotThreadSafe
public class AbortableQuery<T> extends AbstractAbortableRequest<ResultSet, T> implements AbortableRequest<T>, DbCall {

    /*
     * Ensure that if the database returns a result set the timer is stopped.
     */
    // Volatile to ensure all threads see the same value!
    private volatile boolean aborted = false;
    private final DataSource dataSource;
    private final DbCallType callType ;
    private final String sql;
    private final Map<String, Object> params;
    private String idColumn;

    private PreparedStatement preparedStatement;


    public AbortableQuery(DbRequestPrototype<T> prototype, ResponseHandler<ResultSet, T> responseHandler, Map<String, Object> params) {
        this(prototype, responseHandler, prototype.getSql(), params);
    }

    public AbortableQuery(DbRequestPrototype<T> prototype, ResponseHandler<ResultSet, T> responseHandler, String sql, Map<String, Object> params) {
        super(prototype, responseHandler);
        this.dataSource = prototype.getDataSource();
        this.callType = prototype.getCallType();
        this.sql = sql;
        this.params = params;
    }

    @Override
    protected void executeInternally(ResponseHandler responseHandler, Response response) throws ServerException {
        SqlParameterSource paramSource = new MapSqlParameterSource(params);
        PreparedStatementCreator psc = getPreparedStatementCreator(sql, paramSource);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            preparedStatement = psc.createPreparedStatement(connection);

            switch (callType) {
                case INSERT:
                    int result = preparedStatement.executeUpdate();
                    if (StringUtils.isNotBlank(idColumn)) {
                        new UpdateIdResponseHandler().addToResponse(preparedStatement, response);
                    } else {
                        // No generated ID, so just set the result to the number of affected rows
                        response.set(result);
                    }
                    break;

                case DELETE:
                    // fall through
                case UPDATE:
                    response.set(preparedStatement.executeUpdate());
                    break;

                case SELECT:
                    ResultSet resultSet = preparedStatement.executeQuery();
                    responseHandler.addToResponse(resultSet, response);
                    break;

                default:
                    throw new ServerException(ServerError.ILLEGAL_ARGUMENT, "Unknown call type '" + callType + "'.");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (Throwable t) {
                //
            }
            if (!aborted) {
                response.setStatus(ResponseStatus.BACKEND_FAILURE, e);
            }
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }
        finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    protected void abortInternally() {
        try {
            aborted = true;
            preparedStatement.cancel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build a PreparedStatementCreator based on the given SQL and named parameters.
     * <p>Note: Not used for the {@code update} variant with generated key handling.
     * @param sql SQL to execute
     * @param paramSource container of arguments to bind
     * @return the corresponding PreparedStatementCreator
     */
    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);

        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
        List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);

        if (idColumn != null) {
            pscf.setGeneratedKeysColumnNames(idColumn);
        }

        return pscf.newPreparedStatementCreator(params);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public Map<String, ?> getParameters() {
        return params;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }
}
