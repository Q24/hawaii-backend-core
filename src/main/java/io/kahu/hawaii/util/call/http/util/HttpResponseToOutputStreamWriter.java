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
package io.kahu.hawaii.util.call.http.util;

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponseToOutputStreamWriter {
    public void writeResponseToOutputStream(HttpResponse response, OutputStream out) throws ServerException {
        try {
            response.getEntity().writeTo(out);
        } catch (IOException e) {
            throw new ServerException(ServerError.IO, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
