package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.quincus.shipment.api.constant.AlertMessage.FAILED_PICKUP_OR_DELIVERY;
import static com.quincus.shipment.api.constant.AlertMessage.RISK_OF_DELAY;
import static com.quincus.shipment.api.constant.AlertType.JOURNEY_REVIEW_REQUIRED;
import static com.quincus.shipment.api.constant.AlertType.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyMapperTest {
    @Test
    void mapDomainToEntity_shipmentJourneyDomain_shouldReturnShipmentJourneyEntity() {
        ShipmentJourney domain = new ShipmentJourney();
        domain.setJourneyId("JOURNEY1");
        domain.setShipmentId("SHIPMENT1");
        domain.setStatus(JourneyStatus.PLANNED);
        domain.setPackageJourneySegments(Collections.emptyList());

        Alert dummyAlert = new Alert("This is a warning message", WARNING);
        domain.setAlerts(List.of(dummyAlert));

        final ShipmentJourneyEntity entity = ShipmentJourneyMapper.mapDomainToEntity(domain);

        assertThat(entity.getAlerts().get(0).getShortMessage()).isEqualTo(dummyAlert.getShortMessage());
        assertThat(entity.getAlerts().get(0).getType()).isEqualTo(dummyAlert.getType());
        assertThat(entity.getId()).isEqualTo(domain.getJourneyId());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        assertThat(entity.getPackageJourneySegments()).isEmpty();
    }

    @Test
    void mapDomainToEntity_shipmentJourneyDomainNull_shouldReturnNull() {
        assertThat(ShipmentJourneyMapper.mapDomainToEntity(null)).isNull();
    }

    @Test
    void mapEntityToDomain_shipmentJourneyEntity_shouldReturnShipmentJourneyDomain() {
        ShipmentJourneyEntity entity = new ShipmentJourneyEntity();
        entity.setId("journey-1");
        entity.setStatus(JourneyStatus.PLANNED);
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setType(SegmentType.LAST_MILE);
        packageJourneySegmentEntity.setTransportType(TransportType.GROUND);
        entity.addPackageJourneySegment(packageJourneySegmentEntity);

        AlertEntity dummyAlertEntity = new AlertEntity();
        dummyAlertEntity.setShortMessage("This is an error message");
        dummyAlertEntity.setType(AlertType.ERROR);
        entity.setAlerts(List.of(dummyAlertEntity));

        final ShipmentJourney domain = ShipmentJourneyMapper.mapEntityToDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getAlerts()).isNotNull();
        assertThat(domain.getAlerts()).isNotEmpty();
        assertThat(domain.getAlerts().get(0).getShortMessage()).isEqualTo(dummyAlertEntity.getShortMessage());
        assertThat(domain.getAlerts().get(0).getType()).isEqualTo(dummyAlertEntity.getType());
        assertThat(domain.getJourneyId()).isEqualTo(entity.getId());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getPackageJourneySegments()).isNotEmpty();
    }

    @Test
    void mapEntityToDomain_noAlerts_shouldReturnShipmentJourneyDomain() {
        ShipmentJourneyEntity entity = new ShipmentJourneyEntity();
        entity.setId("journey-1");
        entity.setStatus(JourneyStatus.PLANNED);
        PackageJourneySegmentEntity packageJourneySegmentEntity = new PackageJourneySegmentEntity();
        packageJourneySegmentEntity.setType(SegmentType.LAST_MILE);
        packageJourneySegmentEntity.setTransportType(TransportType.GROUND);
        entity.addPackageJourneySegment(packageJourneySegmentEntity);

        final ShipmentJourney domain = ShipmentJourneyMapper.mapEntityToDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getAlerts()).isNotNull();
        assertThat(domain.getAlerts()).isEmpty();
        assertThat(domain.getJourneyId()).isEqualTo(entity.getId());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getPackageJourneySegments()).isNotEmpty();
    }

    @Test
    void mapEntityToDomain_shipmentJourneyEntityNull_shouldReturnNull() {
        assertThat(ShipmentJourneyMapper.mapEntityToDomain(null)).isNull();
    }

    @Test
    void testSetJourneyEntityAlerts() {
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        ShipmentJourney shipmentJourneyDomain = new ShipmentJourney();

        List<Alert> alerts = new ArrayList<>();
        alerts.add(createAlert("1", "shipmentJourneyId1", null, FAILED_PICKUP_OR_DELIVERY, JOURNEY_REVIEW_REQUIRED));
        alerts.add(createAlert("2", "shipmentJourneyId1", null, FAILED_PICKUP_OR_DELIVERY, JOURNEY_REVIEW_REQUIRED));
        alerts.add(createAlert("3", "", "packageJourneySegmentId1", FAILED_PICKUP_OR_DELIVERY, JOURNEY_REVIEW_REQUIRED));
        alerts.add(createAlert("4", "", "packageJourneySegmentId1", FAILED_PICKUP_OR_DELIVERY, JOURNEY_REVIEW_REQUIRED));
        alerts.add(createAlert("5", "shipmentJourneyId1", "packageJourneySegmentId1", FAILED_PICKUP_OR_DELIVERY, JOURNEY_REVIEW_REQUIRED));
        alerts.add(createAlert("6", "shipmentJourneyId1", "packageJourneySegmentId1", FAILED_PICKUP_OR_DELIVERY, JOURNEY_REVIEW_REQUIRED));
        alerts.add(createAlert("7", "shipmentJourneyId1", "", RISK_OF_DELAY, WARNING));
        alerts.add(createAlert("8", "", "packageJourneySegmentId1", RISK_OF_DELAY, WARNING));
        shipmentJourneyDomain.setAlerts(alerts);

        List<AlertEntity> alertEntities = new ArrayList<>();
        AlertEntity alertEntity1 = new AlertEntity();
        alertEntity1.setId("0");
        alertEntity1.setShipmentJourneyId("shipmentJourneyId1");
        alertEntity1.setMessage(FAILED_PICKUP_OR_DELIVERY.getFullMessage());
        alertEntity1.setType(JOURNEY_REVIEW_REQUIRED);
        alertEntities.add(alertEntity1);
        shipmentJourneyEntity.setAlerts(alertEntities);

        ShipmentJourneyMapper.setJourneyEntityAlerts(shipmentJourneyEntity, shipmentJourneyDomain);

        assertThat(shipmentJourneyEntity.getAlerts()).hasSize(6);

    }

    private static Alert createAlert(String id, String shipmentJourneyId, String packageJourneySegmentId,
                                     AlertMessage message, AlertType alertType) {
        Alert alert = new Alert(message, alertType);
        alert.setId(id);
        alert.setShipmentJourneyId(shipmentJourneyId);
        alert.setPackageJourneySegmentId(packageJourneySegmentId);
        return alert;
    }

}
