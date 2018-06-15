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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * <p>
 * A builder that collects the information required to log a request (incoming
 * or outgoing).
 * </p>
 *
 * <p>
 * In the constructor, it starts by setting a type. This is a string that
 * identifies what kind of transaction this is. For example,
 * "my.account.getAuthenticatedUser", "sms.sendTextMessage" or
 * "backend.activateSim".
 * </p>
 *
 * <p>
 * Builder methods then gather the rest of the information: a body (as a
 * String), headers (a list of Strings), and parameters (a JSONObject). The
 * parameters can come from any source, including path parameters, query string
 * parameters and form parameters.
 * </p>
 *
 * <p>
 * When done, logIncoming() or logOutgoing() will log the request. logIncoming()
 * will only log type, body and params.
 * </p>
 *
 * @author ErnstJan.Plugge
 */
public class RequestLogBuilder {
    private LogManager logManager;
    private String type;
    private String id;
    private String method;
    private String uri;
    private List<Header> headers = new ArrayList<>();
    private String body = null;
    private JSONObject params = new JSONObject();

    public RequestLogBuilder(LogManager logManager, String type) {
        this.logManager = logManager;
        this.type = type;
    }

    public RequestLogBuilder body(String body) {
        this.body = body;
        return this;
    }

    public RequestLogBuilder paramNull(String name) {
        Assert.hasLength(name);
        try {
            this.params.put(name, JSONObject.EXPLICIT_NULL);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, boolean value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, int value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, long value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, double value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, String value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, String[] values) {
        Assert.hasLength(name);
        if (values.length > 0) {
            try {
                this.params.put(name, new JSONArray(Arrays.asList(values)));
            } catch (JSONException cant_happen) {
                //
            }
        }
        return this;
    }

    public RequestLogBuilder param(String name, JSONObject value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder param(String name, JSONArray value) {
        Assert.hasLength(name);
        try {
            this.params.put(name, value);
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder excludeParam(String name) {
        Assert.hasLength(name);
        this.params.remove(name);
        return this;
    }

    public RequestLogBuilder formParams(MultiValueMap<String, String> map) {
        Assert.notNull(map);
        try {
            for (String name : map.keySet()) {
                List<String> values = map.get(name);
                if (values.size() != 0) {
                    this.params.put(name, new JSONArray(values));
                }
            }
        } catch (JSONException cant_happen) {
            //
        }
        return this;
    }

    public RequestLogBuilder header(Header header) {
        Assert.notNull(header);
        headers.add(header);
        return this;
    }

    public RequestLogBuilder header(String name, String value) {
        Assert.hasLength(name);
        Assert.hasLength(value);
        return header(new BasicHeader(name, value));
    }

    public RequestLogBuilder headers(List<Header> headers) {
        Assert.notNull(headers);
        this.headers.addAll(headers);
        return this;
    }

    public RequestLogBuilder headers(Header[] headers) {
        Assert.notNull(headers);
        for (Header header : headers) {
            this.headers.add(header);
        }
        return this;
    }

    public RequestLogBuilder id(String id) {
        this.id = id;
        return this;
    }

    public RequestLogBuilder method(String method) {
        this.method = method;
        return this;
    }

    public RequestLogBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    public void logIncoming() {
        logManager.logIncomingCallStart(type, body, params);
    }

    public void logOutgoing() {
        logManager.logOutgoingCallStart(type, id, method, uri, headers, body, params);
    }
}
