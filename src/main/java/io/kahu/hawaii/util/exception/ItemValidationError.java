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

public enum ItemValidationError implements HawaiiItemValidationError {
    // formatter: off
    REQUIRED,
    INVALID,
    NOT_A_NUMBER,
    NOT_A_DATE_IN_MILLIS,
    BIRTH_DATE_IN_THE_FUTURE,
    VALUE_BELOW_MINIMUM,
    VALUE_ABOVE_MAXIMUM,
    NUMBER_NOT_PORTABLE;
    // formatter: on

    @Override
    public String getName() {
        return toString();
    }
}
