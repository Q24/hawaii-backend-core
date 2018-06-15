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
package io.kahu.hawaii.service.sms;

import io.kahu.hawaii.service.mail.MailSender;
import io.kahu.hawaii.util.exception.ServerException;

public class MailSmsSender implements SMSSender {
    private MailSender mailSender;
    private String mailTo;
    
    public MailSmsSender(MailSender mailSender, String mailTo) {
        this.mailSender = mailSender;
        this.mailTo = mailTo;
    }
    
    @Override
    public void sendSms(Sms sms) throws ServerException {
        mailSender.sendMail(mailTo, sms.getMsisdn(), sms.getMessage());
    }

}
