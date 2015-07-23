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

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;

public class DefaultMailSender implements MailSender {
    private static final String MAIL_MIME_TYPE = "text/plain; charset=utf-8";
    /** Internal storage for the mail properties */
    private HawaiiProperties properties;
    private MailConnection mailConnection;

    public DefaultMailSender(HawaiiProperties properties, MailConnection mailConnection) {
        this.properties = properties;
        this.mailConnection = mailConnection;
    }

    @Override
    public void sendHtmlMail(String to, String subject, String htmlMessage) throws ServerException {
        sendMail(to, subject, htmlMessage);
    }

    @Override
    public void sendMail(String to, String subject, String text) throws ServerException {
        sendMail(to, subject, text, properties.getProperty("mail.from"));
    }

    @Override
    public void sendMail(String to, String subject, String text, String from) throws ServerException {
        sendMail(to, subject, text, from, "");
    }

    @Override
    public void sendMail(String to, String subject, String text, String from, String attachment) throws ServerException {
        try {
            mailConnection.connectToMailServer();
            MimeMessage message = mailConnection.createMessage();
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            InternetAddress sender = new InternetAddress(from);
            InternetAddress[] senders = { sender };
            message.setFrom(sender);
            message.setReplyTo(senders);
            message.setSubject(subject);

            BodyPart bodyPart = new MimeBodyPart();

            /**
             * IMPORTANT NOTE: we don't set the mime type to text/html due to
             * possible javascript injection attacks as we don't have proper
             * server side validations yet !!!
             */
            bodyPart.setContent(text, MAIL_MIME_TYPE);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);

            if (!attachment.isEmpty()) {
                // Attachment
                bodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment);
                bodyPart.setDataHandler(new DataHandler(source));
                int index = attachment.lastIndexOf("/");
                bodyPart.setFileName(attachment.substring(index != -1 ? index : attachment.indexOf("\\")));
                multipart.addBodyPart(bodyPart);
            }

            message.setContent(multipart);
            mailConnection.sendMail(message);

        } catch (Exception e) {
            throw new ServerException(ServerError.MAIL_ERROR, e);
        } finally {
            if (mailConnection != null) {
                mailConnection.disconnectFromMailServer();
            }
        }
    }

    @Override
    public String getAttachmentFileName(String attachment) {
        return attachment.substring(attachment.lastIndexOf(File.separator) + 1);
    }

}
