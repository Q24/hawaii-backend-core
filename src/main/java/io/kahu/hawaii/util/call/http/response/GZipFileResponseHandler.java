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
package io.kahu.hawaii.util.call.http.response;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.ResponseStatus;
import io.kahu.hawaii.util.call.http.util.HttpResponseToGZipConverter;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.io.File;

import org.apache.http.HttpResponse;

public class GZipFileResponseHandler extends AbstractHttpResponseHandler<File> implements ResponseHandler<HttpResponse, File> {

    private final File file;

    public GZipFileResponseHandler(File file) {
        super(false);
        this.file = file;
    }

    @Override
    public void doAddToResponse(HttpResponse payload, Response<File> response) throws Exception {
        response.setStatus(ResponseStatus.SUCCESS);
        HttpResponseToGZipConverter converter = new HttpResponseToGZipConverter();
        if (file == null) {
            throw new ServerException(ServerError.IO, "File not specified!");
        }
        converter.toFile(payload, file);

        response.set(file);
    }

}
