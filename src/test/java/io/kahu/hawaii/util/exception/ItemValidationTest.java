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

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


public class ItemValidationTest {

    @Test
    public void assureTwoItemValidationsWithSameNameAndValueAreEqual() throws Exception {
        String key = "key";
        ItemValidationError error = ItemValidationError.INVALID;
        ItemValidation itemValidation1 = new ItemValidation(key, error);
        ItemValidation itemValidation2 = new ItemValidation(key, error);
        assertThat(itemValidation1, is(equalTo(itemValidation2)));
    }

    @Test
    public void assureTwoItemValidationsWithDifferentNameAndSameValueAreNotEqual() throws Exception {
        String key = "key";
        ItemValidationError error = ItemValidationError.INVALID;
        ItemValidation itemValidation1 = new ItemValidation(key, error);
        ItemValidation itemValidation2 = new ItemValidation("otherkey", error);
        assertThat(itemValidation1, is(not(equalTo(itemValidation2))));
    }

    @Test
    public void assureTwoItemValidationsWithDifferentErrorAndSameKeyAreNotEqual() throws Exception {
        String key = "key";
        ItemValidationError error = ItemValidationError.INVALID;
        ItemValidation itemValidation1 = new ItemValidation(key, error);
        ItemValidation itemValidation2 = new ItemValidation(key, ItemValidationError.BIRTH_DATE_IN_THE_FUTURE);
        assertThat(itemValidation1, is(not(equalTo(itemValidation2))));
    }
}
