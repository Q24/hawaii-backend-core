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
package io.kahu.hawaii.rest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public abstract class JSONHelper {
    public static JSONObject getJSONObjectByNameFromArrayForKey(String name, JSONArray array, String key) throws Exception {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getString(key).equals(name)) {
                return obj;
            }
        }
        return null;
    }
    
    public static String getArrayValue(String value, JSONArray values) throws Exception {
        for (int i = 0; i < values.length(); i++) {
            if (values.getString(i).equals(value)) {
                return value;
            }
        }
        return null;
    }
}
