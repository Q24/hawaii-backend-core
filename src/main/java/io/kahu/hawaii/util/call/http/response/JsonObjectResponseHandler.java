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

import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;

public class JsonObjectResponseHandler extends AbstractHttpResponseHandler<JSONObject> implements ResponseHandler<HttpResponse, JSONObject> {
    private final ResponseHeaderChecker checker = new ResponseHeaderChecker();

    private final String expectedContentType;

    public JsonObjectResponseHandler() {
        this("application/json");
    }

    public JsonObjectResponseHandler(String exepctedContentType) {
        super(true);
        this.expectedContentType = exepctedContentType;
    }

    @Override
    public void doAddToResponse(HttpResponse payload, Response<JSONObject> response) throws Exception {
        checker.assertContentTypeIs(payload, expectedContentType);
        response.set(new JSONObject(response.getRawPayload()));
    }
}
