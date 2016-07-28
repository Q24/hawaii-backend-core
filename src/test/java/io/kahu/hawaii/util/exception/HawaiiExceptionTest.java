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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class HawaiiExceptionTest implements ExceptionKeyConstants {

    @Test
    public void testConstructor() throws Exception {
        try {
            ServerError error = null;
            new ServerException(error);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // correct
        }
    }

    @Test
    public void testMessage() throws Exception {
        ServerException e = new ServerException(TestServerError.S1);
        Assert.assertTrue("S1".equals(e.getMessage()));

        e = new ServerException(TestServerError.S1, new NullPointerException("npe"));
        Assert.assertTrue("S1".equals(e.getMessage()));

        e = new ServerException(TestServerError.S1, "piet");
        Assert.assertTrue("S1 - piet".equals(e.getMessage()));

        e = new ServerException(TestServerError.S1, "klaas", new NullPointerException("npe"));
        Assert.assertTrue("S1 - klaas".equals(e.getMessage()));

        e = new ServerException(TestServerError.S1, null, new NullPointerException("npe"));
        Assert.assertTrue("S1".equals(e.getMessage()));

        e = new ServerException(TestServerError.S1, "marie", null);
        Assert.assertTrue("S1 - marie".equals(e.getMessage()));
    }

    @Test
    public void testCause() throws Exception {
        NullPointerException npe = new NullPointerException("npe");
        ServerException e = new ServerException(TestServerError.S1, npe);
        Assert.assertTrue(npe == e.getCause());
    }

    @Test
    public void testGetLogger() throws Exception {
        ServerException e = new ServerException(TestServerError.S1);
        Assert.assertTrue(CoreLoggers.SERVER.equals(e.getLoggerName()));
    }

    @Test
    public void testGetStatus() throws Exception {

        ServerException e = new ServerException(TestServerError.S1);
        Assert.assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.equals(e.getStatus()));

        AuthenticationException auth = new AuthenticationException();
        Assert.assertTrue(HttpStatus.FORBIDDEN.equals(auth.getStatus()));

        AuthorisationException autho = new AuthorisationException();
        Assert.assertTrue(HttpStatus.UNAUTHORIZED.equals(autho.getStatus()));

        ValidationException v = new ValidationException();
        Assert.assertTrue(HttpStatus.BAD_REQUEST.equals(v.getStatus()));
    }

    @Test
    public void testAddIncorrectValidation() throws Exception {
        ValidationException e = new ValidationException();

        try {
            e.addItemValidation(null);
        } catch (IllegalArgumentException exception) {
            // expected
        }

        try {
            e.addRequestValidationError(null);
        } catch (IllegalArgumentException exception) {
            // expected
        }
    }

    @Test
    public void testToJson() throws Exception {

        ValidationException e = new ValidationException();

        JSONObject content = e.toJson();
        Assert.assertTrue(content.getJSONArray(REQUEST_VALIDATION_ERRORS).length() == 0);
        Assert.assertTrue(content.getJSONArray(ITEM_VALIDATION_ERRORS).length() == 0);

        e = new ValidationException();
        e.addRequestValidationError(TestRequestValidationError.VAL1);
        e.addItemValidation(new ItemValidation("smart", TestItemValidationError.VAL2));

        content = e.toJson();
        JSONArray request_errors = content.getJSONArray(REQUEST_VALIDATION_ERRORS);
        Assert.assertTrue(request_errors.length() == 1);
        Assert.assertTrue(request_errors.getString(0).equals(TestRequestValidationError.VAL1.toString()));
        JSONArray item_errors = content.getJSONArray(ITEM_VALIDATION_ERRORS);
        Assert.assertTrue(item_errors.length() == 1);
        Assert.assertTrue(item_errors.getJSONObject(0).getString(ITEM_KEY).equals("smart"));
        Assert.assertTrue(item_errors.getJSONObject(0).getString(ITEM_ERROR_CODE).equals(TestItemValidationError.VAL2.toString()));
    }

}
