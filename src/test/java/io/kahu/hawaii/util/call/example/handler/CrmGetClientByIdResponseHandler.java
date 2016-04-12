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
package io.kahu.hawaii.util.call.example.handler;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.example.domain.Person;
import io.kahu.hawaii.util.call.http.response.AbstractHttpResponseHandler;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;

public class CrmGetClientByIdResponseHandler extends AbstractHttpResponseHandler<Person> {
    public CrmGetClientByIdResponseHandler() {
        super(true);
    }

    protected void doAddToResponse(HttpResponse payload, Response<Person> response) throws Exception {
        JSONObject json = new JSONObject(response.getRawPayload());

        Person person = new Person();
        person.setId(json.optString("id"));
        person.setName(json.optString("name"));
        response.set(person);
    }
}
