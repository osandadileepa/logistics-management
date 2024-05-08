package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentFetchServiceTest {

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShipmentFetchService shipmentFetchService;

    @Test
    void testFindByShipmentTrackingIdOrThrowException_ShouldReturnShipmentEntity_WhenShipmentExists() {
        // Arrange
        String shipmentTrackingId = "123456";
        String organizationId = "org123";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        shipmentEntity.setOrganization(organization);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));

        // Act
        ShipmentEntity result = shipmentFetchService.findByShipmentTrackingIdOrThrowException(shipmentTrackingId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getShipmentTrackingId()).isEqualTo(shipmentTrackingId);
        assertThat(result.getOrganization().getId()).isEqualTo(organizationId);
        verify(userDetailsProvider, times(1)).getCurrentOrganizationId();
        verify(shipmentRepository, times(1)).findByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, organizationId);
        verifyNoMoreInteractions(userDetailsProvider, shipmentRepository);
    }

    @Test
    void testFindByShipmentTrackingIdOrThrowException_ShouldThrowShipmentNotFoundException_WhenShipmentDoesNotExist() {
        // Arrange
        String shipmentTrackingId = "123456";
        String organizationId = "org123";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, organizationId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(() -> shipmentFetchService.findByShipmentTrackingIdOrThrowException(shipmentTrackingId))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining(shipmentTrackingId)
                .hasMessageContaining(organizationId);
        verify(userDetailsProvider, times(2)).getCurrentOrganizationId();
        verify(shipmentRepository, times(1)).findByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, organizationId);
        verifyNoMoreInteractions(userDetailsProvider, shipmentRepository);
    }

    @Test
    void testFindByFindByIdWithFetchOrThrowException_ShouldReturnShipmentEntity_WhenShipmentExists() {
        // Arrange
        String shipmentId = "123456";
        String organizationId = "org123";
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(shipmentId);
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        shipmentEntity.setOrganization(organization);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(shipmentRepository.findByIdWithFetch(shipmentId, organizationId)).thenReturn(Optional.of(shipmentEntity));

        // Act
        ShipmentEntity result = shipmentFetchService.findByIdWithFetchOrThrowException(shipmentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(shipmentId);
        assertThat(result.getOrganization().getId()).isEqualTo(organizationId);
        verify(userDetailsProvider, times(1)).getCurrentOrganizationId();
        verify(shipmentRepository, times(1)).findByIdWithFetch(shipmentId, organizationId);
        verifyNoMoreInteractions(userDetailsProvider, shipmentRepository);
    }

    @Test
    void testFindByFindByIdWithFetchOrThrowException_ShouldThrowsException_WhenNoShipmentExists() {
        // Arrange
        String shipmentId = "123456";
        String organizationId = "org123";

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(shipmentRepository.findByIdWithFetch(shipmentId, organizationId)).thenReturn(Optional.empty());

        // Act
        assertThatThrownBy(() -> shipmentFetchService.findByIdWithFetchOrThrowException(shipmentId))
                .isInstanceOf(ShipmentNotFoundException.class).hasMessage("Shipment Id 123456 not found.");

        // Assert

        verify(userDetailsProvider, times(1)).getCurrentOrganizationId();
        verify(shipmentRepository, times(1)).findByIdWithFetch(shipmentId, organizationId);
        verifyNoMoreInteractions(userDetailsProvider, shipmentRepository);
    }

    @Test
    void givenExistingIdWhenFindByIdOrThrowExceptionThenReturnShipment() {
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        when(shipmentRepository.findById("ExistingId", userDetailsProvider.getCurrentOrganizationId())).thenReturn(Optional.of(mock(ShipmentEntity.class)));

        assertThat(shipmentFetchService.findByIdOrThrowException("ExistingId")).isNotNull();
    }

    @Test
    void findAllRelatedFromOrder_validParam_shouldReturnShipmentList() {
        String orderId = "ORDER-ID";
        Order order = new Order();
        order.setId(orderId);
        String orgId = "ORG-ID";
        Organization organization = new Organization();
        organization.setId(orgId);

        Tuple dummyT = mock(Tuple.class);
        when(dummyT.get(ShipmentEntity_.ID, String.class)).thenReturn("SHP-ID");
        when(dummyT.get(ShipmentEntity_.SHIPMENT_JOURNEY, ShipmentJourneyEntity.class)).thenReturn(new ShipmentJourneyEntity());

        when(shipmentRepository.findShipmentsPartialFieldByOrderId(anyString(), anyString())).thenReturn(Arrays.asList(dummyT));
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(orgId);

        List<ShipmentEntity> shipmentDomainList = shipmentFetchService.findAllByOrderIdUsingTuple(orderId);

        assertThat(shipmentDomainList).isNotEmpty();
        assertThat(shipmentDomainList.get(0).getId()).isEqualTo("SHP-ID");

        verify(shipmentRepository, times(1)).findShipmentsPartialFieldByOrderId(orderId, orgId);
    }

    @Test
    void findAllRelatedFromOrder_noResult_shouldReturnEmptyList() {
        String orderId = "ORDER-ID";

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        when(shipmentRepository.findShipmentsPartialFieldByOrderId(orderId, "orgId")).thenReturn(Collections.emptyList());
        List<ShipmentEntity> result = shipmentFetchService.findAllByOrderIdUsingTuple(orderId);
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void givenInvalidIdWhenFindByIdOrThrowExceptionThenThrowException() {
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        when(shipmentRepository.findById("InvalidId", userDetailsProvider.getCurrentOrganizationId())).thenReturn(Optional.empty());
        String expectedErrorMsg = String.format(shipmentFetchService.ERR_SHIPMENT_NOT_FOUND, "InvalidId");

        AssertionsForClassTypes.assertThatThrownBy(() -> shipmentFetchService.findByIdOrThrowException("InvalidId"))
                .isInstanceOfSatisfying(ShipmentNotFoundException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo(expectedErrorMsg));
    }
    
    @Test
    void givenValidOrderIdWhenFindAllShipmentsByOrderIdAndOrganizationIdThenReturnShipments() {
        String orderId = UUID.randomUUID().toString();
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("orgId");
        when(shipmentRepository.findAllShipmentsByOrderIdAndOrganizationId(orderId, userDetailsProvider.getCurrentOrganizationId())).thenReturn(List.of(new ShipmentEntity()));

        assertThat(shipmentFetchService.findAllShipmentsByOrderId(orderId)).hasSize(1);
    }
}
