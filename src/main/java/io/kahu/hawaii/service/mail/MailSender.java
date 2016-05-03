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

import java.io.File;

import io.kahu.hawaii.util.exception.ServerException;

public interface MailSender {

    void sendHtmlMail(String to, String subject, String htmlMessage) throws ServerException;

    void sendMail(String to, String subject, String text) throws ServerException;

    void sendMail(String to, String subject, String text, String from) throws ServerException;

    void sendMail(String to, String subject, String text, String from, String... attachments) throws ServerException;

    default String getAttachmentFileName(String attachment) {
        return attachment.substring(attachment.lastIndexOf(File.separator) + 1);
    }
}
