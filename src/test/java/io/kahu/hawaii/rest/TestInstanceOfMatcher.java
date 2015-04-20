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

import java.io.Serializable;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public class TestInstanceOfMatcher<T> extends ArgumentMatcher<T> implements Serializable {
    private final Class<T> clazz;

    public TestInstanceOfMatcher(Class<T> clazz) {
        this.clazz = clazz;
    }

    public boolean matches(Object actual) {
        return (actual != null) && clazz.isAssignableFrom(actual.getClass());
    }

    public void describeTo(Description description) {
        description.appendText("isA(" + clazz.getName() + ")");
    }
}
