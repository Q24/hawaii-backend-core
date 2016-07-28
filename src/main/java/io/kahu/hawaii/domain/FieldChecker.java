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

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

public class FieldChecker {
    public boolean checkAllFieldsAreBlank(Object bean) throws ServerException {
        Assert.notNull(bean);
        return checkAllFieldsAreBlank(bean, getFieldNames(bean));
    }

    private boolean checkAllFieldsAreBlank(Object bean, Collection<String> fieldNames) throws ServerException {
        return checkAllFieldsAreBlank(bean, fieldNames.toArray(new String[] {}));
    }

    public boolean checkAllFieldsAreBlankExcept(Object bean, String... ignoredFieldNames) throws ServerException {
        Set<String> fieldNames = getFieldNames(bean);
        fieldNames.removeAll(Arrays.asList(ignoredFieldNames));
        return checkAllFieldsAreBlank(bean, fieldNames);
    }

    public boolean checkAllFieldsAreBlank(Object bean, String... fieldNames) throws ServerException {
        Assert.notNull(bean);
        boolean allBlank = true;
        for (String fieldName : fieldNames) {
            try {
                Object value = PropertyUtils.getSimpleProperty(bean, fieldName);
                boolean isBlank = isBlank(value);
                allBlank = allBlank && isBlank;
            } catch (Exception e) {
                throw new ServerException(ServerError.ILLEGAL_ARGUMENT, "Field '" + fieldName + "' cannot be obtained from object '" + bean + "'.", e);
            }

        }
        return allBlank;
    }

    private boolean isBlank(Object value) throws ServerException {
        boolean blank = (value == null);
        if (!blank && ValueHolder.class.isAssignableFrom(value.getClass())) {
            ValueHolder valueHolder = (ValueHolder) value;
            blank = valueHolder.isEmpty();
        }
        if (!blank && String.class.isAssignableFrom(value.getClass())) {
            blank = StringUtils.isEmpty((String) value);
        }
        if (!blank && Collection.class.isAssignableFrom(value.getClass())) {
            Collection<?> values = (Collection<?>) value;
            /**
             * <pre>
             * Als collection empty, dan blank = true;
             * Als collection niet empty, dan itereren (met initieel blank = true).
             * </pre>
             */
            blank = true;
            if (!values.isEmpty()) {
                for (Object v : values) {
                    blank = blank && isBlank(v);
                }
            }
        }

        return blank;
    }

    private Set<String> getFieldNames(Object bean) {
        Class<?> beanClass = bean.getClass();

        Set<String> fieldNames = new HashSet<String>();

        /*
         * Add all properties from this class.
         */
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(beanClass);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            fieldNames.add(propertyDescriptor.getName());
        }

        /*
         * Remove all properties from the interface 'ValueHolder'.
         */
        propertyDescriptors = PropertyUtils.getPropertyDescriptors(ValueHolder.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            fieldNames.remove(propertyDescriptor.getName());
        }
        /*
         * Remove all properties from the interface 'ValueHolder'.
         */
        propertyDescriptors = PropertyUtils.getPropertyDescriptors(ValidatableDomainObject.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            fieldNames.remove(propertyDescriptor.getName());
        }

        /*
         * Ignore class
         */
        fieldNames.remove("callbacks");
        fieldNames.remove("callback");
        fieldNames.remove("class");
        return fieldNames;
    }
}
