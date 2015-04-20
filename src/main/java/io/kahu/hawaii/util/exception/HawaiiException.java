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
package io.kahu.hawaii.util.exception;

import io.kahu.hawaii.util.logger.CoreLoggers;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;

public abstract class HawaiiException extends Exception {

	private static final long serialVersionUID = 6130927161530126904L;
	
	public HawaiiException(String message, Throwable throwable) {
		super(message, throwable);
	}
	public HawaiiException(String message) {
		super(message);
	}
	public HawaiiException(Throwable throwable) {
		super(throwable);
	}
	public HawaiiException() {
		super();
	}
	
	public abstract HttpStatus getStatus();
	
	public abstract CoreLoggers getLoggerName();
	
	public JSONObject toJson() throws HawaiiException{
		return new JSONObject();
	}
}
