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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.RejectedExecutionException;

public class HawaiiExecutorImplTest extends AbstractDispatcherFrameworkTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRejectTask() throws InterruptedException {
        createExecutor(1, 1, 1);

        exec(createRequest());
        Thread.sleep(1);
        exec(createRequest());
        Thread.sleep(1);

        thrown.expect(RejectedExecutionException.class);
        exec(createRequest());
        verifyStatistics(1, 1, 0, 1);
    }

    @Test
    public void testExecutorFirstStartsThreadsThenQueues() throws InterruptedException {
        createExecutor(1, 2, 1);

        // r1 will be directly consumed by the core thread.
        TestRequest r1 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(1, 0, 0, 0);

        // r2 will create a new thread, which will consume r2.
        TestRequest r2 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(2, 0, 0, 0);

        // r3 will be queued.
        TestRequest r3 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(2, 1, 0, 0);

        // r4 will be rejected
        thrown.expect(RejectedExecutionException.class);
        TestRequest r4 = exec(createRequest());
        verifyStatistics(2, 1, 0, 1);

        // r5 will be rejected
        thrown.expect(RejectedExecutionException.class);
        TestRequest r5 = exec(createRequest());
        verifyStatistics(2, 1, 0, 2);
    }

    @Test
    public void testExecutorKeepsTrackOfCompletedTasks() throws InterruptedException {
        createExecutor(1, 1, 1);

        // This one is directly consumed by one of the threads
        TestRequest r1 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(1, 0, 0, 0);

        // This one is queued
        TestRequest r2 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(1, 1, 0, 0);

        r1.proceed();
        Thread.sleep(2);
        verifyStatistics(1, 0, 1, 0);

        r2.proceed();
        Thread.sleep(2);
        verifyStatistics(0, 0, 2, 0);
    }

    @Test
    public void testExecutorContinuesProcessingAfterRejectingTasks() throws InterruptedException {
        createExecutor(1, 1, 1);

        // r1 will be directly consumed by the core thread.
        TestRequest r1 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(1, 0, 0, 0);

        // r2 will be queued.
        TestRequest r2 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(1, 1, 0, 0);

        // r3 will be queued.
        thrown.expect(RejectedExecutionException.class);
        TestRequest r3 = exec(createRequest());
        verifyStatistics(1, 1, 0, 1);

        // r4 will be rejected
        thrown.expect(RejectedExecutionException.class);
        TestRequest r4 = exec(createRequest());
        verifyStatistics(1, 1, 0, 1);


        // Proceed r1 and r2
        r1.proceed();
        r2.proceed();

        Thread.sleep(2);

        // r5 will be processed normally.
        thrown.expect(RejectedExecutionException.class);
        TestRequest r5 = exec(createRequest());
        Thread.sleep(1);
        verifyStatistics(1, 0, 2, 1);
    }

}