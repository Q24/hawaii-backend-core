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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.io.File;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DefaultMailSenderTest {
    private MailConnection mockedMailConnection;
    private Properties mailProperties;
    private MimeMessage mockedMimeMessage;
    private MailSender mailSender;

    @Before 
    public void setUp() {
        mockedMailConnection = mock(MailConnection.class);
        mockedMimeMessage = mock(MimeMessage.class);
        mailProperties = new Properties();
        mailSender = new DefaultMailSender(new HawaiiProperties(mailProperties),mockedMailConnection);

    }

    @Test
    public void assureThatMailIsSent() throws Exception {
        String to = "hello@test.com";
        String mailFrom = "jopie@test.nl";
        String subject = "unit test for mail sending";
        String content = "mail body";
        mailProperties.put("mail.from", mailFrom);
        when(mockedMailConnection.createMessage()).thenReturn(mockedMimeMessage);
        mailSender.sendMail(to, subject, content);

        verify(mockedMimeMessage).addRecipients(eq(Message.RecipientType.TO), eq(InternetAddress.parse(to)));
        verify(mockedMimeMessage).setFrom(eq(new InternetAddress(mailFrom)));
        verify(mockedMimeMessage).setSubject(eq(subject));

        verify(mockedMailConnection).sendMail(mockedMimeMessage);
        verify(mockedMailConnection).connectToMailServer();
        verify(mockedMailConnection).disconnectFromMailServer();
    }
    
    @Test
    public void assureThatMailWithAttachmentIsSent() throws Exception {
        String to = "hello@test.com";
        String mailFrom = "jopie@test.nl";
        String subject = "unit test for mail sending";
        String content = "mail body";
        String attachment1 = File.separator + "tmp" + File.separator + "001" + File.separator + "demo.txt";
        String attachment2 = File.separator + "tmp" + File.separator + "002" + File.separator + "demo1.txt";
        
        mailProperties.put("mail.from", mailFrom);
        when(mockedMailConnection.createMessage()).thenReturn(mockedMimeMessage);
        mailSender.sendMail(to, subject, content, mailFrom, attachment1, attachment2);
        
        ArgumentCaptor<Multipart> arg = ArgumentCaptor.forClass(Multipart.class);
        verify(mockedMimeMessage).setContent(arg.capture());
        Multipart mp = arg.getValue();
        
        assertThat("Wrong number of body parts", mp.getCount(), is(equalTo(3)));        
    }


    @Test
    public void assureThatServerExceptionWithMailErrorIsSent() throws Exception {
        String to = "hello@test.com";
        String subject = "unit test for mail sending";
        String content = "mail body";
        doThrow(new MessagingException()).when(mockedMailConnection).connectToMailServer();
        try {
            mailSender.sendMail(to, subject, content);
        } catch (ServerException exception) {

            assertThat(exception.getError().getErrorName(), is(equalTo(ServerError.MAIL_ERROR.getErrorName())));
        }
    }

    @Test
    public void assureAttachmentFileNameIsCorrect() throws Exception {
        String attachment1 = File.separator + "tmp" + File.separator + "001" + File.separator + "demo.txt";
        String expectedFileName = "demo.txt";
        String actualFileName1 = mailSender.getAttachmentFileName(attachment1);
        assertThat("fileName should be correct", expectedFileName.equals(actualFileName1));
    }
}
