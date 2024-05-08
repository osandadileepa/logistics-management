package com.quincus.shipment.impl.validator;


import com.quincus.shipment.api.domain.ProofOfCost;
import com.quincus.shipment.api.exception.ProofOfCostException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProofOfCostValidatorTest {

    private final ProofOfCostValidator validator = new ProofOfCostValidator();

    @Test
    void shouldNotThrowExceptionForValidFile() {
        ProofOfCost proofOfCost = new ProofOfCost();
        proofOfCost.setFileName("test.png");
        proofOfCost.setFileSize(1000L);

        validator.validate(List.of(proofOfCost));
    }

    @Test
    void shouldNotThrowExceptionForNullProofOfCost() {
        assertThatNoException().isThrownBy(() -> validator.validate(List.of()));
    }

    @Test
    void shouldThrowExceptionForInvalidFile() {
        ProofOfCost proofOfCost = new ProofOfCost();
        proofOfCost.setFileName("test.text");
        proofOfCost.setFileSize(1000L);

        assertThatThrownBy(() -> validator.validate(List.of(proofOfCost)))
                .isInstanceOf(ProofOfCostException.class);
    }

}
