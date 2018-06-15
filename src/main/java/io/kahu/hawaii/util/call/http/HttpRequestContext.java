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

import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.TimeOut;
import org.springframework.http.HttpMethod;

public class HttpRequestContext<T> extends RequestContext<T> {
    private final HttpMethod method;
    private String baseUrl;
    private String path;

    public HttpRequestContext(HttpMethod method, String baseUrl, String path, String backendSystem, String methodName, TimeOut timeOut) {
        super(backendSystem, methodName, timeOut);
        this.method = method;
        this.baseUrl = baseUrl;
        this.path = path;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
