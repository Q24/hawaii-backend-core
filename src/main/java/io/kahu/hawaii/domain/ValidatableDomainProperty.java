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
package io.kahu.hawaii.domain;

import io.kahu.hawaii.domain.validation.ValidDomainProperty;

@ValidDomainProperty
public interface ValidatableDomainProperty extends DomainProperty {
    /**
     * Calls {@link #getParsedValue()} and uses this value to call
     * {@link #validate(String)}.
     *
     * This returns true for empty / null values.
     */
    boolean validate();

    /**
     * Validate the parsed property value. The method can assume that the
     * parameter parsedValue is not null.
     *
     * @param parsedValue
     *            not null.
     * @return true if the parsed value is properly validated.
     */
    boolean validate(String parsedValue);
}
