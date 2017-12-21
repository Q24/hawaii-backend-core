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

import javax.mail.NoSuchProviderException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * Factory for creating a Mail Transport.
 */
public interface MailSenderHelper {

    /**
     * Create the transport.
     */
    Transport createTransport() throws NoSuchProviderException;

    /**
     * Create the MimeMessage.
     */
    MimeMessage createMimeMessage();
}
