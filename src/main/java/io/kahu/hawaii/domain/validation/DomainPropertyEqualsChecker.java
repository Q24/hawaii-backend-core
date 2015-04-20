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
package io.kahu.hawaii.domain.validation;

import io.kahu.hawaii.domain.AbstractDomainProperty;

public class DomainPropertyEqualsChecker {
    public boolean areEqual(AbstractDomainProperty property, Object other) {
        if (other == null) {
            return false;
        }
        boolean areEqual = false;
        String parsedValue = property.getParsedValue();
        if (other instanceof AbstractDomainProperty) {
            String otherParsedValue = ((AbstractDomainProperty) other).getParsedValue();
            if (parsedValue == null && otherParsedValue == null) {
                return true;
            } else if (parsedValue != null && otherParsedValue != null) {
                areEqual = parsedValue.equals(otherParsedValue);
            }
        } else {
            areEqual = parsedValue.equals(other);
        }
        return areEqual;
    }
}
