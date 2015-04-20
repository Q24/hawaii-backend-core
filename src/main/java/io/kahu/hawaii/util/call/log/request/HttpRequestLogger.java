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
package io.kahu.hawaii.util.call.log.request;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.http.HttpCall;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.RequestLogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpRequestLogger implements RequestLogger {
    @Override
    public void logRequest(LogManager logManager, Request<?> request) {
        if (!(request instanceof HttpCall)) {
            logManager.error(CoreLoggers.SERVER_CALLS, "Request for HttpRequestLogger is not an HttpCall!");
            new GenericRequestLogger().logRequest(logManager, request);
            return;
        }

        HttpRequest httpRequest = ((HttpCall) request).getHttpRequest();
        HttpEntity entity = null;
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
        }

        RequestLogBuilder log = new RequestLogBuilder(logManager, request.getCallName());
        log = log.id(request.getId());

        if (httpRequest instanceof HttpUriRequest) {
            log = log.method(((HttpUriRequest) httpRequest).getMethod());
            log = log.uri(((HttpUriRequest) httpRequest).getURI().toString());
        }

        if (httpRequest.getAllHeaders() != null) {
            log = log.headers(httpRequest.getAllHeaders());
        }

        String body = null;
        if (entity != null) {
            if (entity.isRepeatable()) {
                try {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        entity.writeTo(baos);
                        body = baos.toString("UTF-8");
                    }
                    body = logManager.prettyPrintJson(body);
                    body = logManager.prettyPrintXml(body);
                } catch (UnsupportedEncodingException cant_happen) {
                    // Ignore
                } catch (IOException very_unlikely) {
                    logManager.debug(CoreLoggers.SERVER_CALLS, "Unable to extract HTTP body - ignoring", very_unlikely);
                }
            } else {
                body = "Unrepeatable entity of type: " + entity.getClass().getName();
            }
        }
        if (body != null) {
            log = log.body(body);
        }

        log.logOutgoing();
    }
}
