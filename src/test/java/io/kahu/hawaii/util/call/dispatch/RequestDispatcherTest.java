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

import io.kahu.hawaii.util.call.Response;
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
import static org.mockito.Mockito.when;

public class RequestDispatcherTest extends AbstractDispatcherFrameworkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private ExecutorServiceRepository executorRepository;

    private RequestDispatcher requestDispatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void setUp(int maxSize) {
        createExecutor(1, maxSize, 1);
        when(executorRepository.getService(anyObject())).thenReturn(getExecutor());
        when(executorRepository.getServiceMonitor(anyObject())).thenReturn(getExecutor());

        requestDispatcher = new RequestDispatcher(executorRepository, getLogManager());
    }

    @Test
    public void testExecuteRequest() throws InterruptedException, ServerException {
        setUp(1);
        TestRequest request = createRequest(1);
        Response<String> response = dispatch(requestDispatcher, request);

        Thread.sleep(30);
        verifyStatistics(1, 0, 0, 0);

        request.proceed();

        response.get();
        Thread.sleep(5);
        verifyStatistics(0, 0, 1, 0);

        assertThat(response.get(), is(equalTo("Backend response.")));
    }

    @Test
    public void testRequestTimesOut() throws InterruptedException, ServerException {
        setUp(1);
        TestRequest request = createRequest(1);
        Response<String> response = dispatch(requestDispatcher, request);

        thrown.expect(ServerException.class);
        response.get();

        assertThat(request.isAborted(), is(true));
    }

    @Test
    public void testRequestRejected() throws InterruptedException, ServerException {
        setUp(1);
        dispatch(requestDispatcher, createRequest(1));

        TestRequest request = createRequest(1);
        Response<String> response = dispatch(requestDispatcher, request);

        thrown.expect(ServerException.class);
        response.get();

        assertThat(request.isRejected(), is(true));
    }

    @Test
    public void testExecuteAsyncRequest() throws InterruptedException, ServerException {
        setUp(2);
        TestRequest request = createRequest(1);
        Response<String> response = dispatchAsync(requestDispatcher, request);

        Thread.sleep(50);
        verifyStatistics(2, 0, 0, 0);

        request.proceed();

        response.get();
        Thread.sleep(10);
        verifyStatistics(0, 0, 2, 0);

        assertThat(response.get(), is(equalTo("Backend response.")));
    }

    @Test
    public void testAsyncRequestTimesOut() throws InterruptedException, ServerException {
        setUp(2);
        TestRequest request = createRequest(1);
        Response<String> response = dispatchAsync(requestDispatcher, request);

        thrown.expect(ServerException.class);
        response.get();

        assertThat(request.isAborted(), is(true));
    }


    @Test
    public void testAsyncRequestRejected() throws InterruptedException, ServerException {
        setUp(4);
        dispatchAsync(requestDispatcher, createRequest(1));

        TestRequest request = createRequest(1);
        Response<String> response = dispatchAsync(requestDispatcher, request);

        thrown.expect(ServerException.class);
        response.get();

        assertThat(request.isRejected(), is(true));
    }

    private Response<String> dispatch(final RequestDispatcher dispatcher, final TestRequest request) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    dispatcher.execute(request);
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        };

        t.setDaemon(true);
        t.start();

        return request.getResponse();
    }

    private Response<String> dispatchAsync(final RequestDispatcher dispatcher, final TestRequest request) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    dispatcher.executeAsync(request);
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        };

        t.setDaemon(true);
        t.start();

        return request.getResponse();
    }

}