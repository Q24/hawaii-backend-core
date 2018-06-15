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
package io.kahu.hawaii.util.call.example.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class RestServer {
    private final List<String> resources = new ArrayList<>();
    private int port = 8080;
    private Server jettyServer;

    private CountDownLatch latch = new CountDownLatch(1);

    public RestServer() {
        // Default constructor.
    }

    public RestServer(int port) {
        this.port = port;
    }


    public void addResource(Class<?> resource) {
        assert resource != null : "Resource cannot be null.";

        resources.add(resource.getCanonicalName());
    }

    public void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server(port);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                resources.stream().collect(Collectors.joining(",")));


        Thread thread = new Thread() {
            public void run() {
                try {
                    jettyServer.start();
                    latch.countDown();
                    jettyServer.join();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

        await();
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public void stop() throws Exception {
        jettyServer.stop();
        jettyServer.destroy();
    }

}
