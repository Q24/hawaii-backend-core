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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.TimeOut;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import io.kahu.hawaii.service.sql.SqlQueryService;
import io.kahu.hawaii.util.call.configuration.RequestConfigurations;
import io.kahu.hawaii.util.logger.LogManager;

import java.util.concurrent.TimeUnit;

public class DbRequestBuilderRepositoryTest {
    private static final String SYSTEM_NAME = "test-system";
    private static final String RESOURCE_PATH = "/sql/" + SYSTEM_NAME + "/";
    private static final String QUERY_NAME = "test-query";

    private DbRequestBuilderRepository repository;
    private DbRequestPrototype prototype;
    private RequestConfigurations requestConfigurations;
    private SqlQueryService sqlQueryService;

    @Before
    public void setUp() throws Exception {
        sqlQueryService = mock(SqlQueryService.class);
        when(sqlQueryService.getSqlQuery(RESOURCE_PATH, QUERY_NAME)).thenReturn("SELECT");
        requestConfigurations = new RequestConfigurations();
        prototype = mock(DbRequestPrototype.class);
    }

    @Test
    public void thatCorrectSqlIsReturned() throws Exception {
        repository = new DbRequestBuilderRepository(RESOURCE_PATH, sqlQueryService, requestConfigurations, prototype, mock(LogManager.class));
        DbRequestBuilder builder = repository.get(QUERY_NAME);
        assertThat("wrong SQL", builder.getSql(), is(equalTo("SELECT")));
    }

    @Test
    public void thatDbRequestHasDefaultTimeOut() throws Exception {
        repository = new DbRequestBuilderRepository(RESOURCE_PATH, sqlQueryService, requestConfigurations, prototype, mock(LogManager.class));
        DbRequestBuilder builder = repository.get(QUERY_NAME);
        assertThat("wrong SQL", builder.getRequestContext().getTimeOut().toString(), is(equalTo("10 SECONDS")));
    }

    @Test
    public void thatDbRequestGetsConfiguredCallTimeOut() throws Exception {
        // Configure a timeout for this specific call
        final TimeOut timeOut = new TimeOut(13, TimeUnit.SECONDS);
        requestConfigurations.get(SYSTEM_NAME + "." + QUERY_NAME).setTimeOut(timeOut);

        repository = new DbRequestBuilderRepository(RESOURCE_PATH, sqlQueryService, requestConfigurations, prototype, mock(LogManager.class));
        DbRequestBuilder builder = repository.get(QUERY_NAME);
        assertThat("wrong timeout", builder.getRequestContext().getTimeOut(), is(timeOut));
    }

    @Test
    public void thatDbRequestGetsConfiguredSystemTimeOut() throws Exception {
        // Configure a default timeout for this system
        final TimeOut timeOut = new TimeOut(15, TimeUnit.SECONDS);
        requestConfigurations.get(SYSTEM_NAME).setTimeOut(timeOut);

        repository = new DbRequestBuilderRepository(RESOURCE_PATH, sqlQueryService, requestConfigurations, prototype, mock(LogManager.class));
        DbRequestBuilder builder = repository.get(QUERY_NAME);
        assertThat("wrong timeout", builder.getRequestContext().getTimeOut(), is(timeOut));
    }

}
