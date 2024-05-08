package com.quincus.shipment.impl.validator;

import com.quincus.order.api.domain.Root;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class RootOrderValidator {

    private static final String VALIDATION_ERROR_MESSAGE = "%s : %s";
    private final ValidatorFactory validatorFactory;

    public void validate(Root rootOrder) {
        Set<ConstraintViolation<Root>> constraints = validatorFactory.getValidator().validate(rootOrder);
        if (!constraints.isEmpty()) {
            List<String> constraintErrorMessages = constraints.stream().map(v ->
                    String.format(VALIDATION_ERROR_MESSAGE, v.getPropertyPath().toString(), v.getMessage())
            ).toList();
            throw new QuincusValidationException(String.valueOf(constraintErrorMessages));
        }
    }
}
