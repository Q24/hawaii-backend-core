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
package io.kahu.hawaii.util.json;

import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class AbstractJsonWriter<D> implements JsonResultConverter<D> {

    @Override
    public final JSONObject toJson(D domainObject) throws JSONException {
        return addToJson(new JSONObject(), domainObject);
    }

    @Override
    public final JSONObject addToJson(JSONObject json, D domainObject) throws JSONException {
        if (domainObject == null) {
            return null;
        }

        writeJson(domainObject, json);
        return json;
    }

    @Override
    public JSONArray toJson(Collection<D> domainObjects) throws JSONException {
        if (domainObjects == null) {
            return null;
        }
        JSONArray result = new JSONArray();
        for (D d : domainObjects) {
            JSONObject json = toJson(d);
            if (json.length() > 0) {
                result.put(json);
            }
        }
        return result;
    }

    /**
     *
     * @param domainObject
     *            not null
     * @param json
     * @throws JSONException
     */
    protected abstract void writeJson(D domainObject, JSONObject json) throws JSONException;
}
