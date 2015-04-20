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
package io.kahu.hawaii.rest;

import io.kahu.hawaii.util.exception.ServerException;

import org.codehaus.jettison.json.JSONObject;

public interface JSONSerializable {
	/**
	 * Json-ification will need to handle JSONExceptions.
	 * These can be messaged through an HawaiiException
	 * with the appropriate error-code
	 */
	public JSONObject toJson() throws ServerException;
}
