package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.mapper.CustomerMapper;
import com.quincus.shipment.impl.mapper.OrderMapper;
import com.quincus.shipment.impl.mapper.ServiceTypeMapper;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.valueobject.OrderShipmentMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShipmentMetadataService {
    private final OrderShipmentMetadataPostProcessService orderShipmentMetadataPostProcessService;
    private final ShipmentJourneyService shipmentJourneyService;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final OrganizationService organizationService;
    private final ServiceTypeService serviceTypeService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final UserDetailsProvider userDetailsProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderShipmentMetadata createOrderShipmentMetaData(Shipment shipment, ShipmentJourneyEntity shipmentJourney) {
        return assembleOrderShipmentMetadata(shipment, shipmentJourney);
    }

    private OrderShipmentMetadata assembleOrderShipmentMetadata(Shipment shipment, ShipmentJourneyEntity shipmentJourney) {
        log.info("Starting the creation of OrderShipmentMetadata for order id: `{}` and shipment tracking id: `{}`", shipment.getOrderId(), shipment.getShipmentTrackingId());
        final OrganizationEntity organization = findOrCreateOrganizationEntity();
        final OrderEntity order = findOrCreateOrder(shipment);
        final CustomerEntity customer = findOrCreateCustomerEntity(shipment.getCustomer());
        final AddressEntity origin = findOrCreateAddressEntity(shipment.getOrigin());
        final AddressEntity destination = findOrCreateAddressEntity(shipment.getDestination());
        final ServiceTypeEntity serviceType = findOrCreateServiceTypeEntity(shipment.getServiceType());
        orderShipmentMetadataPostProcessService.archiveOrderData(shipment.getOrder());
        log.info("Finished creating OrderShipmentMetadata for order id: `{}` and shipment tracking id: `{}`", shipment.getOrderId(), shipment.getShipmentTrackingId());
        return new OrderShipmentMetadata(organization, order, customer, origin, destination, serviceType, shipmentJourney);
    }

    private OrderEntity findOrCreateOrder(Shipment shipment) {
        OrderEntity orderEntity = OrderMapper.mapDomainToEntity(shipment.getOrder(), objectMapper);
        orderEntity.setId(shipment.getOrder().getId());
        return orderService.findOrCreateOrder(orderEntity);
    }

    private ServiceTypeEntity findOrCreateServiceTypeEntity(ServiceType serviceType) {
        ServiceTypeEntity serviceTypeEntity = ServiceTypeMapper.mapDomainToEntity(serviceType, userDetailsProvider.getCurrentOrganizationId());
        return serviceTypeService.findOrCreateServiceType(serviceTypeEntity);
    }

    private CustomerEntity findOrCreateCustomerEntity(Customer customer) {
        CustomerEntity customerEntity = CustomerMapper.mapDomainToEntity(customer, userDetailsProvider.getCurrentOrganizationId());
        return customerService.findOrCreateCustomer(customerEntity);
    }

    private AddressEntity findOrCreateAddressEntity(Address address) {
        return addressService.createAddressEntityForOrganization(address);
    }

    private OrganizationEntity findOrCreateOrganizationEntity() {
        return organizationService.findOrCreateOrganization();
    }

}
