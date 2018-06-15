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
package io.kahu.hawaii.util.logger;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class RequestLogBuilderTest {
    public LogManager logManager;

    @Before
    public void setUp() {
        logManager = mock(DefaultLogManager.class);
    }

    @After
    public void tearDown() {
        logManager = null;
    }

    @Test
    public void testDirectLog() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder.logIncoming();

        JSONObject params = new JSONObject();
        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testBody() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.body("the-body");
        builder.logIncoming();

        JSONObject params = new JSONObject();
        verify(logManager).logIncomingCallStart(eq("the-type"), eq("the-body"), eq(params));
    }

    @Test
    public void testNullParamValue() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.paramNull("the-param");
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", JSONObject.EXPLICIT_NULL);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testBooleanParamValue() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", true);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", true);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testIntParamValue() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", 42);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", 42);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testLongParamValue() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", 42L);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", 42L);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testDoubleParamValue() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", 42.1D);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", 42.1D);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testStringParamValue() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", "the-value");
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", "the-value");

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testObjectParamValue() throws Exception {
        JSONObject o = new JSONObject();
        o.put("foo", "bar");
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", o);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", o);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testArrayParamValue() throws Exception {
        JSONArray a = new JSONArray();
        a.put(42);
        a.put("foo");
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", a);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        params.put("the-param", a);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testExcludeParam() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.param("the-param", "the-value");
        builder = builder.excludeParam("the-param");
        builder.logIncoming();

        JSONObject params = new JSONObject();

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testFormParams() throws Exception {
        MultiValueMap<String, String> m = new LinkedMultiValueMap<String, String>();
        m.add("foo", "bar");
        m.add("foo", "baz");
        m.add("fred", "derf");
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.formParams(m);
        builder.logIncoming();

        JSONObject params = new JSONObject();
        JSONArray a = new JSONArray();
        a.put("bar");
        a.put("baz");
        params.put("foo", a);
        a = new JSONArray();
        a.put("derf");
        params.put("fred", a);

        verify(logManager).logIncomingCallStart(eq("the-type"), isNull(String.class), eq(params));
    }

    @Test
    public void testHeaderFromKV() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.header("the-header", "the-value");
        builder.logOutgoing();

        JSONObject params = new JSONObject();
        List<Header> headers = new ArrayList<>();
        headers.add(new TestHeaderImpl("the-header", "the-value"));

        verify(logManager).logOutgoingCallStart(eq("the-type"), isNull(String.class), isNull(String.class), isNull(String.class), eq(headers), isNull(String.class), eq(params));
    }

    @Test
    public void testHeader() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.header(new TestHeaderImpl("the-header", "the-value"));
        builder.logOutgoing();

        JSONObject params = new JSONObject();
        List<Header> headers = new ArrayList<>();
        headers.add(new TestHeaderImpl("the-header", "the-value"));

        verify(logManager).logOutgoingCallStart(eq("the-type"), isNull(String.class), isNull(String.class), isNull(String.class), eq(headers), isNull(String.class), eq(params));
    }

    @Test
    public void testHeaders() throws Exception {
        Header[] l1 = new Header[2];
        l1[0] = new TestHeaderImpl("the-header-1", "the-value-1");
        l1[1] = new TestHeaderImpl("the-header-2", "the-value-2");
        List<Header> l2 = new ArrayList<>();
        l2.add(new TestHeaderImpl("the-header-3", "the-value-3"));
        l2.add(new TestHeaderImpl("the-header-4", "the-value-4"));
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.headers(l1);
        builder = builder.headers(l2);
        builder.logOutgoing();

        JSONObject params = new JSONObject();
        List<Header> headers = new ArrayList<>();
        headers.add(new TestHeaderImpl("the-header-1", "the-value-1"));
        headers.add(new TestHeaderImpl("the-header-2", "the-value-2"));
        headers.add(new TestHeaderImpl("the-header-3", "the-value-3"));
        headers.add(new TestHeaderImpl("the-header-4", "the-value-4"));

        verify(logManager).logOutgoingCallStart(eq("the-type"), isNull(String.class), isNull(String.class), isNull(String.class), eq(headers), isNull(String.class), eq(params));
    }

    @Test
    public void testId() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.id("the-id");
        builder.logOutgoing();

        JSONObject params = new JSONObject();
        List<Header> headers = new ArrayList<>();

        verify(logManager).logOutgoingCallStart(eq("the-type"), eq("the-id"), isNull(String.class), isNull(String.class), eq(headers), isNull(String.class), eq(params));
    }

    @Test
    public void testMethod() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.method("the-method");
        builder.logOutgoing();

        JSONObject params = new JSONObject();
        List<Header> headers = new ArrayList<>();

        verify(logManager).logOutgoingCallStart(eq("the-type"), isNull(String.class), eq("the-method"), isNull(String.class), eq(headers), isNull(String.class), eq(params));
    }

    @Test
    public void testUri() throws Exception {
        RequestLogBuilder builder = new RequestLogBuilder(logManager, "the-type");
        builder = builder.uri("the-uri");
        builder.logOutgoing();

        JSONObject params = new JSONObject();
        List<Header> headers = new ArrayList<>();

        verify(logManager).logOutgoingCallStart(eq("the-type"), isNull(String.class), isNull(String.class), eq("the-uri"), eq(headers), isNull(String.class), eq(params));
    }
}
