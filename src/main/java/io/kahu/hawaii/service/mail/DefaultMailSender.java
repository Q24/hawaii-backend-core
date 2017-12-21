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
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static java.util.Objects.requireNonNull;

public class DefaultMailSender implements MailSender {

    private static final String MAIL_MIME_TYPE = "text/plain; charset=utf-8";

    /**
     * Internal storage for the mail properties
     */
    private final HawaiiProperties properties;

    /**
     * The factory to create SMTP session with for sending emails.
     */
    private final MailSenderHelper mailSenderHelper;

    public DefaultMailSender(final HawaiiProperties properties, final MailSenderHelper mailSenderHelper) {
        this.properties = requireNonNull(properties);
        this.mailSenderHelper = requireNonNull(mailSenderHelper);
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
    public void sendMail(String to, String subject, String text, String from, String... attachments) throws ServerException {
        try {
            final Transport transport = mailSenderHelper.createTransport();
            final MimeMessage message = mailSenderHelper.createMimeMessage();

            final InternetAddress[] recipients = InternetAddress.parse(to);
            message.addRecipients(Message.RecipientType.TO, recipients);

            final InternetAddress sender = new InternetAddress(from);
            message.setFrom(sender);

            final InternetAddress[] senders = {sender};
            message.setReplyTo(senders);

            message.setSubject(subject);

            final BodyPart bodyPart = new MimeBodyPart();

            /**
             * IMPORTANT NOTE: we don't set the mime type to text/html due to
             * possible javascript injection attacks as we don't have proper
             * server side validations yet !!!
             */
            bodyPart.setContent(text, MAIL_MIME_TYPE);

            final Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);

            if (attachments != null) {
                for (String attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        final MimeBodyPart attachmentPart = new MimeBodyPart();
                        final DataSource source = new FileDataSource(attachment);
                        attachmentPart.setDataHandler(new DataHandler(source));
                        attachmentPart.setFileName(getAttachmentFileName(attachment));
                        multipart.addBodyPart(attachmentPart);
                    }
                }
            }

            message.setContent(multipart);

            try {
                transport.connect();
                transport.sendMessage(message, recipients);
            } finally {
                if (transport != null) {
                    transport.close();
                }
            }

        } catch (Exception e) {
            throw new ServerException(ServerError.MAIL_ERROR, e);
        }
    }

}
