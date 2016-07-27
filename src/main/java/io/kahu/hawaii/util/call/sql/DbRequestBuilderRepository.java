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

import io.kahu.hawaii.service.sql.SqlQueryService;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.configuration.RequestConfiguration;
import io.kahu.hawaii.util.call.configuration.RequestConfigurations;
import io.kahu.hawaii.util.call.sql.response.CountResponseHandler;
import io.kahu.hawaii.util.call.sql.response.ListResponseHandler;
import io.kahu.hawaii.util.call.sql.response.ResultSetResponseHandler;
import io.kahu.hawaii.util.call.sql.response.RowCallbackResponseHandler;
import io.kahu.hawaii.util.call.sql.response.ScalarResponseHandler;
import io.kahu.hawaii.util.call.sql.response.SetResponseHandler;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import java.net.URI;
import java.nio.file.*;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Stream;

public class DbRequestBuilderRepository {
    private final Map<String, DbRequestBuilder> builders = new HashMap<>();

    private final LogManager logManager;
    private final RequestConfigurations configurations;
    private final SqlQueryService queryService;
    private final String resourcePath;

    private final DbRequestPrototype prototype;


    public DbRequestBuilderRepository(String resourcePath, SqlQueryService queryService, RequestConfigurations configurations, DbRequestPrototype prototype, LogManager logManager) {
        this.resourcePath = resourcePath;
        this.queryService = queryService;
        this.configurations = configurations;

        this.prototype = prototype;

        this.logManager = logManager;

        load(resourcePath);
    }

    public DbRequestBuilderRepository add(DbRequestBuilder<?>... requestBuilder) throws ServerException {
        if (requestBuilder != null) {
            for (DbRequestBuilder<?> rb : requestBuilder) {
                doAdd(rb);
            }
        }
        return this;
    }

    private void doAdd(DbRequestBuilder<?> requestBuilder) throws ServerException {
        String name = requestBuilder.getRequestContext().getMethodName();
        if (builders.put(name, requestBuilder) != null) {
            throw new ServerException(ServerError.ILLEGAL_ARGUMENT, "DB Request with name '" + name + "' already exists.");
        }

        RequestContext requestContext = requestBuilder.getRequestContext();
        String lookup = createLookup(requestContext.getBackendSystem(), requestContext.getMethodName());

        RequestConfiguration requestConfiguration = configurations.get(lookup);
        requestContext.setConfiguration(requestConfiguration);
        requestConfiguration.setContext(requestContext);
        logManager.debug(CoreLoggers.SERVER,
                "Configuring call '" + lookup + "' to use '" + requestContext.getExecutorName() + "' with timeout '" + requestContext.getTimeOut() + "'.");
    }

    public DbRequestBuilder<?> get(String name) throws ServerException {
        DbRequestBuilder<?> builder = builders.get(name);
        if (builder == null) {
            throw new ServerException(ServerError.ILLEGAL_ARGUMENT, "DB Request with name '" + name + "' does not exist.");
        }
        return builder.newInstance();
    }

    public <T> DbRequestBuilder<T> get(String name, RowMapper<T> rowMapper) throws ServerException {
        return ((DbRequestBuilder<T>) get(name)).withResponseHandler(new ScalarResponseHandler<>(rowMapper));
    }

    public <T> DbRequestBuilder<T> get(String name, ResponseHandler<ResultSet, T> responseHandler) throws ServerException {
        return ((DbRequestBuilder<T>) get(name)).withResponseHandler(responseHandler);
    }
    
    public <T> DbRequestBuilder<T> get(String name, ResultSetExtractor<T> resultSetExtractor) throws ServerException {
        return ((DbRequestBuilder<T>) get(name)).withResponseHandler(new ResultSetResponseHandler(resultSetExtractor));
    }
    
    public DbRequestBuilder<Void> get(String name, RowCallbackHandler rowCallbackHandler) throws ServerException {
        return ((DbRequestBuilder<Void>) get(name)).withResponseHandler(new RowCallbackResponseHandler(rowCallbackHandler));
    }
    
    public <T> DbRequestBuilder<List<T>> getList(String name, RowMapper<T> rowMapper) throws ServerException {
        return ((DbRequestBuilder<List<T>>) get(name)).withResponseHandler(new ListResponseHandler<>(rowMapper));
    }

    public <T> DbRequestBuilder<Set<T>> getSet(String name, RowMapper<T> rowMapper) throws ServerException {
        return ((DbRequestBuilder<Set<T>>) get(name)).withResponseHandler(new SetResponseHandler<>(rowMapper));
    }

    public DbRequestBuilder<Long> getCount(String name) throws ServerException {
        return ((DbRequestBuilder<Long>) get(name)).withResponseHandler(new CountResponseHandler());
    }

    public <T> Page<T> getPage(String name, Map<String, Object> params, RowMapper<T> rowMapper, Pageable pageable, QueryEnhancer queryEnhancer) throws ServerException {
        return getPage(name + "_count", name + "_paged", params, rowMapper, pageable, queryEnhancer, null);
    }

    public <T> Page<T> getPage(String count, String fetch, Map<String, Object> params, RowMapper<T> rowMapper, Pageable pageable, QueryEnhancer queryEnhancer) throws ServerException {
        return getPage(count, fetch, params, rowMapper, pageable, queryEnhancer, null);
    }

    public <T> Page<T> getPage(String count, String fetch, Map<String, Object> params, RowMapper<T> rowMapper, Pageable pageable, QueryEnhancer queryEnhancer, Long maxRows) throws ServerException {
        Assert.notNull(pageable);
        
        // retrieve query...
        Long rowCount = getCount(count).withParams(params).withQueryEnhancer(queryEnhancer).withPageable(pageable).get();
        if (maxRows != null && (rowCount > maxRows)) {
            // Threshold defined and total number of records is higher
            return new PageImpl<>(new ArrayList<>(), new PageRequest(0, 1), rowCount);
        }

        queryEnhancer.addPaging(params, pageable);

        final List<T> pageItems = getList(fetch, rowMapper).withParams(params).withQueryEnhancer(queryEnhancer).withPageable(pageable).get();

        // return the page
        return new PageImpl<>(pageItems, pageable, rowCount);
    }
    
    public DbRequestBuilder<Integer> updateOrDelete(String name) throws ServerException {
        return ((DbRequestBuilder<Integer>) get(name));
    }

    private void load(final String resourcePath) {
        String directory = resourcePath;
        if (!StringUtils.startsWith(directory, "/")) {
            directory = "/" + directory;
        }
        if (!StringUtils.endsWith(directory, "/")) {
            directory += "/";
        }
        walkDirectory(directory);
    }

    private void walkDirectory(final String directory) {
        try {
            URI uri = this.getClass().getResource(directory).toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = null;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (Exception e) {
                    // ignore
                }
                if (fileSystem == null) {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                myPath = fileSystem.getPath(directory);
            } else {
                myPath = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(myPath, 1);
            walk.forEach((path) -> {
                String name = path.getFileName().toString();
                if (name.endsWith(".sql")) {
                    DbRequestPrototype p = createPrototype(directory, name);
                    if (p != null) {
                        try {
                            add(new DbRequestBuilder(p));
                        } catch (ServerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DbRequestPrototype createPrototype(String directory, String name) {
        try {
            String n = StringUtils.replace(name, ".sql", "");
            String sql = queryService.getSqlQuery(directory, n);
            String system = directory.substring("/sql/".length());
            system = system.replaceAll("/","");
            RequestContext context = new RequestContext(system, n);
            return new DbRequestPrototype(prototype.getRequestDispatcher(), context, null, prototype.getLogger(), prototype.getDataSource(), getCallType(sql), sql);
        } catch (ServerException e) {
            e.printStackTrace();
        }

        return null;
    }

    private DbCallType getCallType(String sql) {
        String command = sql.toUpperCase();
        if (command.contains("UPDATE")) {
            return DbCallType.UPDATE;
        } else if (command.contains("DELETE")) {
            return DbCallType.DELETE;
        } else if (command.contains("INSERT")) {
            return DbCallType.INSERT;
        } else {
            return DbCallType.SELECT;
        }
    }
    /*
     *
     */
    private String createLookup(String system, String method) {
        return system + "." + method;
    }
}
