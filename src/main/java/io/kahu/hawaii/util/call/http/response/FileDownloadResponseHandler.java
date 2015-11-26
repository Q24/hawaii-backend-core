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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.http.util.HttpResponseToOutputStreamWriter;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

public class FileDownloadResponseHandler implements ResponseHandler<HttpResponse, FileDownload> {
    private final FileDownload fileDownload;

    public FileDownloadResponseHandler(final FileDownload fileDownload) throws ServerException {
        if (fileDownload == null) {
            throw new ServerException(ServerError.IO, "FileDownload not specified!");
        }
        this.fileDownload = fileDownload;
    }

    @Override
    public void addToResponse(final HttpResponse payload, final Response<FileDownload> response) throws ServerException {
        StatusLine statusLine = payload.getStatusLine();
        response.setStatusLine(statusLine.toString());
        response.setStatusCode(statusLine.getStatusCode());
        List<Header> l = new ArrayList<>();
        if (payload.getAllHeaders() != null) {
            for (Header header : payload.getAllHeaders()) {
                l.add(header);
            }
        }
        response.setHeaders(l);

        try {
            doAddToResponse(payload, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void doAddToResponse(final HttpResponse payload, final Response<FileDownload> response) throws Exception {
        fileDownload.writeHttpStatus(response.getStatusCode());
        fileDownload.writeHeaders();

        HttpResponseToOutputStreamWriter writer = new HttpResponseToOutputStreamWriter();
        writer.writeResponseToOutputStream(payload, fileDownload.getOutputStream());

        fileDownload.close();
        response.set(fileDownload);
    }

}
