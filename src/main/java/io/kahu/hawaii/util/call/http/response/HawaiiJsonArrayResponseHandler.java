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

import io.kahu.hawaii.rest.ResponseKeyConstants;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.ResponseStatus;

import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class HawaiiJsonArrayResponseHandler extends AbstractHttpResponseHandler<JSONArray> implements ResponseHandler<HttpResponse, JSONArray> {
    public HawaiiJsonArrayResponseHandler() {
        super(true);
    }

    private final ResponseHeaderChecker checker = new ResponseHeaderChecker();

    @Override
    public void doAddToResponse(HttpResponse payload, Response<JSONArray> response) throws Exception {
        checker.assertContentTypeIs(payload, "application/json");

        JSONObject jsonResponse = new JSONObject(response.getRawPayload());

        int status = jsonResponse.getInt(ResponseKeyConstants.STATUS_KEY);
        if (status != 200) {
            response.setStatus(ResponseStatus.BACKEND_FAILURE, "Got status '" + status + "' from backend.");
        } else {
            JSONArray data = jsonResponse.getJSONArray(ResponseKeyConstants.DATA_KEY);
            response.set(data);
        }
    }
}
