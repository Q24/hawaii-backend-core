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
package io.kahu.hawaii.util.call.dispatch;

import io.kahu.hawaii.util.call.*;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

public class ResponseHandlerTest extends AbstractDispatcherFrameworkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private ExecutorRepository executorRepository;

    private RequestDispatcher requestDispatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void setUp(int maxSize) {
        createExecutor(1, maxSize, 1);
        when(executorRepository.getExecutor(anyObject())).thenReturn(getExecutor());
        when(executorRepository.getAsyncExecutor(anyObject())).thenReturn(getExecutor());

        requestDispatcher = new RequestDispatcher(executorRepository, getLogManager());
    }

    @Test
    public void testHandleResponse() throws InterruptedException, ServerException {
        setUp(1);

        TestRequest request = createRequest(1, (String payload, Response<String> response) -> {
                assertThat(payload, is(equalTo("Backend response.")));
                response.set("Some other response");
            });


        Response<String> response = dispatch(requestDispatcher, request);

        Thread.sleep(100);
        verifyStatistics(1, 0, 0, 0);

        request.proceed();

        response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);

        assertThat(response.get(), is(equalTo("Some other response")));
    }

    @Test
    public void testHandleResponseMustSetResponseStatus() throws InterruptedException, ServerException {
        setUp(1);

        TestRequest request = createRequest(1, (String payload, Response<String> response) -> assertThat(payload, is(equalTo("Backend response."))) );


        Response<String> response = dispatch(requestDispatcher, request);

        request.proceed();

        thrown.expect(ServerException.class);
        thrown.expectMessage(is("METHOD_ERROR - Response handler did not set the response status."));
        response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);
    }

    @Test
    public void testThrownExceptionIsWrappedInServerException() throws InterruptedException, ServerException {
        setUp(1);

        TestRequest request = createRequest(1, (String payload, Response<String> response) -> {throw new IllegalArgumentException("Foo");} );


        Response<String> response = dispatch(requestDispatcher, request);

        request.proceed();

        thrown.expect(ServerException.class);
        thrown.expectMessage(is("UNEXPECTED_EXCEPTION - Execution exception"));
        response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);
    }

    @Test
    public void testServerExceptionIsPassedThrough() throws InterruptedException, ServerException {
        setUp(1);

        ServerException e = new ServerException(ServerError.IO, "Some message");
        TestRequest request = createRequest(1, (String payload, Response<String> response) -> {throw e;} );


        Response<String> response = dispatch(requestDispatcher, request);

        request.proceed();

        thrown.expect(ServerException.class);
        thrown.expect(is(e));
        response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);
    }


    @Test
    public void testNullAsResponse() throws InterruptedException, ServerException {
        setUp(1);

        TestRequest request = createRequest(1, (String payload, Response<String> response) -> response.set(null) );


        Response<String> response = dispatch(requestDispatcher, request);

        request.proceed();

        String value = response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);
        assertThat(value, is(equalTo(null)));
    }


    @Test
    public void testHandleResponseWithInternalFailure() throws InterruptedException, ServerException {
        setUp(1);

        TestRequest request = createRequest(1, (String payload, Response<String> response) -> response.setStatus(ResponseStatus.INTERNAL_FAILURE, "some message") );


        Response<String> response = dispatch(requestDispatcher, request);

        request.proceed();

        thrown.expect(ServerException.class);
        thrown.expectMessage(is("UNEXPECTED_EXCEPTION - some message"));
        response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);
    }



}
