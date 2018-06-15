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
package io.kahu.hawaii.util.json;

import io.kahu.hawaii.util.exception.ServerException;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface JsonConverter<D> {

    D fromJson(JSONObject source) throws JSONException, ServerException;

    D fromJson(JSONObject parent, String key) throws JSONException, ServerException;

    <T extends D> T fromJson(JSONObject parent, String key, T target) throws JSONException, ServerException;

    List<D> fromJson(JSONArray source) throws JSONException, ServerException;

    <T extends D> T fromJson(JSONObject source, T target) throws JSONException, ServerException;

}
