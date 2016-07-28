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

import io.kahu.hawaii.domain.validation.Mandatory;
import io.kahu.hawaii.domain.validation.Optional;
import io.kahu.hawaii.util.exception.HawaiiRequestValidationError;
import io.kahu.hawaii.util.exception.ItemValidation;
import io.kahu.hawaii.util.exception.ItemValidationError;
import io.kahu.hawaii.util.exception.RequestValidationError;
import io.kahu.hawaii.util.exception.ServerException;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang.StringUtils;

/**
 * The base class for all domain objects. It contains a basic implementation for
 * checking whether the object is empty.
 *
 * Next to this the {@linkplain ValidatableDomainObject} interface is
 * implemented. The validation itself is delegated to the Javax Validation
 * framework.
 */
public class AbstractValidatableDomainObject implements Serializable, DomainObject, ValidatableDomainObject<AbstractValidatableDomainObject> {
    private static final long serialVersionUID = 1L;
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private Set<ConstraintViolation<AbstractValidatableDomainObject>> violations = null;

    /**
     * This method delegates to
     * {@linkplain FieldChecker#checkAllFieldsAreBlank(Object)} method.
     */
    @Override
    public boolean isEmpty() {
        try {
            return new FieldChecker().checkAllFieldsAreBlank(this);
        } catch (ServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void setViolations(Set<ConstraintViolation<AbstractValidatableDomainObject>> violations) {
        this.violations = violations;
    }

    @Override
    public Set<ConstraintViolation<AbstractValidatableDomainObject>> getViolations() {
        return violations;
    }

    /**
     * Checks whether a previous validation returned no validations. It
     * basically checks <tt>getViolations().isEmpty()</tt>. See
     * {@link #validate()}.
     *
     * This check is only useful for top-level objects. For embedded objects
     * this method always returns true.
     */
    @Override
    public boolean isValid() {
        if (violations == null) {
            return true;
        }
        return violations.isEmpty();
    }

    /**
     * Calls the javax validation framework (jsr-303) to validate this object.
     * The results are stored via {@link #setViolations(Set)}. Before validating
     * this class the method {@link #isValid()} will return an error.
     */
    @Override
    public void validate() {
        setViolations(VALIDATOR_FACTORY.getValidator().validate(this));
    }

    @Override
    public void validate(List<HawaiiRequestValidationError> requestValidations, List<ItemValidation> itemValidations) {
        if (getViolations() == null) {
            validate();
        }
        Set<ConstraintViolation<AbstractValidatableDomainObject>> violations = getViolations();
        if (violations != null) {
            for (ConstraintViolation<AbstractValidatableDomainObject> constraintViolation : violations) {
                String propertyKey = null;

                Annotation ann = constraintViolation.getConstraintDescriptor().getAnnotation();
                if (ann instanceof Mandatory) {
                    propertyKey = ((Mandatory) ann).key();
                    if (StringUtils.isBlank(propertyKey)) {
                        throw new IllegalStateException(String.format("@Mandatory annotation without key on %s", constraintViolation.getPropertyPath()));
                    }
                    if (((Mandatory) ann).protocolError()) {
                        requestValidations.add(RequestValidationError.PROTOCOL_ERROR);
                    } else {
                        Object invalidObject = constraintViolation.getInvalidValue();
                        if (invalidObject == null || ((ValueHolder) invalidObject).isEmpty()) {
                            itemValidations.add(new ItemValidation(propertyKey, ItemValidationError.REQUIRED));
                        } else {
                            itemValidations.add(new ItemValidation(propertyKey, ItemValidationError.INVALID));
                        }
                    }
                } else if (ann instanceof Optional) {
                    propertyKey = ((Optional) ann).key();
                    if (StringUtils.isBlank(propertyKey)) {
                        throw new IllegalStateException(String.format("@Optional annotation without key on %s", constraintViolation.getPropertyPath()));
                    }
                    if (((Optional) ann).protocolError()) {
                        requestValidations.add(RequestValidationError.PROTOCOL_ERROR);
                    } else {
                        itemValidations.add(new ItemValidation(propertyKey, ItemValidationError.INVALID));
                    }
                } else if (ann.annotationType().isAnnotationPresent(HawaiiValidation.class)) {
                    try {
                        Method method;
                        Boolean protocolError = Boolean.FALSE;
                        try {
                            method = ann.getClass().getMethod("protocolError");
                            protocolError = (Boolean) method.invoke(ann);
                        } catch (NoSuchMethodException e) {
                            // ignore
                        }
                        if (protocolError) {
                            requestValidations.add(RequestValidationError.PROTOCOL_ERROR);
                        } else {

                            method = ann.getClass().getMethod("key");
                            propertyKey = (String) method.invoke(ann);
                            // Default error is INVALID
                            ItemValidationError error = ItemValidationError.INVALID;
                            String message = null;
                            try {
                                method = ann.getClass().getMethod("message");
                                message = (String) method.invoke(ann);
                            } catch (NoSuchMethodException e) {
                                // ignore
                            }
                            // Error override through annotation message
                            if (StringUtils.isNotBlank(message)) {
                                error = ItemValidationError.valueOf(message);
                            }
                            itemValidations.add(new ItemValidation(propertyKey, error));
                        }
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(String.format("Annotation %s without key on %s", ann.annotationType().getCanonicalName(),
                                constraintViolation.getPropertyPath()));
                    } catch (Exception e) {
                        throw new IllegalStateException("Exception validating Class validation", e);
                    }
                    continue;
                } else {
                    // ignored
                    continue;
                }
            }
        }
    }

}
