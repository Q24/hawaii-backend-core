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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class BooleanProperty extends EnumProperty {
    private static final String EMPTY = "empty";

    private static final long serialVersionUID = 1L;

    private static final String[] POSSIBLE_VALUES = new String[] { "TRUE", "YES", "Y", "JA", "J", "FALSE", "NO", "N", "NEE", "1", "0", EMPTY };

    public BooleanProperty() {
        this("");
    }

    public BooleanProperty(Boolean value) {
        super(value ? "TRUE" : "FALSE", POSSIBLE_VALUES);
    }

    public BooleanProperty(String value) {
        super(StringUtils.defaultIfBlank(value, EMPTY), POSSIBLE_VALUES);
    }

    @Override
    public boolean isEmpty() {
        boolean empty = super.isEmpty();
        if (!empty) {
            empty = EMPTY.equalsIgnoreCase(getValue());
        }
        return empty;
    }

    @Override
    public String toString() {
        if (StringUtils.isBlank(getValue()) || EMPTY.equals(getValue())) {
            return EMPTY;
        }
        return "" + toBoolean();
    }

    public boolean toBoolean() {
        Set<String> trueValues = new HashSet<String>(Arrays.asList(new String[] { "TRUE", "YES", "Y", "JA", "J", "1" }));
        return trueValues.contains(StringUtils.upperCase(getValue()));
    }
}
