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
package io.kahu.hawaii.util.call.http;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.http.response.StringResponseHandler;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.exception.ServerException;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class HttpRequestBuilderTest {

    private RequestDispatcher requestDispatcher;
    private ResponseHandler<HttpResponse, String> responseHandler;
    private HttpRequestContext<String> requestContext;
    private CallLogger<String> logger;
    private HttpRequestBuilder<String> builderWithHeaderProvider;
    private HttpRequestBuilder<String> builderWithoutHeaderProvider;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws ServerException {
        requestDispatcher = mock(RequestDispatcher.class);
        responseHandler = mock(StringResponseHandler.class);
        requestContext = new HttpRequestContext<String>(HttpMethod.GET, "http://test.com", "/testUrl", "dynalean", "get_shop_locations", 1);
        logger = mock(CallLogger.class);
        builderWithoutHeaderProvider = new HttpRequestBuilder<String>(requestDispatcher, requestContext, responseHandler, logger);
        builderWithHeaderProvider = new HttpRequestBuilder<String>(requestDispatcher, requestContext, responseHandler, logger, new TestHttpHeaderProvider());
    }

    @Test
    public void assureThatHttpHeaderProviderAddsHeader() throws ServerException {
        Request<String> request = builderWithHeaderProvider.newInstance().build();

        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader1", "TestValue1");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader2", "TestValue2");
    }

    @Test
    public void assureWithHeaderAddsHeader() throws ServerException {
        Request<String> request = builderWithoutHeaderProvider.newInstance().withHeader("TestHeader3", "TestValue3").build();

        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader3", "TestValue3");
    }

    @Test
    public void assureWithHeadersAddsHeaders() throws ServerException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("TestHeader3", "TestValue3");
        headers.put("TestHeader4", "TestValue4");
        Request<String> request = builderWithoutHeaderProvider.newInstance().withHeaders(headers).build();

        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader3", "TestValue3");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader4", "TestValue4");
    }

    @Test
    public void assureWithHeadersAndProviderAddsHeaders() throws ServerException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("TestHeader3", "TestValue3");
        headers.put("TestHeader4", "TestValue4");
        Request<String> request = builderWithHeaderProvider.newInstance().withHeaders(headers).build();

        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader1", "TestValue1");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader2", "TestValue2");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader3", "TestValue3");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader4", "TestValue4");
    }

    @Test
    public void assureWithHeaderAndProviderAddsHeaders() throws ServerException {
        Request<String> request = builderWithHeaderProvider.newInstance().withHeader("TestHeader3", "TestValue3").build();

        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader1", "TestValue1");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader2", "TestValue2");
        verifyHeader((AbortableHttpRequest<String>) request, "TestHeader3", "TestValue3");
    }

    private void verifyHeader(AbortableHttpRequest<String> request, String key, String value) {
        Header header = request.getHttpRequest().getFirstHeader(key);
        assert (header != null);
        assert (header.getValue().equals(value));
    }
}
