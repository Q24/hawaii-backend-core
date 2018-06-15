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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumberProperty extends AbstractDomainProperty implements Serializable, Comparable<NumberProperty> {
    private static final long serialVersionUID = 1L;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static int NUMBER_OF_DECIMALS = 2;
    private BigDecimal numberValue;
    private boolean validNumber = false;

    public NumberProperty() {
        this(ZERO);
    }

    public NumberProperty(BigDecimal bigDecimal) {
        this(format(bigDecimal));
    }

    public NumberProperty(Double val) {
        this(format(val));
    }

    public NumberProperty(String number) {
        super(number);
        try {
            if (number == null) {
                this.numberValue = null;
                this.validNumber = true;
            } else {
                Locale locale = Locale.GERMAN;
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                Number numeric = numberFormat.parse(number);
                this.numberValue = createBigDecimal(numeric.toString());
                this.validNumber = true;
            }
        } catch (ParseException e) {
            this.validNumber = false;
        }
    }

    private static String format(BigDecimal number) {
        if (number == null) {
            return null;
        }
        return format(number.doubleValue());
    }

    private static String format(Double number) {
        if (number == null || number.isNaN()) {
            return null;
        }
        Locale locale = Locale.GERMAN;
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        return numberFormat.format(number);
    }

    private static BigDecimal createBigDecimal(String value) {
        return new BigDecimal(value).setScale(NUMBER_OF_DECIMALS, RoundingMode.HALF_UP);
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(BigDecimal numberValue) {
        this.numberValue = numberValue;
    }

    public NumberProperty add(NumberProperty number) {
        return new NumberProperty(numberValue.add(number.getNumberValue()));
    }

    @Override
    public String toString() {
        if (numberValue != null) {
            return numberValue.toString();
        } else {
            return "";
        }
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean equality = false;
        if (o instanceof NumberProperty) {
            equality = this.getNumberValue().equals(((NumberProperty) o).getNumberValue());
        }
        return equality;
    }

    @Override
    public boolean isEmpty() {
        boolean isEmpty = super.isEmpty();
        if (!isEmpty && validNumber) {
            isEmpty = (0 == ZERO.compareTo(getNumberValue()));
        }
        return isEmpty;
    }

    public int toInteger() {
        return getNumberValue().intValue();
    }

    public double toDouble() {
        return getNumberValue().doubleValue();
    }

    @Override
    public boolean validate(String parsedValue) {
        return validNumber;
    }

    @Override
    public int compareTo(NumberProperty o) {
        return getNumberValue().compareTo(o.getNumberValue());
    }

    public boolean isZero() {
        return BigDecimal.ZERO.compareTo(getNumberValue()) == 0;
    }
}