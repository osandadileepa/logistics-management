package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.valueobject.OrderShipmentMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderShipmentMetadataServiceTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private OrderShipmentMetadataService orderShipmentMetadataService;
    @Mock
    private ShipmentJourneyService shipmentJourneyService;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private ServiceTypeService serviceTypeService;
    @Mock
    private FacilityService facilityService;
    @Mock
    private CustomerService customerService;
    @Mock
    private OrderService orderService;
    @Mock
    private AddressService addressService;
    @Mock
    private PartnerService partnerService;
    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private OrderShipmentMetadataPostProcessService orderShipmentMetadataPostProcessService;

    @Test
    void testCreateOrderShipmentMetaDataWithExistingShipments() {
        Shipment shipment = new Shipment();
        shipment.setOrderId("testOrderID");
        shipment.setShipmentTrackingId("testTrackingID");
        Order order = new Order();
        order.setId(shipment.getId());
        shipment.setOrder(order);

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(shipment.getOrderId());
        shipment.setShipmentJourney(new ShipmentJourney());

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentTrackingId(shipment.getShipmentTrackingId());
        shipmentEntity.setOrder(orderEntity);

        ShipmentJourneyEntity shipmentJourneyEntity = mock(ShipmentJourneyEntity.class);

        mockDependenciesForOrderShipmentMetadataCreation();

        OrderShipmentMetadata result = orderShipmentMetadataService.createOrderShipmentMetaData(shipment, shipmentJourneyEntity);

        assertThat(result).isNotNull();
        verify(orderShipmentMetadataPostProcessService, times(1)).archiveOrderData(order);
    }

    @Test
    void testCreateOrderShipmentMetaDataWithShipmentJourney() {
        Shipment shipment = new Shipment();
        shipment.setOrderId("testOrderID");
        shipment.setShipmentTrackingId("testTrackingID");
        Order order = new Order();
        order.setId(shipment.getId());
        shipment.setOrder(order);

        ShipmentJourneyEntity shipmentJourneyEntity = mock(ShipmentJourneyEntity.class);

        mockDependenciesForOrderShipmentMetadataCreation();

        OrderShipmentMetadata result = orderShipmentMetadataService.createOrderShipmentMetaData(shipment, shipmentJourneyEntity);

        assertThat(result).isNotNull();
        verify(orderShipmentMetadataPostProcessService, times(1)).archiveOrderData(order);
    }

    private void mockDependenciesForOrderShipmentMetadataCreation() {
        // Mock service calls
        when(organizationService.findOrCreateOrganization()).thenReturn(new OrganizationEntity());
        when(orderService.findOrCreateOrder(any())).thenReturn(new OrderEntity());
        when(customerService.findOrCreateCustomer(any())).thenReturn(new CustomerEntity());
        when(addressService.createAddressEntityForOrganization(any())).thenReturn(new AddressEntity());
        when(serviceTypeService.findOrCreateServiceType(any())).thenReturn(new ServiceTypeEntity());
    }

}
