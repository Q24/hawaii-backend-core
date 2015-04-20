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

import org.springframework.http.HttpStatus;

public class FeatureNotEnabledException extends HawaiiException {

    private static final long serialVersionUID = 1L;

    public FeatureNotEnabledException() {
        super();
    }

    public FeatureNotEnabledException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FeatureNotEnabledException(String message) {
        super(message);
    }

    public FeatureNotEnabledException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public CoreLoggers getLoggerName() {
        return CoreLoggers.SERVER_EXCEPTION;
    }

}
