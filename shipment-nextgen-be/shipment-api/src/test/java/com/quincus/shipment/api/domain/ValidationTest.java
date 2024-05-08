package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public abstract class ValidationTest {
    static ValidatorFactory validatorFactory;
    protected static Validator validator;

    @BeforeAll
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    static Set<ConstraintViolation<Object>> validateModel(Object model) {
        return validator.validate(model);
    }

    @AfterAll
    public static void cleanUpValidator() {
        validatorFactory.close();
    }
}
