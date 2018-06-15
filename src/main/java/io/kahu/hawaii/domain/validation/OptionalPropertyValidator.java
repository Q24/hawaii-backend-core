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
package io.kahu.hawaii.domain.validation;

import io.kahu.hawaii.domain.ValidatableDomainProperty;
import io.kahu.hawaii.domain.ValueHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang.StringUtils;

public class OptionalPropertyValidator implements ConstraintValidator<Optional, ValueHolder> {

    private String key;

    @Override
    public void initialize(Optional constraintAnnotation) {
        // Do nothing
        this.key = constraintAnnotation.key();
    }

    @Override
    public boolean isValid(ValueHolder value, ConstraintValidatorContext context) {
        if (value != null && StringUtils.isNotBlank(key) && !value.isEmpty()) {
            if (value instanceof ValidatableDomainProperty) {
                if (!((ValidatableDomainProperty) value).validate()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("INVALID").addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

}
