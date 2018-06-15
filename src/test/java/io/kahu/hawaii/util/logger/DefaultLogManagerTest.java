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

import io.kahu.hawaii.util.exception.HawaiiException;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.*;

import static org.mockito.Mockito.*;

public class DefaultLogManagerTest {
    private Logger logger;
    private DefaultLogManager logManager;
    private LoggingConfiguration config;
    private String lineSeparator;

    // These patterns are used to make the logging output under test
    // predictable.

    // The logging output contains log.loc="..." which is the code location
    // where the logging is performed.
    // But this changes whenever this test class is modified.
    // The location is simply replaced with log.loc="the-location" for testing.

    // The logging output also contains tx.duration=#
    // This is normally 0 for unit tests since the tests complete very quickly.
    // But occasionally,
    // a test will just happen to span a millisecond boundary, meaning
    // tx.duration will be 1 or larger.
    // The duration is fixed at 42 for testing.

    private static Pattern locpattern = Pattern.compile("log.loc=\"io.kahu.hawaii.util.logger.DefaultLogManagerTest:\\d+\"");
    private static Pattern durationpattern = Pattern.compile("tx.duration=[\\d\\.]+ ");

    private String normalizeLogOutput(String message) {
        String s = message;
        s = locpattern.matcher(s).replaceFirst("log.loc=\"the-location\"");
        s = durationpattern.matcher(s).replaceFirst("tx.duration=42 ");
        return s;
    }

    @Before
    public void setUp() {
        lineSeparator = System.getProperty("line.separator");

        LoggingContext.remove();

        logger = mock(Logger.class);
        when(logger.isEnabledFor(any(Level.class))).thenReturn(true);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);

        config = new LoggingConfiguration();
        config.setTracingFields(new String[] { "log.loc", "client.ip", "client.session.id", "user.email", "user.msisdn", "user.username", "user.partyid",
                "user.cmagent", "context.id", "thread.id", "tx.id", "tx.type" });

        LogManagerConfiguration cfg = new LogManagerConfiguration(config);
        logManager = new DefaultLogManager(cfg);
        logManager.addLogger(CoreLoggers.SERVER, logger);
    }

    @After
    public void tearDown() {
        LoggingContext.remove();

        logger = null;
        logManager = null;
    }

    @Test
    public void testPlainLogWithMessage() {
        logManager.info(CoreLoggers.SERVER, "foo");

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals("log.loc=\"the-location\" message=#foo", message);
    }

    @Test
    public void testPlainLogWithMessageAndContext() {
        LoggingContext.get().put("bar", "baz");
        logManager.info(CoreLoggers.SERVER, "foo");

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals("log.loc=\"the-location\" bar=\"baz\" message=#foo", message);
    }

    @Test
    public void testPlainLogWithMessageAndLiteralOverride() {
        LoggingContext.get().put("bar", "baz");
        logManager.literalField("bar").info(CoreLoggers.SERVER, "foo");

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals("log.loc=\"the-location\" message=\"foo\" bar=#baz", message);
    }

    @Test
    public void testPlainLogWithThrowable() {
        Throwable t = new IllegalArgumentException("illegal-argument");
        logManager.error(CoreLoggers.SERVER, t);

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger).log(eq(Level.ERROR), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertTrue(message.startsWith("log.loc=\"the-location\" exception.class=\"java.lang.IllegalArgumentException\" "
                + "exception.message=\"illegal-argument\" " + "exception.trace=#java.lang.IllegalArgumentException: illegal-argument" + lineSeparator
                + " \tat io.kahu.hawaii.util.logger.DefaultLogManagerTest.testPlainLogWithThrowable"));
    }

    @Test
    public void testPlainLogWithThrowableAndLiteralOverride() {
        LoggingContext.get().put("bar", "baz");
        Throwable t = new IllegalArgumentException("illegal-argument");
        logManager.literalField("bar").error(CoreLoggers.SERVER, t);

        String escapedLineSeparator = StringEscapeUtils.escapeJava(lineSeparator);

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger).log(eq(Level.ERROR), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertTrue(message.startsWith("log.loc=\"the-location\" exception.class=\"java.lang.IllegalArgumentException\" "
                + "exception.message=\"illegal-argument\" exception.trace=\"java.lang.IllegalArgumentException: illegal-argument" + escapedLineSeparator
                + "\\tat io.kahu.hawaii.util.logger.DefaultLogManagerTest.testPlainLogWithThrowableAndLiteralOverride"));
        Assert.assertTrue(message.endsWith(" bar=#baz"));
    }

    @Test
    public void testLogIncomingCallStart() throws JSONException {
        LoggingContext.get().put("client.ip", "127.0.0.1");
        JSONObject params = new JSONObject();
        params.put("foo", "bar");
        logManager.logIncomingCallStart("the-type", "the-body", params);

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals(
                "log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.type=\"the-type\" tx.request.params={\"foo\":\"bar\"} tx.request.body=\"the-body\"",
                message);

        verify(logger, times(0)).log(eq(Level.DEBUG), any(String.class));
    }

    @Test
    public void testLogIncomingCallStartWithLargeBody() throws JSONException {
        LoggingContext.get().put("client.ip", "127.0.0.1");
        JSONObject params = new JSONObject();
        params.put("foo", "bar");
        logManager.logIncomingCallStart("the-type", "01234567890123456789012345678901234567890123456789", params);

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals(
                "log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.type=\"the-type\" tx.request.params={\"foo\":\"bar\"} tx.request.body=\"0123456789012345678901234567890123456789[...]\"",
                message);

        arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.DEBUG), arg.capture());
        message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals(
                "log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.type=\"the-type\" tx.request.body=#01234567890123456789012345678901234567890123456789",
                message);
    }

    @Test
    public void testLogIncomingCallEnd() throws JSONException {
        LoggingContext.get().put("client.ip", "127.0.0.1");
        logManager.logIncomingCallEnd(200, "the-body");

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals("log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.result=0 tx.response.status=200 tx.duration=42 tx.response.body=\"the-body\"",
                message);

        verify(logger, times(0)).log(eq(Level.DEBUG), any(String.class));
    }

    @Test
    public void testLogIncomingCallEndWithLargeBody() throws JSONException {
        LoggingContext.get().put("client.ip", "127.0.0.1");
        logManager.logIncomingCallEnd(200, "01234567890123456789012345678901234567890123456789");

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals(
                "log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.result=0 tx.response.status=200 tx.duration=42 tx.response.body=\"0123456789012345678901234567890123456789[...]\"",
                message);

        arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.DEBUG), arg.capture());
        message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals(
                "log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.result=0 tx.response.status=200 tx.duration=42 tx.response.body=#01234567890123456789012345678901234567890123456789",
                message);
    }

    @Test
    public void testLogIncomingCallEndWithException() throws JSONException {
        LoggingContext.get().put("client.ip", "127.0.0.1");
        HawaiiException exception = new ServerException(ServerError.ILLEGAL_ARGUMENT);
        logManager.logIncomingCallEnd(exception);

        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.INFO), arg.capture());
        String message = normalizeLogOutput(arg.getValue());

        Assert.assertEquals(
                "log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.result=2 tx.response.status=500 tx.duration=42 tx.response.exception=\"io.kahu.hawaii.util.exception.ServerException\" tx.response.message=\"ILLEGAL_ARGUMENT\"",
                message);

        arg = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).log(eq(Level.DEBUG), arg.capture());
        message = normalizeLogOutput(arg.getValue());

        Assert.assertTrue(message
                .startsWith("log.loc=\"the-location\" client.ip=\"127.0.0.1\" tx.result=2 tx.response.status=500 tx.duration=42 tx.response.trace=#io.kahu.hawaii.util.exception.ServerException: ILLEGAL_ARGUMENT"
                        + lineSeparator + " \tat io.kahu.hawaii.util.logger.DefaultLogManagerTest.testLogIncomingCallEndWithException"));
    }

    @Test
    public void testMaskPasswordsInQueryString() throws JSONException {
        config.setUrlFields(new String[] { "url1", "url2", "url3", "url4", "url5", "url6", "url7" });
        config.setPasswordParameters(new String[] { "password" });

        LoggingContext.get().put("url1", "http://localhost/?password=secret");
        LoggingContext.get().put("url2", "http://localhost/?foo=bar&password=secret");
        LoggingContext.get().put("url3", "http://localhost/?password=secret&foo=bar");
        LoggingContext.get().put("url4", "http://localhost/?foo=bar&password=secret&bar=baz");
        LoggingContext.get().put("url5", "http://localhost/?foo=bar");
        LoggingContext.get().put("url6", 42);
        LoggingContext.get().put("ignore", "http://localhost/?password=secret");

        logManager.maskPasswords(config);

        Assert.assertEquals("http://localhost/?password=********", LoggingContext.get().get("url1"));
        Assert.assertEquals("http://localhost/?foo=bar&password=********", LoggingContext.get().get("url2"));
        Assert.assertEquals("http://localhost/?password=********&foo=bar", LoggingContext.get().get("url3"));
        Assert.assertEquals("http://localhost/?foo=bar&password=********&bar=baz", LoggingContext.get().get("url4"));
        Assert.assertEquals("http://localhost/?foo=bar", LoggingContext.get().get("url5"));
        Assert.assertEquals("********", LoggingContext.get().get("url6"));
        Assert.assertEquals(null, LoggingContext.get().get("url7"));
        Assert.assertEquals("http://localhost/?password=secret", LoggingContext.get().get("ignore"));
    }

    @Test
    public void testMaskPasswordsInParameterObject() throws JSONException {
        config.setParameterFields(new String[] { "params1", "params2" });
        config.setPasswordParameters(new String[] { "password" });

        JSONObject o;

        o = new JSONObject("{\"password\": [\"secret\"], \"foo\": [\"bar\"]}");
        LoggingContext.get().put("params1", o);

        o = new JSONObject("{\"foo\": [\"bar\"]}");
        LoggingContext.get().put("params2", o);

        o = new JSONObject("{\"password\": [\"secret\"], \"foo\": [\"bar\"]}");
        LoggingContext.get().put("ignore", o);

        logManager.maskPasswords(config);

        Assert.assertEquals("[\"********\"]", ((JSONObject) LoggingContext.get().get("params1")).get("password").toString());
        Assert.assertEquals("[\"bar\"]", ((JSONObject) LoggingContext.get().get("params1")).get("foo").toString());
        Assert.assertEquals("[\"bar\"]", ((JSONObject) LoggingContext.get().get("params2")).get("foo").toString());
        Assert.assertEquals("[\"secret\"]", ((JSONObject) LoggingContext.get().get("ignore")).get("password").toString());
        Assert.assertEquals("[\"bar\"]", ((JSONObject) LoggingContext.get().get("ignore")).get("foo").toString());
    }

    @Test
    public void testMaskPasswordsInHeaders() throws JSONException {
        config.setHeaderFields(new String[] { "headers1", "headers2" });
        config.setPasswordParameters(new String[] { "Password" });

        JSONArray a;

        a = new JSONArray();
        a.put("Foo: Bar");
        a.put("Password: secret");
        a.put("Bar: Baz");
        LoggingContext.get().put("headers1", a);

        a = new JSONArray();
        a.put("Foo: Bar");
        LoggingContext.get().put("headers2", a);

        a = new JSONArray();
        a.put("Foo: Bar");
        a.put("Password: secret");
        a.put("Bar: Baz");
        LoggingContext.get().put("ignore", a);

        logManager.maskPasswords(config);

        Assert.assertEquals("[\"Foo: Bar\",\"Password: ********\",\"Bar: Baz\"]", LoggingContext.get().get("headers1").toString());
        Assert.assertEquals("[\"Foo: Bar\"]", LoggingContext.get().get("headers2").toString());
        Assert.assertEquals("[\"Foo: Bar\",\"Password: secret\",\"Bar: Baz\"]", LoggingContext.get().get("ignore").toString());
    }

    @Test
    public void testMaskPasswordsInBody() throws JSONException {
        config.setBodyFields(new String[] { "body1", "body2" });
        config.setBodyPasswordPatterns(new String[] { "<password>([^<]+)<" });

        LoggingContext.get().put("body1", "<xml><password>secret</password><foo>bar</foo></xml>");
        LoggingContext.get().put("body2", "<xml><foo>bar</foo></xml>");
        LoggingContext.get().put("ignore", "<xml><password>secret</password><foo>bar</foo></xml>");

        logManager.maskPasswords(config);

        Assert.assertEquals("<xml><password>********</password><foo>bar</foo></xml>", LoggingContext.get().get("body1"));
        Assert.assertEquals("<xml><foo>bar</foo></xml>", LoggingContext.get().get("body2"));
        Assert.assertEquals("<xml><password>secret</password><foo>bar</foo></xml>", LoggingContext.get().get("ignore"));
    }
}
