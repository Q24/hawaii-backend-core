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

import io.kahu.hawaii.rest.Validatable;

import java.util.Set;

import javax.validation.ConstraintViolation;

public interface ValidatableDomainObject<T extends DomainObject> extends Validatable, ValueHolder {
    /**
     * Validate the domain object.
     */
    void validate();

    /**
     * Store the constraint violations.
     *
     * @param violations
     */
    void setViolations(Set<ConstraintViolation<T>> violations);

    /**
     * Retrieve the constraint violations.
     *
     * @return
     */
    Set<ConstraintViolation<T>> getViolations();

    /**
     * Check whether a previously {@link #validate()} resulted in no errors.
     */
    boolean isValid();

}
