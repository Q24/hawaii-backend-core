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

import io.kahu.hawaii.util.exception.HawaiiRequestValidationError;
import io.kahu.hawaii.util.exception.ItemValidation;

import java.util.List;

public interface Validatable {
    public void validate(List<HawaiiRequestValidationError> request_validations, List<ItemValidation> item_validations);
}
