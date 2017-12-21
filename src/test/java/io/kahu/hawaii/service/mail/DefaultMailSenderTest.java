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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultMailSenderTest {

    private Transport transport;
    private MimeMessage mimeMessage;

    private Properties mailProperties;
    private MailSender mailSender;

    @Before 
    public void setUp() throws Exception {
        transport = mock(Transport.class);

        mimeMessage = mock(MimeMessage.class);

        mailProperties = new Properties();

        final MailSenderHelper mailSenderHelper = mock(MailSenderHelper.class);
        when(mailSenderHelper.createTransport()).thenReturn(transport);
        when(mailSenderHelper.createMimeMessage()).thenReturn(mimeMessage);

        mailSender = new DefaultMailSender(new HawaiiProperties(mailProperties), mailSenderHelper);
    }

    @Test
    public void assureThatMailIsSent() throws Exception {
        String to = "hello@test.com";
        String mailFrom = "jopie@test.nl";
        String subject = "unit test for mail sending";
        String content = "mail body";
        mailProperties.put("mail.from", mailFrom);
        mailSender.sendMail(to, subject, content);

        final InternetAddress[] addresses = InternetAddress.parse(to);
        verify(mimeMessage).addRecipients(eq(Message.RecipientType.TO), eq(addresses));
        verify(mimeMessage).setFrom(eq(new InternetAddress(mailFrom)));
        verify(mimeMessage).setSubject(eq(subject));


        verify(transport).sendMessage(mimeMessage, addresses);
        verify(transport).close();
    }
    
    @Test
    public void assureThatAttachmentsAreAddedToTheMail() throws Exception {
        String to = "hello@test.com";
        String mailFrom = "jopie@test.nl";
        String subject = "unit test for mail sending";
        String content = "mail body";
        String attachment1 = File.separator + "tmp" + File.separator + "001" + File.separator + "demo.txt";
        String attachment2 = File.separator + "tmp" + File.separator + "002" + File.separator + "demo1.txt";
        
        mailProperties.put("mail.from", mailFrom);
        mailSender.sendMail(to, subject, content, mailFrom, attachment1, attachment2);
        
        ArgumentCaptor<Multipart> arg = ArgumentCaptor.forClass(Multipart.class);
        verify(mimeMessage).setContent(arg.capture());
        Multipart mp = arg.getValue();
        
        assertThat("Wrong number of body parts", mp.getCount(), is(equalTo(3)));        
    }


    @Test
    public void assureThatServerExceptionWithMailErrorIsSent() throws Exception {
        String to = "hello@test.com";
        String subject = "unit test for mail sending";
        String content = "mail body";
        doThrow(new MessagingException()).when(transport).sendMessage(any(), any());
        try {
            mailSender.sendMail(to, subject, content);
        } catch (ServerException exception) {

            assertThat(exception.getError().getErrorName(), is(equalTo(ServerError.MAIL_ERROR.getErrorName())));
        }
    }

    @Test
    public void assureAttachmentFileNameIsCorrect()  {
        String attachment1 = File.separator + "tmp" + File.separator + "001" + File.separator + "demo.txt";
        String expectedFileName = "demo.txt";
        String actualFileName1 = mailSender.getAttachmentFileName(attachment1);
        assertThat("fileName should be correct", expectedFileName.equals(actualFileName1));
    }
}
