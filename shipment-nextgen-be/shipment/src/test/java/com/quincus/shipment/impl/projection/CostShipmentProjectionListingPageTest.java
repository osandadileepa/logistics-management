package com.quincus.shipment.impl.projection;

import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.projection.CostShipmentProjectionListingPage;
import com.quincus.shipment.impl.service.AddressService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CostShipmentProjectionListingPageTest {

    private CostShipmentProjectionListingPage costShipmentProjectionListingPage;

    @BeforeEach
    public void setUp() {
        EntityManager entityManager = mock(EntityManager.class);
        AddressService addressService = mock(AddressService.class);
        PackageJourneySegmentService packageJourneySegmentService = mock(PackageJourneySegmentService.class);
        costShipmentProjectionListingPage = new CostShipmentProjectionListingPage(entityManager, addressService,
                packageJourneySegmentService);
    }

    @Test
    void shouldCreateShipmentEntityListFromResultReturnsEmptyList() {
        List<Tuple> tuples = new ArrayList<>();

        List<ShipmentEntity> shipmentEntities = costShipmentProjectionListingPage.createShipmentEntityListFromResult(tuples);

        assertThat(shipmentEntities).isEmpty();
    }

    @Test
    void shouldCreateShipmentEntityListFromResultWithNullTuples() {
        List<Tuple> tuples = null;

        List<ShipmentEntity> result = costShipmentProjectionListingPage.createShipmentEntityListFromResult(tuples);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCreateShipmentEntityListFromResult() {
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID().toString());
        Tuple tuple1 = mock(Tuple.class);
        when(tuple1.get(BaseEntity_.ID, String.class)).thenReturn("1");
        when(tuple1.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class)).thenReturn("ST-123");
        when(tuple1.get(ShipmentEntity_.ORIGIN_ID, String.class)).thenReturn("O-123");
        when(tuple1.get(ShipmentEntity_.DESTINATION_ID, String.class)).thenReturn("D-123");
        when(tuple1.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID, String.class)).thenReturn("SJ-123");

        when(tuple1.get(ShipmentEntity_.ORDER, OrderEntity.class)).thenReturn(order);
        Tuple tuple2 = mock(Tuple.class);
        when(tuple2.get(BaseEntity_.ID, String.class)).thenReturn("2");
        when(tuple2.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class)).thenReturn("ST-123");
        when(tuple2.get(ShipmentEntity_.ORIGIN_ID, String.class)).thenReturn("O-123");
        when(tuple2.get(ShipmentEntity_.DESTINATION_ID, String.class)).thenReturn("D-123");
        when(tuple2.get(ShipmentEntity_.SHIPMENT_JOURNEY_ID, String.class)).thenReturn("SJ-123");
        when(tuple2.get(ShipmentEntity_.ORDER, OrderEntity.class)).thenReturn(order);
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(tuple1);
        tuples.add(tuple2);

        AddressService addressService = mock(AddressService.class);
        when(addressService.getAddressByIds(any())).thenReturn(new ArrayList<>());
        PackageJourneySegmentService packageJourneySegmentService = mock(PackageJourneySegmentService.class);
        when(packageJourneySegmentService.findByShipmentJourneyIds(any())).thenReturn(new ArrayList<>());

        CostShipmentProjectionListingPage page = new CostShipmentProjectionListingPage(mock(EntityManager.class), addressService, packageJourneySegmentService);
        List<ShipmentEntity> shipmentEntities = page.createShipmentEntityListFromResult(tuples);

        assertThat(shipmentEntities).hasSize(2);
        ShipmentEntity entity1 = shipmentEntities.get(0);
        assertThat(entity1).isNotNull();
        assertThat(entity1.getId()).isEqualTo("1");
        assertThat(entity1.getOrigin()).isNotNull();
        assertThat(entity1.getDestination()).isNotNull();
        assertThat(entity1.getShipmentJourney()).isNotNull();
        assertThat(entity1.getOrder()).isNotNull();
        ShipmentEntity entity2 = shipmentEntities.get(1);
        assertThat(entity2).isNotNull();
        assertThat(entity2.getId()).isEqualTo("2");
        assertThat(entity2.getOrigin()).isNotNull();
        assertThat(entity2.getDestination()).isNotNull();
        assertThat(entity2.getShipmentJourney()).isNotNull();
        assertThat(entity2.getOrder()).isNotNull();
    }

}

