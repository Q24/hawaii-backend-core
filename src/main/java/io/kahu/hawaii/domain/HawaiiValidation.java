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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * Marker annotation used is validation of domain objects.
 *
 * Annotations must be declared with a key indicating the attribute to which the
 * error will be assigned.
 *
 * Failing validation will be reported as an INVALID error. If so desired, the
 * reported error may be overridden by setting a message. The message will be
 * used to lookup an ItemValidationError, e.g. REQUIRED.
 *
 * @author paul
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ANNOTATION_TYPE })
public @interface HawaiiValidation {
}
