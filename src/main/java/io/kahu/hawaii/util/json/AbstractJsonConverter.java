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

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class AbstractJsonConverter<D> implements JsonConverter<D> {

    private final Class<D> targetClass;

    public AbstractJsonConverter(Class<D> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public D fromJson(JSONObject parent, String key) throws JSONException, ServerException {
        JSONObject source = parent.optJSONObject(key);
        if (source == null) {
            return null;
        }
        return fromJson(source);
    }

    @Override
    public <T extends D> T fromJson(JSONObject parent, String key, T target) throws JSONException, ServerException {
        JSONObject source = parent.optJSONObject(key);
        if (source == null) {
            return null;
        }
        return fromJson(source, target);
    }

    @Override
    public D fromJson(JSONObject source) throws JSONException, ServerException {
        try {
            return fromJson(source, targetClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION);
        }
    }

    @Override
    public <T extends D> T fromJson(JSONObject source, T target) throws JSONException, ServerException {
        doConvert(source, target);
        return target;
    }

    @Override
    public List<D> fromJson(JSONArray source) throws JSONException, ServerException {
        if (source == null) {
            return null;
        }
        List<D> result = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            JSONObject jsonObject = source.getJSONObject(i);
            D d = fromJson(jsonObject);
            result.add(d);
        }
        return result;
    }

    /**
     * Convert the not null JSON object <tt>source</tt> into a domain object.
     *
     * @param source
     * @return
     * @throws JSONException
     * @throws ServerException
     */
    protected abstract <T extends D> void doConvert(JSONObject source, T target) throws JSONException, ServerException;

}
