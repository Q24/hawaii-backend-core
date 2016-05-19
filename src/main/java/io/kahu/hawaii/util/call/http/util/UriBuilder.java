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
package io.kahu.hawaii.util.call.http.util;

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class UriBuilder {
    private static final Pattern pathVariablePattern = Pattern.compile("(\\{.+?\\})");

    private String baseUrl;
    private String path;
    private String[] pathVariables;
    private Map<String, Object> queryParameters;

    public UriBuilder withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public UriBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public UriBuilder withPathVariables(String... pathVariables) {
        this.pathVariables = pathVariables;
        return this;
    }

    public UriBuilder withQueryParameters(Map<String, Object> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public URI build() throws ServerException {
        assert baseUrl != null : "Base URL must be set.";
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (StringUtils.isNotBlank(path)) {
            String trimmedPath = StringUtils.trim(path);
            if (trimmedPath.startsWith("/") && baseUrl.endsWith("/")) {
                trimmedPath = trimmedPath.substring(1);
            }
            urlBuilder.append(trimmedPath);
        }
        if (pathVariables != null && pathVariables.length > 0) {
            String url = urlBuilder.toString();
            urlBuilder = new StringBuilder();
            substitutePathVariables(urlBuilder, url);
        }
        if (queryParameters != null && !queryParameters.isEmpty()) {
            appendQueryParameters(urlBuilder, queryParameters);
        }

        try {
            return new URI(urlBuilder.toString());
        } catch (URISyntaxException e) {
            throw new ServerException(ServerError.URI_INVALID, e.getMessage(), e);
        }
    }

    private void substitutePathVariables(StringBuilder builder, String url) {
        Matcher matcher = pathVariablePattern.matcher(url);

        int currentPos = 0;
        int matchNumber = 0;
        while (matcher.find()) {
            int matchStart = matcher.start();
            if (matchStart > currentPos) {
                builder.append(url.substring(currentPos, matchStart));
            }
            builder.append(pathVariables[matchNumber]);

            currentPos = matcher.end();
            matchNumber++;
        }
        if (currentPos < url.length()) {
            builder.append(url.substring(currentPos));
        }
        if (matchNumber != pathVariables.length) {
            System.err.println("Substitution for '" + url + "' got '" + pathVariables.length + "' variables and the number of groups is '"
                    + matcher.groupCount() + "'.");
        }
    }

    private void appendQueryParameters(StringBuilder builder, Map<String, Object> queryParameters) {
        char separator = '?';

        for (Entry<String, Object> entry : queryParameters.entrySet()) {
            builder.append(separator);
            builder.append(encodeUriComponent(entry.getKey())).append("=").append(encodeUriComponent(entry.getValue()));
            separator = '&';
        }
    }

    // TODO: proper exception handling
    private String encodeUriComponent(Object obj) {
        if (obj == null) {
            return "";
        }
        try {
            return URLEncoder.encode(obj.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

    public static final void main(String[] args) throws ServerException {
        String string = "http://localhost:9091/dynalean-stub/rest/orders/{order_id}/locations";
        URI url = new UriBuilder().withBaseUrl("http://localhost:9091/dynalean-stub").withPath("rest/orders/{order_id}/locations").withPathVariables("an_id")
                .build();

        System.out.println(string);
        System.out.println(url);

        url = new UriBuilder().withBaseUrl("http://localhost:9091/dynalean-stub").withPath("rest/orders/").build();
        System.out.println(url);

        url = new UriBuilder().withBaseUrl("http://localhost:9091/dynalean-stub/rest/quotes/{quote_id}/{email_address}").withPathVariables("one", "two")
                .build();
        System.out.println(url);
    }
}
