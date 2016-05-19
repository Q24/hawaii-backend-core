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

import io.kahu.hawaii.domain.validation.DomainPropertyEqualsChecker;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * AbstractDomainProperty is a "wrapper" around string values. The original
 * value is retained. Subclasses can override the {@linkplain #getParsedValue()}
 * method to leniently parse the value into another string.
 *
 * The parsed value is used in validating the property.
 */
public abstract class AbstractDomainProperty implements Serializable, ValidatableDomainProperty {
    private static final long serialVersionUID = 1L;
    private String value;
    private boolean hasCachedParsedValue = false;
    private String cachedParsedValue;

    public AbstractDomainProperty(String value) {
        setValue(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public final void setValue(String value) {
        this.value = value;
        hasCachedParsedValue = false;
        cachedParsedValue = null;
    }

    public boolean isValueEmpty() {
        return StringUtils.isEmpty(getValue());
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(getParsedValue());
    }

    @Override
    public boolean validate() {
        if (isEmpty()) {
            return true;
        }
        return validate(getParsedValue());
    }

    @Override
    public final String getParsedValue() {
        if (isValueEmpty()) {
            return getValue();
        }
        if (hasCachedParsedValue()) {
            return getCachedParsedValue();
        }

        setParsedValue(parseValue(StringUtils.trim(getValue())));
        return getCachedParsedValue();
    }

    protected String parseValue(String value) {
        return value;
    }

    protected boolean hasCachedParsedValue() {
        return hasCachedParsedValue;
    }

    protected String getCachedParsedValue() {
        return cachedParsedValue;
    }

    protected void setParsedValue(String parsedValue) {
        cachedParsedValue = parsedValue;
        this.hasCachedParsedValue = true;
    }

    @Override
    public int hashCode() {
        if (StringUtils.isNotEmpty(getParsedValue())) {
            return getParsedValue().hashCode();
        } else {
            return super.hashCode();
        }
    }

    @Override
    public boolean equals(Object other) {
        return new DomainPropertyEqualsChecker().areEqual(this, other);
    }
}