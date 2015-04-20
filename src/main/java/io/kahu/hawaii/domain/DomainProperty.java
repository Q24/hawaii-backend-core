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
package io.kahu.hawaii.domain;

import io.kahu.hawaii.domain.ValueHolder;

public interface DomainProperty extends ValueHolder {
    /**
     * Return the parsed value from this domain property. The parsing should be
     * done forgiving, in a lenient manner. If the value cannot be parsed the
     * parsed value should be null.
     *
     * @return String
     */
    String getParsedValue();
}
