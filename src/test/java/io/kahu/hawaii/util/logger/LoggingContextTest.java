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
package io.kahu.hawaii.util.logger;

import io.kahu.hawaii.util.logger.LoggingContext.PopResource;
import org.junit.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggingContextTest {
    private LoggingContext ctx2 = null;

    @Before
    public void setUp() {
        ctx2 = null;
        LoggingContext.remove();
    }

    @After
    public void tearDown() {
        ctx2 = null;
        LoggingContext.remove();
    }

    private void setContext2(LoggingContext ctx) {
        ctx2 = ctx;
    }

    @Test
    public void testGetInstance() throws Exception {
        LoggingContext ctx1 = LoggingContext.get();
        ctx1.put("foo", "bar");
        ctx2 = null;

        Thread t = new Thread(() -> {
            LoggingContext ctx = LoggingContext.get();
            ctx.put("fred", "derf");
            setContext2(ctx);
        });
        t.start();
        t.join();

        Assert.assertNotNull(ctx2);
        Assert.assertEquals("bar", ctx1.get("foo"));
        Assert.assertEquals(null, ctx2.get("foo"));
        Assert.assertEquals(null, ctx1.get("fred"));
        Assert.assertEquals("derf", ctx2.get("fred"));
    }

    @Test
    public void testRemove() throws Exception {
        Assert.assertEquals(null, LoggingContext.get().get("foo"));
        LoggingContext.get().put("foo", "bar");
        Assert.assertEquals("bar", LoggingContext.get().get("foo"));
        LoggingContext.remove();
        Assert.assertEquals(null, LoggingContext.get().get("foo"));
    }

    @Test
    public void testMapFunctionality() throws Exception {
        Assert.assertEquals(null, LoggingContext.get().get("foo"));
        Assert.assertFalse(LoggingContext.get().containsKey("foo"));
        Assert.assertTrue(LoggingContext.get().isEmpty());

        Assert.assertEquals(null, LoggingContext.get().put("foo", "bar"));

        Assert.assertEquals("bar", LoggingContext.get().get("foo"));
        Assert.assertTrue(LoggingContext.get().containsKey("foo"));
        Assert.assertFalse(LoggingContext.get().isEmpty());

        Assert.assertEquals("bar", LoggingContext.get().put("foo", "baz"));

        Assert.assertEquals("baz", LoggingContext.get().get("foo"));
        Assert.assertTrue(LoggingContext.get().containsKey("foo"));
        Assert.assertFalse(LoggingContext.get().isEmpty());

        Assert.assertEquals("baz", LoggingContext.get().remove("foo"));

        Assert.assertEquals(null, LoggingContext.get().get("foo"));
        Assert.assertFalse(LoggingContext.get().containsKey("foo"));
        Assert.assertTrue(LoggingContext.get().isEmpty());
    }

    @Test
    public void testPush_put() throws Exception {
        try (PopResource res = LoggingContext.get().push()) {
            LoggingContext.get().put("foo", "baz");
            LoggingContext.get().put("fred", "derf");
            Assert.assertEquals("baz", LoggingContext.get().get("foo"));
            Assert.assertEquals("derf", LoggingContext.get().get("fred"));
        }

        Assert.assertEquals(null, LoggingContext.get().get("foo"));
        Assert.assertEquals(null, LoggingContext.get().get("fred"));

        LoggingContext.get().put("foo", "bar");

        try (PopResource res = LoggingContext.get().push()) {
            LoggingContext.get().put("foo", "baz");
            LoggingContext.get().put("fred", "derf");
            Assert.assertEquals("baz", LoggingContext.get().get("foo"));
            Assert.assertEquals("derf", LoggingContext.get().get("fred"));
        }

        Assert.assertEquals("bar", LoggingContext.get().get("foo"));
        Assert.assertEquals(null, LoggingContext.get().get("fred"));
    }

    @Test
    public void testPush_remove() throws Exception {
        LoggingContext.get().put("foo", "bar");

        try (PopResource res = LoggingContext.get().push()) {
            LoggingContext.get().remove("foo");
            Assert.assertEquals(null, LoggingContext.get().get("foo"));
        }

        Assert.assertEquals("bar", LoggingContext.get().get("foo"));
    }

    @Test
    public void testFormatFields() throws Exception {
        LoggingContext.get().put("client.ip", "127.0.0.\n1");
        LoggingContext.get().put("foo", "bar");
        LoggingContext.get().put("message", "123\n456\n789");
        LoggingContext.get().put("tx.id", 123.456);
        LoggingContext.get().put("fred", "derf");

        LoggingConfiguration config = new LoggingConfiguration();
        config.setTracingFields(new String[] { "log.loc", "client.ip", "client.session.id", "user.email", "user.msisdn", "user.username", "user.partyid",
                "user.cmagent", "context.id", "thread.id", "tx.id", "tx.type" });

        Assert.assertEquals("client.ip=\"127.0.0.\\n1\" tx.id=123.456 foo=\"bar\" message=\"123\\n456\\n789\" fred=\"derf\"", LoggingContext.get()
                .formatFields(null, config.getTracingFields(), config.getComplexityThreshold()));

        Assert.assertEquals("client.ip=\"127.0.0.\\n1\" tx.id=123.456 foo=\"bar\" message=\"123\\n456\\n789\" fred=\"derf\"", LoggingContext.get()
                .formatFields("bar", config.getTracingFields(), config.getComplexityThreshold()));

        Assert.assertEquals("client.ip=\"127.0.0.\\n1\" tx.id=123.456 foo=\"bar\" fred=\"derf\" message=#123\n 456\n 789",
                LoggingContext.get().formatFields("message", config.getTracingFields(), config.getComplexityThreshold()));
    }

    @Test
    public void testGetObjectComplextity() throws Exception {
        Assert.assertEquals(1, LoggingContext.getObjectComplexity(null));
        Assert.assertEquals(1, LoggingContext.getObjectComplexity(true));
        Assert.assertEquals(1, LoggingContext.getObjectComplexity(42));
        Assert.assertEquals(1, LoggingContext.getObjectComplexity("foo"));
        JSONObject o = new JSONObject();
        Assert.assertEquals(1, LoggingContext.getObjectComplexity(o));
        o.put("foo", "bar");
        Assert.assertEquals(2, LoggingContext.getObjectComplexity(o));
        o.put("bar", "baz");
        Assert.assertEquals(3, LoggingContext.getObjectComplexity(o));
        JSONArray a = new JSONArray();
        Assert.assertEquals(1, LoggingContext.getObjectComplexity(a));
        a.put("foo");
        Assert.assertEquals(2, LoggingContext.getObjectComplexity(a));
        a.put("bar");
        Assert.assertEquals(3, LoggingContext.getObjectComplexity(a));
    }
}
