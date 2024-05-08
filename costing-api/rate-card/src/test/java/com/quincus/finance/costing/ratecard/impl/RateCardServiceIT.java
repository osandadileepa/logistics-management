package com.quincus.finance.costing.ratecard.impl;

import com.quincus.finance.costing.common.exception.CostingApiException;
import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.api.model.RateCardCalculationType;
import com.quincus.finance.costing.ratecard.impl.config.ITConfiguration;
import com.quincus.finance.costing.ratecard.impl.data.RateCalculationTestData;
import com.quincus.finance.costing.ratecard.impl.service.RateCardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.quincus.finance.costing.ratecard.impl.service.RateCardService.ERR_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ITConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RateCardServiceIT {

    @Autowired
    private RateCardService rateCardService;


    @Test
    @DisplayName("GIVEN rateCard request WHEN create THEN save and return expected")
    void returnAndUCreateRateCard() {
        RateCard rateCard = RateCalculationTestData.dummyRateCard();

        RateCard result = rateCardService.create(rateCard);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(rateCard);
    }

    @Test
    @DisplayName("GIVEN update request WHEN update THEN update and return expected")
    void returnAndUpdateRateCard() {
        RateCard rateCard = RateCalculationTestData.dummyRateCard();
        RateCard createdRateCard = rateCardService.create(rateCard);
        createdRateCard.setCalculationType(RateCardCalculationType.PER_DISTANCE_UNIT);

        RateCard updatedRateCard = rateCardService.update(createdRateCard);

        assertThat(updatedRateCard)
                .usingRecursiveComparison()
                .ignoringFields("createTime", "modifyTime", "id")
                .isEqualTo(createdRateCard);
    }

    @Test
    @DisplayName("GIVEN non-existing update request WHEN update THEN throw error")
    void throwErrorWhenNonExistingRateCard() {
        RateCard rateCard = RateCalculationTestData.dummyRateCard();
        rateCard.setId(UUID.randomUUID().toString());

        assertThatThrownBy(() -> rateCardService.update(rateCard))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(String.format(ERR_NOT_FOUND, rateCard.getId()));
                });
    }

    @Test
    @DisplayName("GIVEN rate card id request WHEN find THEN return expected")
    void returnRateCardWhenExistingRateCardId() {
        RateCard rateCard = RateCalculationTestData.dummyRateCard();

        RateCard createdRateCard = rateCardService.create(rateCard);

        RateCard result = rateCardService.get(createdRateCard.getId());

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(createdRateCard);

    }

    @Test
    @DisplayName("GIVEN non-existing rate card id WHEN find THEN throw error")
    void throwErrorWhenNonExistingRateCardId() {
        RateCard rateCard = RateCalculationTestData.dummyRateCard();

        rateCard.setId(UUID.randomUUID().toString());

        assertThatThrownBy(() -> rateCardService.get(rateCard.getId()))
                .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(String.format(ERR_NOT_FOUND, rateCard.getId()));
                });
    }

    @Test
    @DisplayName("GIVEN non existing rate card id WHEN delete THEN do nothing")
    void doesNotThrowExceptionDeleteWhenGivenRateCardId() {
        assertDoesNotThrow(() -> rateCardService.delete(UUID.randomUUID().toString()));
    }
}
