package io.kahu.hawaii.domain.validation;

import io.kahu.hawaii.domain.DomainProperty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PropertyHasValueValidator implements ConstraintValidator<PropertyHasValue, DomainProperty> {
    private String requiredValue;

    @Override
    public void initialize(PropertyHasValue constraintAnnotation) {
        requiredValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(DomainProperty value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return requiredValue.equals(value.getParsedValue());
    }

}
