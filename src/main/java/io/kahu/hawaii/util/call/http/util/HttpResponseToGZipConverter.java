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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

public class HttpResponseToGZipConverter {
    public void toFile(HttpResponse response, File file) throws ServerException {
        FileOutputStream outputStream = null;
        GZIPOutputStream gzipOS = null;
        try {
            outputStream = new FileOutputStream(file);
            gzipOS = new GZIPOutputStream(outputStream);
            response.getEntity().writeTo(gzipOS);
        } catch (IOException e) {
            throw new ServerException(ServerError.IO, e);
        } finally {
            IOUtils.closeQuietly(gzipOS);
            IOUtils.closeQuietly(outputStream);
        }
    }
}
