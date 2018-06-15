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

import io.kahu.hawaii.util.exception.HawaiiRequestValidationError;
import io.kahu.hawaii.util.exception.ItemValidation;
import io.kahu.hawaii.util.exception.RequestValidationError;
import io.kahu.hawaii.util.exception.ValidationException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.RequestLogBuilder;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

public class AbstractController {

    protected final LogManager logManager;
    private final String logTypePrefix;

    protected AbstractController(LogManager logManager, String logTypePrefix) {
        Assert.notNull(logManager);
        this.logManager = logManager;
        this.logTypePrefix = logTypePrefix;
    }

    protected void validate(Validatable validatable) throws ValidationException {
        List<ItemValidation> itemValidations = new ArrayList<>();
        List<HawaiiRequestValidationError> requestValidations = new ArrayList<>();

        validatable.validate(requestValidations, itemValidations);

        if (itemValidations.size() > 0 || requestValidations.size() > 0) {
            ValidationException exception = new ValidationException();
            exception.setItemValidations(itemValidations);
            exception.setRequestValidationErrors(requestValidations);
            throw exception;
        }
    }

    protected JSONObject toJson(String params) throws ValidationException {
        try {
            return new JSONObject((params != null) ? params : "");
        } catch (JSONException e) {
            throw new ValidationException(RequestValidationError.PROTOCOL_ERROR);
        }
    }

    protected RequestLogBuilder requestLog() {

        String method = new Throwable().getStackTrace()[1].getMethodName();
        String type = logTypePrefix + "." + method;
        RequestLogBuilder builder = new RequestLogBuilder(logManager, type);

        try {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = sra.getRequest();

            // path variables
            Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (pathVariables != null) {
                for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
                    builder.param(entry.getKey(), entry.getValue());
                }
            }

            // request parameters
            Enumeration<?> requestParameters = request.getParameterNames();
            while (requestParameters.hasMoreElements()) {
                String key = (String) requestParameters.nextElement();
                String[] values = request.getParameterValues(key);
                builder.param(key, values);
            }

        } catch (Throwable t) {
            logManager.warn(CoreLoggers.SERVER, "Unable to log request", t);
            // ignore
        }
        return builder.excludeParam("_");
    }
}
