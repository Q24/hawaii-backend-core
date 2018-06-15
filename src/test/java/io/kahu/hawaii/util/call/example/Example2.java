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
package io.kahu.hawaii.util.call.example;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.RequestPrototype;
import io.kahu.hawaii.util.call.TimeOut;
import io.kahu.hawaii.util.call.dispatch.ExecutorRepository;
import io.kahu.hawaii.util.call.dispatch.HawaiiExecutorImpl;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.example.domain.Person;
import io.kahu.hawaii.util.call.example.service.ClientResource;
import io.kahu.hawaii.util.call.example.handler.GetCustomerByIdResponseHandler;
import io.kahu.hawaii.util.call.example.service.RestServer;
import io.kahu.hawaii.util.call.http.HttpRequestBuilder;
import io.kahu.hawaii.util.call.http.HttpRequestContext;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.call.log.CallLoggerImpl;
import io.kahu.hawaii.util.call.log.request.HttpRequestLogger;
import io.kahu.hawaii.util.call.log.response.JsonPayloadResponseLogger;
import io.kahu.hawaii.util.logger.DefaultLogManager;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LogManagerConfiguration;
import io.kahu.hawaii.util.logger.LoggingConfiguration;
import org.apache.http.HttpResponse;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Example2 {
    private static final int SERVER_PORT = 8080;



    public static final void main(String[] args) throws Exception {
        DOMConfigurator.configure(Example2.class.getResource("/log4j.xml").getFile());

        RestServer server = null;
        ExecutorRepository executorRepository = null;
        try {
            /*
             * Create our rest server with a 'ClientResource'.
             */
            server = new RestServer(SERVER_PORT);
            server.addResource(ClientResource.class);
            server.start();

            /*
             * START of generic setup
             */
            // Create a log manager (purpose and explanation out of scope for this example).
            LogManager logManager = new DefaultLogManager(new LogManagerConfiguration(new LoggingConfiguration()));

            // Create an executor, which holds a queue with core size 1, max size 2, a queue of size 2. Threads 'outside the core pool' that are still active after one minute will get cleaned up.
            HawaiiExecutorImpl executor = new HawaiiExecutorImpl(ExecutorRepository.DEFAULT_EXECUTOR_NAME, 1, 2, 2, new TimeOut(1, TimeUnit.MINUTES), logManager);
            // Create an executor, which holds a queue with core size 1, max size 2, a queue of size 2. Threads 'outside the core pool' that are still active after one minute will get cleaned up.
            HawaiiExecutorImpl executor2 = new HawaiiExecutorImpl("crm", 1, 2, 2, new TimeOut(1, TimeUnit.MINUTES), logManager);

            // Create the repository that holds all executors
            executorRepository = new ExecutorRepository(logManager);
            executorRepository.add(executor);
            executorRepository.add(executor2);

            Map<String, String> defaultExecutors = new HashMap<>();
            defaultExecutors.put("crm", "crm");
            executorRepository.setDefaultExecutors(defaultExecutors);

            // Create a new request dispatcher.
            RequestDispatcher requestDispatcher = new RequestDispatcher(executorRepository, logManager);

            /*
             * END of generic setup
             */

            /*
             * Setup the request (builder).
             */
            HttpRequestContext<Person> context = new HttpRequestContext<>(HttpMethod.GET, "http://localhost:" + SERVER_PORT, "/client/{client-id}", "crm", "get_client_by_id", new TimeOut(10, TimeUnit.SECONDS));
            CallLogger callLogger = new CallLoggerImpl<>(logManager, new HttpRequestLogger(), new JsonPayloadResponseLogger<Person>());
            RequestPrototype<HttpResponse, Person> prototype = new RequestPrototype(requestDispatcher, context, new GetCustomerByIdResponseHandler(), callLogger);
            HttpRequestBuilder<Person> getPersonByIdRequest = new HttpRequestBuilder<>(prototype);

            /*
             * Use the request (builder).
             */
            Request<Person> request = getPersonByIdRequest.newInstance().withPathVariables("10").build();
            Person person = request.execute().get();

            System.err.println("CLIENT - Got client '" + person.getName() + "' with id '" + person.getId() + "'.");

        } finally {
            server.stop();
            if (executorRepository != null) {
                executorRepository.stop();
            }
        }

    }
}
