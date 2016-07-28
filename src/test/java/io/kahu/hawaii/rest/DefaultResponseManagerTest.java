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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.kahu.hawaii.util.exception.AuthenticationException;
import io.kahu.hawaii.util.exception.AuthorisationException;
import io.kahu.hawaii.util.exception.ExceptionKeyConstants;
import io.kahu.hawaii.util.exception.HawaiiException;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.exception.TestServerError;
import io.kahu.hawaii.util.exception.ValidationException;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DefaultResponseManagerTest implements ExceptionKeyConstants {

    /**
     * TODO create a test that supplies a MOCK exception and have that MOCK
     * generate an Exception on the activation of toJson();
     *
     * @throws Exception
     */

    private LogManager logManager;
    private DefaultResponseManager responseManager;

    @Before
    public void setUp() {
        logManager = mock(LogManager.class);
        responseManager = new DefaultResponseManager(logManager, null);

    }

    @Test(expected = AssertionError.class)
    public void assureThatToResponseWithNullValueForJSONSerializableFails() throws Exception {
        JSONSerializable object = null;
        responseManager.toResponse(object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assureThatToResponseWithNullValueForListFails() throws Exception {
        List<JSONSerializable> objects = null;
        responseManager.toResponse(objects);
    }

    @Test
    public void assureThatToResponseWithNullExceptionIsLogged() throws Exception {
        ServerException exception = null;
        ResponseEntity<?> response = responseManager.toResponse(exception);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
        verify(logManager).logIncomingCallEnd(Mockito.argThat(new TestInstanceOfMatcher<>(HawaiiException.class)));
    }

    @Test
    public void testToResponseWithRuntimeException() throws Exception {
        ResponseEntity<?> response = responseManager.toResponse(new Throwable());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));

        response = responseManager.toResponse(new AssertionError());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));

        response = responseManager.toResponse(new NullPointerException());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));

        verify(logManager, times(3)).logIncomingCallEnd(Mockito.argThat(new TestInstanceOfMatcher<>(HawaiiException.class)));
    }

    @Test
    public void assureThatForbiddenStatusIsSetForAuthenticationException() throws Exception {
        ResponseEntity<?> response = responseManager.toResponse(new AuthenticationException());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
        verify(logManager).logIncomingCallEnd(Mockito.argThat(new TestInstanceOfMatcher<>(AuthenticationException.class)));
    }

    @Test
    public void assureThatUnauthorizedStatusIsSetForAuthorisationException() throws Exception {
        ResponseEntity<?> response = responseManager.toResponse(new AuthorisationException("Some Message"));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
        verify(logManager).logIncomingCallEnd(200, "Some Message");
    }

    @Test
    public void assureThatBadRequestStatusIsSetForValidationException() throws Exception {
        ResponseEntity<?> response = responseManager.toResponse(new ValidationException());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
        verify(logManager).logIncomingCallEnd(eq(HttpStatus.BAD_REQUEST.value()), any(String.class));
    }

    @Test
    public void testThatEmptyResponseIsOk() throws Exception {
        ResponseEntity<?> response = responseManager.toResponse();
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    public void assurteThatResponseContainsJSONArrayWithJsonObject() throws Exception {

        ResponseEntity<?> response = responseManager.toResponse(new ObjectExample("piet", "puk"));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));

        JSONObject json = new JSONObject(response.getBody().toString());

        JSONArray content = json.getJSONArray("data");
        assertThat(content.length(), is(equalTo(1)));

        assertThat(content.getJSONObject(0).getString("firstname"), is(equalTo("piet")));
        assertThat(content.getJSONObject(0).getString("lastname"), is(equalTo("puk")));

    }

    @Test
    public void testToResponseObjectList() throws Exception {
        List<JSONSerializable> objects = new ArrayList<JSONSerializable>();
        objects.add(new ObjectExample("piet", "puk"));
        objects.add(new ObjectExample("jan", "klaassen"));
        ResponseEntity<?> response = responseManager.toResponse(objects);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));

        JSONObject json = new JSONObject(response.getBody().toString());

        JSONArray content = json.getJSONArray("data");

        assertThat(content.length(), is(equalTo(2)));
        assertThat(content.getJSONObject(0).getString("firstname"), is(equalTo("piet")));
        assertThat(content.getJSONObject(1).getString("firstname"), is(equalTo("jan")));
    }

    private class ObjectExample implements JSONSerializable {

        private final String firstname;
        private final String lastname;

        private ObjectExample(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }

        @Override
        public JSONObject toJson() throws ServerException {
            try {
                JSONObject object = new JSONObject();
                object.put("firstname", firstname);
                object.put("lastname", lastname);
                return object;
            } catch (JSONException e) {
                throw new ServerException(TestServerError.S1);
            }
        }
    }

    @Test
    public void testHeadersDoesNotContainXHawaiiTxIdWhenDisabled() throws ServerException {
        responseManager = new DefaultResponseManager(logManager, null); // 2nd argument null to disable X-Hawaii-Tx-Id
        ResponseEntity<?> response = responseManager.toResponse();
        HttpHeaders headers = response.getHeaders();
        assertThat(headers, not(hasKey(DefaultResponseManager.X_HAWAII_TRANSACTION_ID_HEADER)));
    }

    @Test
    public void testHeadersContainsXHawaiiTxIdWhenEnabledAndTxIdInLoggingContent() throws ServerException {
        responseManager = new DefaultResponseManager(logManager, "tx.id"); // 2nd argument null to enable X-Hawaii-Tx-Id
        LoggingContext.get().put("tx.id", "12345"); // store tx.id in logging content
        ResponseEntity<?> response = responseManager.toResponse();
        HttpHeaders headers = response.getHeaders();
        assertThat(headers, hasKey(DefaultResponseManager.X_HAWAII_TRANSACTION_ID_HEADER));
        assertThat(headers.getFirst(DefaultResponseManager.X_HAWAII_TRANSACTION_ID_HEADER).toString(), is("12345"));
    }

    @Test
    public void testHeadersDoesNotContainXHawaiiTxIdWhenEnabledButTxIdNotInLoggingContent() throws ServerException {
        responseManager = new DefaultResponseManager(logManager, "tx.id"); // 2nd argument null to enable X-Hawaii-Tx-Id
        LoggingContext.get().remove("tx.id"); // make sure tx.id not in logging content
        ResponseEntity<?> response = responseManager.toResponse();
        HttpHeaders headers = response.getHeaders();
        assertThat(headers, not(hasKey(DefaultResponseManager.X_HAWAII_TRANSACTION_ID_HEADER)));
    }
}
