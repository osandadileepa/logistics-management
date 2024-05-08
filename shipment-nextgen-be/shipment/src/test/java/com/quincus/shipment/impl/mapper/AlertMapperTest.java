package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.ConstraintType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;

class AlertMapperTest {

    @Spy
    private AlertMapper mapper = Mappers.getMapper(AlertMapper.class);

    @Test
    @DisplayName("given alert when mapDomainToEntity then return expected alertEntity")
    void returnExpectedWhenMapDomainToEntity() {
        Alert domain = new Alert();
        domain.setId("0001");
        domain.setShortMessage("This is a warning message");
        domain.setMessage("This is a full warning message [XXX]");
        domain.setType(AlertType.WARNING);
        domain.setConstraint(ConstraintType.SOFT_CONSTRAINT);
        domain.setDismissed(true);
        domain.setDismissedBy("0002");

        AlertEntity entity = mapper.toEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("fields", "shipmentJourney", "modifyTime", "createTime", "version", "packageJourneySegment")
                .isEqualTo(domain);
    }

    @Test
    @DisplayName("given alertEntity when mapEntityToDomain then return expected alert")
    void returnExpectedWhenMapEntityToDomain() {
        AlertEntity entity = new AlertEntity();
        entity.setId("0001");
        entity.setShortMessage("This is a warning message");
        entity.setMessage("This is a full warning message [XXX]");
        entity.setType(AlertType.WARNING);
        entity.setConstraint(ConstraintType.SOFT_CONSTRAINT);
        entity.setDismissed(true);
        entity.setDismissedBy("0002");

        Alert domain = mapper.toDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("packageJourneySegmentId", "shipmentJourneyId")
                .isEqualTo(entity);

    }

}
