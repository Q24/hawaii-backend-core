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
package io.kahu.hawaii.service.sms;

import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.LogManager;

public class FakeSmsSender implements SMSSender {

    private LogManager logManager;

    public FakeSmsSender(LogManager logManager) {
        this.logManager = logManager;
        
        logManager.audit("Using FakeSmsSender");
    }
    
    @Override
    public void sendSms(Sms sms) throws ServerException {
        logManager.audit("Sending (fake) SMS: " + sms);
    }

}
