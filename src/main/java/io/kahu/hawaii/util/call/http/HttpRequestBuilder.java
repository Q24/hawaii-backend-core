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

import io.kahu.hawaii.util.call.*;
import io.kahu.hawaii.util.call.http.util.UriBuilder;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.http.HttpMethod;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

@NotThreadSafe
public class HttpRequestBuilder<T> implements RequestBuilder<T> {
    private static final HttpClientBuilder HTTP_CLIENT_BUILDER = HttpClientBuilder.create().disableContentCompression();

    private static final String JSON = "application/json";
    private static final String TEXT = "text/plain";
    private static final String UTF_8 = "UTF-8";

    @SuppressWarnings("rawtypes")
    private Constructor<HttpRequestBuilder> constructor;

    private boolean active = false;

    private final Map<String, String> headers = new HashMap<>();
    private String mimeType = TEXT;
    private String characterEncoding = UTF_8;

    private String[] pathVariables;

    private Map<String, Object> queryParameters;

    private ResponseCallback<T> callback;

    private HttpHeaderProvider httpHeaderProvider;

    private HttpRequestCredentials credentials;

    // Post
    private String payload;
    private Map<String, Object> payloads;

    public HttpRequestBuilder(RequestPrototype<HttpResponse, T> prototype, HttpHeaderProvider httpHeaderProvider, HttpRequestCredentials credentials) throws ServerException {
        this(prototype, httpHeaderProvider);
        this.credentials = credentials;
    }

    public HttpRequestBuilder(RequestPrototype<HttpResponse, T> prototype, HttpHeaderProvider httpHeaderProvider) throws ServerException {
        this(prototype);
        this.httpHeaderProvider = httpHeaderProvider;
    }

    public HttpRequestBuilder(RequestPrototype<HttpResponse, T> prototype) throws ServerException {
        this.prototype = new RequestPrototype(prototype);
        setConstructor();
    }

    private RequestPrototype<HttpResponse, T>  prototype;

    private void setConstructor() throws ServerException {
        try {
            constructor = HttpRequestBuilder.class.getConstructor(RequestPrototype.class, HttpHeaderProvider.class, HttpRequestCredentials.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }
    }

    @SuppressWarnings("unchecked")
    public HttpRequestBuilder<T> newInstance() throws ServerException {
        try {
            return constructor.newInstance(prototype, httpHeaderProvider, credentials).activate();
        } catch (Exception e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBuilder<T> activate() {
        this.active = true;
        return this;
    }

    public HttpRequestBuilder<T> withMimeType(String mimeType) {
        assert active : "Not active.";
        this.mimeType = mimeType;
        return this;
    }

    public HttpRequestBuilder<T> withCharacterEncoding(String characterEncoding) {
        assert active : "Not active.";
        this.characterEncoding = characterEncoding;
        return this;
    }

    public HttpRequestBuilder<T> withHeader(String name, String value) {
        assert active : "Not active.";
        this.headers.put(name, value);
        return this;
    }

    public HttpRequestBuilder<T> withHeaders(Map<String, String> headers) {
        assert active : "Not active.";
        this.headers.putAll(headers);
        return this;
    }

    public HttpRequestBuilder<T> withPathVariables(String... pathVariables) {
        assert active : "Not active.";
        this.pathVariables = pathVariables;
        return this;
    }

    public HttpRequestBuilder<T> withQueryParameters(Map<String, Object> queryParameters) {
        assert active : "Not active.";
        this.queryParameters = queryParameters;
        return this;
    }

    public HttpRequestBuilder<T> withPayload(String payload) {
        assert active : "Not active.";
        this.payload = payload;
        return this;
    }

    public HttpRequestBuilder<T> withPayload(JSONObject payload) {
        assert active : "Not active";
        withMimeType(JSON);
        withPayload(payload.toString());
        return this;
    }

    public HttpRequestBuilder<T> withPayload(JSONArray payload) {
        assert active : "Not active";
        withMimeType(JSON);
        withPayload(payload.toString());
        return this;
    }

    public HttpRequestBuilder<T> withPayload(Map<String, Object> payloads) {
        assert active : "Not active";
        this.payloads = payloads;
        withMimeType("application/x-www-form-urlencoded");
        return this;
    }

    public HttpRequestBuilder<T> withCallback(ResponseCallback<T> callback) {
        this.callback = callback;
        return this;
    }

    public HttpRequestBuilder<T> withResponseHandler(ResponseHandler<HttpResponse, T> responseHandler) {
        prototype.setResponseHandler(responseHandler);
        return this;
    }

    public HttpRequestBuilder<T> withCredentials(HttpRequestCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public Request<T> build() throws ServerException {
        assert active : "Not active.";
        AbortableHttpRequest<T> request;
        URI uri = getUri();

        final HttpEntity httpEntity;
        HttpMethod method = getRequestContext().getMethod();
        switch (method) {
        case GET:
            // if payload != null throw exception?
            request = new GetRequest<>(prototype, uri);
            break;
        case POST:
            request = new PostRequest<>(prototype, uri);
            httpEntity = getPayload();
            if (httpEntity != null) {
                ((HttpPost) request.getHttpRequest()).setEntity(httpEntity);
            }
            break;
        case DELETE:
            request = new DeleteRequest<>(prototype, uri);
            break;
        case PATCH:
            request = new PatchRequest<>(prototype, uri);
            httpEntity = getPayload();
            if (httpEntity != null) {
                ((HttpPatch) request.getHttpRequest()).setEntity(httpEntity);
            }
            break;
        case PUT:
            request = new PutRequest<>(prototype, uri);
            httpEntity = getPayload();
            if (httpEntity != null) {
                ((HttpPut) request.getHttpRequest()).setEntity(httpEntity);
            }
            break;
        default:
            throw new ServerException(ServerError.METHOD_ERROR, "Method '" + method + "' is not supported.");
        }

        addHeaders();

        Set<Entry<String, String>> entrySet = headers.entrySet();
        for (Entry<String, String> entry : entrySet) {
            request.getHttpRequest().addHeader(entry.getKey(), entry.getValue());
        }

        addAuthentication(request, uri);

        request.setCallback(callback);

        request.setHttpClientBuilder(HTTP_CLIENT_BUILDER);
        return request;
    }

    private HttpEntity getPayload() {
        HttpEntity httpEntity = null;
        if (payload != null) {
            httpEntity = new StringEntity(payload, ContentType.create(mimeType, characterEncoding));
        }
        if (payloads != null) {
            List<NameValuePair> params = new ArrayList<>();
            for (Entry<String, Object> entry : payloads.entrySet()) {
                String stringValue = StringUtils.defaultString(entry.getValue());
                params.add(new BasicNameValuePair(entry.getKey(), stringValue));
            }
            try {
                httpEntity = new UrlEncodedFormEntity(params);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return httpEntity;
    }

    private void addAuthentication(AbortableHttpRequest<T> request, URI uri) {
        if (credentials != null) {
            CredentialsProvider credentialsProvider = null;

            switch (credentials.getAuthenticationType()) {
            case BASIC:
                // fall-through to default
            default:
                BasicScheme authenticationScheme = new BasicScheme();

                credentialsProvider = new BasicCredentialsProvider();
                // Explicitly set the AuthScope to ANY_REALM in order to get
                // 'preemptive authentication' to work
                credentialsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM, authenticationScheme.getSchemeName()),
                        new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword()));

                // Create an auth cache for preemptive authentication.
                AuthCache authCache = new BasicAuthCache();
                authCache.put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), authenticationScheme);
                request.getHttpClientContext().setAuthCache(authCache);

                break;
            }

            request.getHttpClientContext().setCredentialsProvider(credentialsProvider);
        }
    }

    private URI getUri() throws ServerException {
        return new UriBuilder().withBaseUrl(getRequestContext().getBaseUrl()).withPath(getRequestContext().getPath()).withPathVariables(pathVariables)
                .withQueryParameters(queryParameters).build();
    }

    @Override
    public HttpRequestContext<T> getRequestContext() {
        return (HttpRequestContext<T>) prototype.getContext();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("URI: ");
        try {
            builder.append(getUri().toString()).append("\n");
        } catch (ServerException e) {
            builder.append(e).append("\n");
            // ignored
        }
        builder.append("Body: ");
        if (payload != null) {
            builder.append(payload.toString());
        }

        if (payloads != null) {
            for (Entry<String, Object> entry : payloads.entrySet()) {
                String stringValue = StringUtils.defaultString(entry.getValue());
                builder.append(entry.getKey()).append("=").append(stringValue);
                builder.append('&');
            }
        }
        return builder.toString();
    }

    private void addHeaders() throws ServerException {
        if (httpHeaderProvider != null) {
            Map<String, String> headers = httpHeaderProvider.getHeaders();
            if (headers != null) {
                withHeaders(headers);
            }
        }
    }

}
