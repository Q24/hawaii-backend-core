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

import java.io.Serializable;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class IntegerProperty extends AbstractDomainProperty implements Serializable, Comparable<IntegerProperty> {
    private static final long serialVersionUID = 1L;

    private static final BigInteger ZERO = BigInteger.ZERO;

    private BigInteger numberValue;
    private boolean validNumber = false;

    public IntegerProperty() {
        this(ZERO);
    }

    public IntegerProperty(int integer) {
        this(format(integer));
    }

    public IntegerProperty(BigInteger bigInteger) {
        this(format(bigInteger));
    }

    public IntegerProperty(String number) {
        super(number);
        try {
            if (number == null) {
                this.numberValue = null;
                this.validNumber = true;
            } else {
                Locale locale = Locale.GERMAN;
                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                Number numeric = numberFormat.parse(number);
                this.numberValue = createBigInteger(numeric.toString());
                this.validNumber = true;
            }
        } catch (ParseException e) {
            this.validNumber = false;
        }
    }

    private static String format(BigInteger number) {
        if (number == null) {
            return null;
        }
        return format(number.intValue());
    }

    private static String format(Integer number) {
        if (number == null) {
            return null;
        }
        Locale locale = Locale.GERMAN;
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        return numberFormat.format(number);
    }

    private static BigInteger createBigInteger(String value) {
        return new BigInteger(value);
    }

    public BigInteger getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(BigInteger numberValue) {
        this.numberValue = numberValue;
    }

    public IntegerProperty add(IntegerProperty number) {
        return new IntegerProperty(numberValue.add(number.getNumberValue()));
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
        if (o instanceof IntegerProperty) {
            equality = this.getNumberValue().equals(((IntegerProperty) o).getNumberValue());
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
    public int compareTo(IntegerProperty o) {
        return getNumberValue().compareTo(o.getNumberValue());
    }
}