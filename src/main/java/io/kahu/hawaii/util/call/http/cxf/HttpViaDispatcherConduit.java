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
package io.kahu.hawaii.util.call.http.cxf;

import io.kahu.hawaii.util.call.TimeOut;
import io.kahu.hawaii.util.call.configuration.RequestConfiguration;
import io.kahu.hawaii.util.call.configuration.RequestConfigurations;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.http.HttpRequestContext;
import io.kahu.hawaii.util.call.http.SoapRequest;
import io.kahu.hawaii.util.call.http.response.SoapResponseHandler;
import io.kahu.hawaii.util.call.log.CallLoggerImpl;
import io.kahu.hawaii.util.call.log.request.HttpRequestLogger;
import io.kahu.hawaii.util.call.log.response.SoapResponseLogger;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.LogManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpMethod;

public class HttpViaDispatcherConduit extends AbstractConduit implements Conduit {
    private static final HttpClientBuilder HTTP_CLIENT_BUILDER = HttpClientBuilder.create().disableContentCompression();

    private final EndpointInfo endpointInfo;
    private MessageObserver observer;
    private final RequestConfigurations requestConfigurations;
    private final RequestDispatcher requestDispatcher;
    private final LogManager logManager;

    public HttpViaDispatcherConduit(Bus bus, EndpointInfo endpointInfo, EndpointReferenceType target, RequestConfigurations requestConfigurations,
            RequestDispatcher requestDispatcher, LogManager logManager) {
        super(target);
        this.endpointInfo = endpointInfo;
        this.requestConfigurations = requestConfigurations;
        this.requestDispatcher = requestDispatcher;
        this.logManager = logManager;
    }

    @Override
    public void setMessageObserver(MessageObserver observer) {
        this.observer = observer;
    }

    @Override
    public MessageObserver getMessageObserver() {
        return observer;
    }

    @Override
    public void prepare(Message message) {
        message.setContent(OutputStream.class, new ByteArrayOutputStream());
    }

    @Override
    public void close(Message message) throws IOException {
        OutputStream outputStream = message.getContent(OutputStream.class);
        if (outputStream != null) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) outputStream;
            String soapMessage = new String(baos.toByteArray());

            // TODO: find a better way to retrieve the SOAPAction
            String soapAction = SoapActionInInterceptor.getSoapAction(message);

            // Try to extract baseUrl and path from url, for HttpRequestContext
            String url = this.endpointInfo.getAddress();
            String baseUrl = url;
            String path = "";
            int p1 = url.indexOf("://");
            if (p1 > 0) {
                int p2 = url.indexOf("/", p1 + 3);
                if (p2 > 0) {
                    baseUrl = url.substring(0, p2);
                    path = url.substring(p2);
                }
            }

            // Try to extract systemName and methodName from logging context,
            // for HttpRequestContext
            String callType = (String) logManager.getContext("call.type");
            String systemName = "<unknown>";
            String methodName = soapAction;
            if (callType != null) {
                int p = callType.indexOf('.');
                if (p > 0) {
                    systemName = callType.substring(0, p);
                    methodName = callType.substring(p + 1);
                }
            }

            HttpRequestContext<String> context = new HttpRequestContext<>(HttpMethod.POST, baseUrl, path, systemName, methodName, new TimeOut(20,
                    TimeUnit.SECONDS));

            SoapRequest<String> soapRequest = new SoapRequest<>(requestDispatcher, context, url, soapMessage, soapAction, new SoapResponseHandler(),
                    new CallLoggerImpl<>(logManager, new HttpRequestLogger(), new SoapResponseLogger()));

            soapRequest.setHttpClientBuilder(HTTP_CLIENT_BUILDER);

            RequestConfiguration configuration = requestConfigurations.get(context.toString());
            context.setConfiguration(configuration);

            try {
                String result = soapRequest.execute().get();

                ExchangeImpl exchange = (ExchangeImpl) message.getExchange();
                MessageImpl input = new MessageImpl();
                input.setExchange(exchange);
                InputStream inputStream = new ByteArrayInputStream(result.getBytes());
                input.setContent(InputStream.class, inputStream);
                observer.onMessage(input);
            } catch (ServerException e) {
                // ignore
            }
        }
        super.close(message);
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    protected Logger getLogger() {
        return null;
    }
}
