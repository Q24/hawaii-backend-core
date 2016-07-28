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

import org.springframework.util.Assert;

public class StringProperty extends AbstractDomainProperty {
    private static final long serialVersionUID = 1L;

    private Integer minLength;
    private Integer maxLength;

    public StringProperty(String value) {
        this(value, null, null);
    }

    public StringProperty(String value, Integer maxLength) {
        this(value, null, maxLength);
    }

    public StringProperty(String value, Integer minLength, Integer maxLength) {
        super(value);

        if (minLength != null) {
            this.minLength = minLength.intValue();
        }
        if (maxLength != null) {
            this.maxLength = maxLength.intValue();
            Assert.isTrue(maxLength > 0);
        }
        if (minLength != null && maxLength != null) {
            Assert.isTrue(minLength <= maxLength);
        }
    }

    protected int getLength() {
        return getParsedValue().length();
    }

    @Override
    public boolean validate(String parsedValue) {
        if (isEmpty()) {
            return true;
        }
        if (minLength != null && minLength > getLength()) {
            return false;
        }
        if (maxLength != null && getLength() > maxLength) {
            return false;
        }

        return isValidText(parsedValue);
    }

    /**
     * Perform extra validations on the string value. The value is not empty /
     * blank.
     *
     * @param parsedValue
     *            TODO
     */
    protected boolean isValidText(String parsedValue) {
        /*
         * String values have no validations. Sub types must override this
         * method.
         */
        return true;
    }
}
