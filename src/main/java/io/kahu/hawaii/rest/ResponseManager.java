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

import io.kahu.hawaii.rest.JSONSerializable;
import io.kahu.hawaii.rest.ResponseKeyConstants;
import io.kahu.hawaii.util.exception.ServerException;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.ResponseEntity;

public interface ResponseManager extends ResponseKeyConstants {
    
    ResponseEntity<String> toResponse(Throwable t);

    ResponseEntity<String> toResponse(List<JSONSerializable> objects) throws ServerException;

    ResponseEntity<String> toResponse(JSONSerializable... objects) throws ServerException;

    ResponseEntity<String> toResponse(JSONObject... objects) throws ServerException;

    ResponseEntity<String> toResponse(JSONArray objects) throws ServerException;

    ResponseEntity<String> toResponse() throws ServerException;

}
