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
package io.kahu.hawaii.service.mail;

import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import org.springframework.util.Assert;

public class FakeMailSender implements MailSender {

    private final LogManager logManager;

    public FakeMailSender(LogManager logManager) {
        Assert.notNull(logManager);
        this.logManager = logManager;
    }

    @Override
    public void sendHtmlMail(String to, String subject, String htmlMessage) throws ServerException {
        sendMail(to, subject, htmlMessage);
    }

    @Override
    public void sendMail(String to, String subject, String text) throws ServerException {
        sendMail(to, subject, text, "");
    }

    @Override
    public void sendMail(String to, String subject, String text, String from, String... attachments) throws ServerException {
        logInfo("---------------------------------------");
        logInfo("To:         " + to);
        logInfo("From:       " + from);
        logInfo("Subject:    " + subject);
        logInfo("Message:    ");
        logInfo(text);
        for (String attachment : attachments){
            logInfo("Attachment: " + attachment);
        }
        logInfo("---------------------------------------");
    }

    private void logInfo(String message) {
        logManager.info(CoreLoggers.FAKE_EMAIL, message);
    }
}
