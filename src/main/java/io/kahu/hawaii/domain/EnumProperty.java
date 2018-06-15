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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class EnumProperty extends AbstractDomainProperty {
    private static final long serialVersionUID = 1L;
    private final List<String> allowedValues;

    public EnumProperty(String value, Object[] possibleValues) {
        super(StringUtils.upperCase(value));
        allowedValues = new ArrayList<String>();
        for (Object possibleValue : possibleValues) {
            allowedValues.add(StringUtils.upperCase(possibleValue.toString()));
        }
    }

    public EnumProperty(String value, List<?> possibleValues) {
        super(StringUtils.upperCase(value));
        allowedValues = new ArrayList<String>();
        for (Object possibleValue : possibleValues) {
            allowedValues.add(StringUtils.upperCase(possibleValue.toString()));
        }
    }

    @Override
    public boolean validate(String parsedValue) {
        return allowedValues.contains(StringUtils.upperCase(parsedValue));
    }
}
