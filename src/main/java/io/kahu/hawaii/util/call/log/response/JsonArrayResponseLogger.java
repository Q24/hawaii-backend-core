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
package io.kahu.hawaii.util.call.log.response;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.logger.LogManager;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

public class JsonArrayResponseLogger implements ResponseLogger<JSONArray> {
    @Override
    public void logResponse(LogManager logManager, Response<JSONArray> response) {
        String type = response.getRequest().getCallName();
        String id = response.getRequest().getId();
        String body = null;
        JSONArray o = response.getResponsePayload();
        if (o != null) {
            try {
                if (logManager.isComplex(o)) {
                    body = o.toString(2);
                } else {
                    body = o.toString();
                }
            } catch (JSONException cant_happen) {
                // Ignore
            }
        }

        logManager.logOutgoingCallEnd(type, id, response, body);
    }
}
