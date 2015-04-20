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

import io.kahu.hawaii.domain.BooleanProperty;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class JsonHelper {

    public static <D> void append(JSONObject json, JsonResultConverter<D> converter, D domainObject) throws JSONException {
        converter.addToJson(json, domainObject);
    }

    public static <D> void add(JSONObject json, String key, JsonResultConverter<D> converter, D domainObject) throws JSONException {
        json.put(key, converter.toJson(domainObject));
    }

    public static void add(JSONObject json, String key, DateTime dateTime) throws JSONException {
        String value = null;
        if (dateTime != null) {
            value = "" + dateTime.toDate().getTime();
        }
        add(json, key, value, false);
    }

    public static void add(JSONObject json, String key, Object value) throws JSONException {
        add(json, key, value, false);
    }

    public static void add(JSONObject json, String key, LocalDate value) throws JSONException {
        add(json, key, value, "yyyy-MM-dd");
    }

    public static void add(JSONObject json, String key, LocalDate value, String format) throws JSONException {
        if (value == null) {
            json.put(key, "");
        } else {
            add(json, key, value.toString(format), false);
        }
    }

    public static void add(JSONObject json, String key, BooleanProperty value) throws JSONException {
        if (value != null && !value.isEmpty()) {
            json.put(key, value.toBoolean());
        } else {
            json.put(key, "empty");
        }
    }

    public static void add(JSONObject json, String key, Object value, boolean useParsedValue) throws JSONException {
        if (value == null) {
            json.put(key, "");
        } else if (value instanceof JSONObject || value instanceof JSONArray) {
            json.put(key, value);
        } else if (value instanceof Collection<?>) {
            Collection<?> values = (Collection<?>) value;
            JSONArray array = new JSONArray();
            for (Object v : values) {
                array.put(v);
            }
            json.put(key, array);
        } else if (value instanceof Number) {
            json.put(key, value);
        } else {
            json.put(key, StringUtils.defaultIfBlank(value.toString(), ""));
        }
    }

}
