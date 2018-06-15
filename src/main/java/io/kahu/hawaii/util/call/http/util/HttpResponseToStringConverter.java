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
package io.kahu.hawaii.util.call.http.util;

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class HttpResponseToStringConverter {
    public String toString(HttpResponse response) throws ServerException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final HttpEntity responseEntity = response.getEntity();
            // A 204 response doesn't have any content
            if (responseEntity != null) {
                responseEntity.writeTo(outputStream);
            }
        } catch (IOException e) {
            throw new ServerException(ServerError.IO, e);
        }
        return outputStream.toString();
    }
}
