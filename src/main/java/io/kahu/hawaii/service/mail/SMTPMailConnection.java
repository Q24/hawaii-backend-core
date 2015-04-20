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

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class SMTPMailConnection implements MailConnection {

    private HawaiiProperties properties;
    
    /**
     * Authenticator for the SMTP server.
     */
    private class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(properties.getProperty("mail.user"), properties.getProperty("mail.password"));
        }
    }
    
    public SMTPMailConnection(HawaiiProperties properties) {
        this.properties = properties;
    }
 
    private Transport transport;
    private Session session;
    @Override
    public void connectToMailServer() throws MessagingException {
        session = Session.getDefaultInstance(properties.getProperties(), new SMTPAuthenticator());
        transport = session.getTransport();
        transport.connect();
    }
    
    @Override
    public MimeMessage createMessage() {
        MimeMessage message = new MimeMessage(session);
        return message;
    }
    @Override
    public void sendMail(MimeMessage message) throws MessagingException {
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
    }
    
    @Override
    public void disconnectFromMailServer()  {
        try {
            transport.close();
        } catch (MessagingException e) {
            //ignore
           // e.printStackTrace();
        }
    }
}
