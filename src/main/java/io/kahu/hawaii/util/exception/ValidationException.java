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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

public class ValidationException extends HawaiiException implements ExceptionKeyConstants {

    private static final long serialVersionUID = 8024960096864705454L;

    public ValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable throwable) {
        super(throwable);
    }

    public ValidationException() {
        super();
    }

    public ValidationException(HawaiiRequestValidationError error) {
        this(error, null);
    }

    public ValidationException(HawaiiRequestValidationError error, String message) {
        super(message);
        Assert.notNull(error);
        addRequestValidationError(error);
    }

    public ValidationException(ItemValidation error) {
        super();
        Assert.notNull(error);
        addItemValidation(error);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST; // 400
    }

    @Override
    public CoreLoggers getLoggerName() {
        return CoreLoggers.CLIENT;
    }

    private List<HawaiiRequestValidationError> requestValidationErrors;

    public void setRequestValidationErrors(List<HawaiiRequestValidationError> errors) {
        requestValidationErrors = errors;
    }

    public void addRequestValidationError(HawaiiRequestValidationError error) {
        Assert.notNull(error);

        if (requestValidationErrors == null) {
            requestValidationErrors = new ArrayList<>();
        }
        requestValidationErrors.add(error);
    }

    private List<ItemValidation> itemValidations;

    public void setItemValidations(List<ItemValidation> errors) {
        itemValidations = errors;
    }

    public void addItemValidation(ItemValidation error) {
        Assert.notNull(error);
        if (itemValidations == null) {
            itemValidations = new ArrayList<>();
        }
        itemValidations.add(error);
    }

    public void addItemValidations(List<ItemValidation> errors) {
        Assert.notNull(errors);
        if (itemValidations == null) {
            itemValidations = new ArrayList<>();
        }
        itemValidations.addAll(errors);
    }

    @Override
    public JSONObject toJson() throws HawaiiException {
        try {
            JSONObject result = new JSONObject();
            JSONArray request_errors = new JSONArray();
            JSONArray item_errors = new JSONArray();
            result.put(REQUEST_VALIDATION_ERRORS, request_errors);
            result.put(ITEM_VALIDATION_ERRORS, item_errors);

            if (requestValidationErrors != null && requestValidationErrors.size() > 0) {
                for (HawaiiRequestValidationError error : requestValidationErrors) {
                    request_errors.put(error.getName());
                }
            }

            if (itemValidations != null && itemValidations.size() > 0) {
                for (ItemValidation item : itemValidations) {
                    JSONObject error = new JSONObject();
                    error.put(ITEM_KEY, item.getKey());
                    error.put(ITEM_ERROR_CODE, item.getError().toString());
                    item_errors.put(error);
                }
            }
            return result;
        } catch (JSONException e) {
            throw new ServerException(ServerError.JSON, e);
        }
    }

    @Override
    public String getMessage() {
        try {
            String message = super.getMessage();
            return (message != null) ? message + " - " + toJson().toString() : toJson().toString();
        } catch (HawaiiException ignoreThis) {
            return "Unexpected Error, call Math";
        }
    }
}
