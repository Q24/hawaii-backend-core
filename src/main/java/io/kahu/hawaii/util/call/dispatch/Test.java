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
package io.kahu.hawaii.util.call.dispatch;

import io.kahu.hawaii.util.call.AbstractAbortableRequest;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.call.log.CallLoggerImpl;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.DefaultLogManager;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LogManagerConfiguration;
import io.kahu.hawaii.util.logger.LoggingConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.xml.DOMConfigurator;

public class Test {

    public static final void main(String[] args) throws Exception {
        Test t = new Test();
        t.test();
    }

    public void test() throws Exception {
        int amount = 120;
        DOMConfigurator.configure("/home/tapir/vf/src/kahuna-backend/conf/log4j.xml");

        LoggingConfiguration config = new LoggingConfiguration();
        LogManagerConfiguration logManagerConfig = new LogManagerConfiguration(config);
        LogManager logManager = new DefaultLogManager(logManagerConfig);

        File configFile = new File("/home/tapir/vf/src/kahuna-backend/conf/dispatcher_config.json");
        ExecutorServiceRepository executorServiceRepository = new ExecutorServiceRepository(configFile, logManager, new RequestConfigurations());
        RequestDispatcher dispatcher = new RequestDispatcher(executorServiceRepository, logManager);

        List<MyRequest> requests = new ArrayList<>();

        final CountDownLatch start = new CountDownLatch(amount);
        final CountDownLatch stopped = new CountDownLatch(amount);
        CallLogger<String> callLogger = new CallLoggerImpl<String>(logManager, null, null);
        for (int i = 0; i < amount; i++) {
            final MyRequest request = new MyRequest(dispatcher, new RequestContext<String>("test", "test", 100), null, callLogger, i, stopped);
            requests.add(request);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    try {
                        start.await();
                        request.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            new Thread(r).start();
            start.countDown();
        }

        stopped.await();
        MyRequest request = new MyRequest(dispatcher, new RequestContext<String>("test", "test", 100), null, callLogger, -1, new CountDownLatch(0));
        System.out.println("Done");
        System.out.println(" *" + executorServiceRepository.getQueue(request).getQueueStatistic().toString());
        for (int i = 0; i < 70; i++) {
            new MyRequest(dispatcher, new RequestContext<String>("test", "test", 100), null, callLogger, 120 + i, stopped).execute();
            Thread.sleep(980);
        }
        // new MyRequest(dispatcher, new RequestContext<String>("test", "test",
        // 100), null, callLogger, i++, stopped);
        // final MyRequest request = new MyRequest(dispatcher, new
        // RequestContext<String>("test", "test", 100), null, callLogger, i,
        // stopped);
        //
        System.out.println(" *" + executorServiceRepository.getQueue(request).getQueueStatistic().toString());
    }

    public class MyRequest extends AbstractAbortableRequest<String, String> {
        private final int i;
        private final CountDownLatch latch;

        public MyRequest(RequestDispatcher requestDispatcher, RequestContext<String> context, ResponseHandler<String, String> responseHandler,
                CallLogger<String> logger, int i, CountDownLatch latch) {
            super(requestDispatcher, context, responseHandler, logger);
            this.i = i;
            this.latch = latch;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + i + "]";
        }

        @Override
        protected void executeInternally(ResponseHandler<String, String> responseHandler, Response<String> response) throws ServerException {
            System.out.println("Start '" + this + "'.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
            System.out.println("Stop '" + this + "'.");
        }

        @Override
        protected void abortInternally() {
            latch.countDown();
        }

        @Override
        protected void rejectInternally() {
            super.rejectInternally();
            latch.countDown();
        }

    }

}
