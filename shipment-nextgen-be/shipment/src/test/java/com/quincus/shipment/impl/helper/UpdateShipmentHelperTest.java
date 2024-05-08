package com.quincus.shipment.impl.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.OrderAttachment;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.mapper.MapperTestUtil;
import com.quincus.shipment.impl.mapper.PackageMapper;
import com.quincus.shipment.impl.mapper.ShipmentJourneyMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.service.AddressService;
import com.quincus.shipment.impl.service.CustomerService;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.OrderService;
import com.quincus.shipment.impl.service.OrganizationService;
import com.quincus.shipment.impl.service.ServiceTypeService;
import com.quincus.shipment.impl.service.ShipmentFetchService;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateShipmentHelperTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    private final MapperTestUtil mapperTestUtil = MapperTestUtil.getInstance();
    @Spy
    private final ObjectMapper objectMapper = testUtil.getObjectMapper();
    @InjectMocks
    private UpdateShipmentHelper updateShipmentHelper;
    @Mock
    private LocationHierarchyService locationHierarchyService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private CustomerService customerService;
    @Mock
    private ServiceTypeService serviceTypeService;
    @Mock
    private ShipmentFetchService shipmentFetchService;
    @Mock
    private MilestoneService milestoneService;
    @Mock
    private AddressService addressService;

    @Test
    void updatePackageEntityFromDomain_packageDomainNull_shouldNotUpdatePackageEntity() {
        Package domain = testUtil.createSingleShipmentData().getShipmentPackage();
        PackageEntity entity = PackageMapper.toEntity(domain);

        updateShipmentHelper.updatePackageEntityFromDomain(null, entity);

        assertThat(domain.getId())
                .withFailMessage("Package ID mismatch.")
                .isEqualTo(entity.getId());
        assertThat(domain.getType())
                .withFailMessage("Package Type mismatch.")
                .isEqualTo(entity.getType());
        assertThat(domain.getCurrency())
                .withFailMessage("Package Currency mismatch.")
                .isEqualTo(entity.getCurrency());
        assertThat(domain.getTotalValue())
                .withFailMessage("Package Total Value mismatch.")
                .isEqualTo(entity.getTotalValue());
        assertThat(domain.getValue())
                .withFailMessage("Package Value mismatch.")
                .isEqualTo(entity.getValue());
        assertThat(domain.getReadyTime())
                .withFailMessage("Package Ready Time mismatch.")
                .isEqualTo(entity.getReadyTime());
        assertThat(domain.getSource())
                .withFailMessage("Package Source mismatch.")
                .isEqualTo(entity.getSource());
    }

    @Test
    void updateShipmentEntityFromDomain_shipmentDomain_shouldUpdateShipmentEntity() {
        Shipment domain = testUtil.createSingleShipmentData();
        ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);

        assertThat(entity).isNotNull();
        domain.setPickUpLocation(domain.getPickUpLocation() + "_updated");
        domain.setDeliveryLocation(domain.getDeliveryLocation() + "_updated");
        domain.setReturnLocation(domain.getReturnLocation() + "_updated");

        List<String> extraCareInfoList = domain.getExtraCareInfo();
        extraCareInfoList.replaceAll(e -> e + "_updated");
        extraCareInfoList.add("new-extra-care");
        domain.setExtraCareInfo(extraCareInfoList);

        List<String> insuranceInfoList = domain.getInsuranceInfo();
        insuranceInfoList.replaceAll(e -> e + "_updated");
        insuranceInfoList.add("new-insurance");
        domain.setInsuranceInfo(insuranceInfoList);

        ServiceType serviceType = domain.getServiceType();
        serviceType.setId(serviceType.getId() + "_updated");
        serviceType.setCode(serviceType.getCode() + "updated");
        serviceType.setName(serviceType.getName() + "_updated");
        domain.setServiceType(serviceType);

        domain.setUserId(domain.getUserId() + "_updated");

        Sender sender = domain.getSender();
        sender.setName(sender.getName() + "_updated");
        sender.setEmail(sender.getEmail() + "_updated");
        sender.setContactNumber(sender.getContactNumber() + "_updated");
        domain.setSender(sender);

        Consignee consignee = domain.getConsignee();
        consignee.setName(consignee.getName() + "_updated");
        consignee.setEmail(consignee.getEmail() + "_updated");
        consignee.setContactNumber(consignee.getContactNumber() + "_updated");
        domain.setConsignee(consignee);

        Address origin = domain.getOrigin();
        origin.setCountry(origin.getCountry() + "_updated");
        origin.setState(origin.getState() + "_updated");
        origin.setCity(origin.getCity() + "_updated");
        origin.setLine1(origin.getLine1() + "_updated");
        origin.setLine2(origin.getLine2() + "_updated");
        origin.setLine3(origin.getLine3() + "_updated");
        origin.setPostalCode(origin.getPostalCode() + "_updated");
        origin.setFullAddress(origin.getFullAddress() + "_updated");
        origin.setLatitude(origin.getLatitude() + "_updated");
        origin.setLongitude(origin.getLongitude() + "_updated");
        origin.setManualCoordinates(!origin.isManualCoordinates());
        domain.setOrigin(origin);

        Address destination = domain.getDestination();
        destination.setCountry(destination.getCountry() + "_updated");
        destination.setState(destination.getState() + "_updated");
        destination.setCity(destination.getCity() + "_updated");
        destination.setLine1(destination.getLine1() + "_updated");
        destination.setLine2(destination.getLine2() + "_updated");
        destination.setLine3(destination.getLine3() + "_updated");
        destination.setPostalCode(destination.getPostalCode() + "_updated");
        destination.setFullAddress(destination.getFullAddress() + "_updated");
        destination.setLatitude(destination.getLatitude() + "_updated");
        destination.setLongitude(destination.getLongitude() + "_updated");
        destination.setManualCoordinates(!destination.isManualCoordinates());
        domain.setDestination(destination);

        Package shipmentPackage = domain.getShipmentPackage();
        shipmentPackage.setTotalValue(shipmentPackage.getTotalValue().negate());
        shipmentPackage.setCurrency(shipmentPackage.getCurrency() + "_updated");
        shipmentPackage.setType(shipmentPackage.getType() + "_updated");
        shipmentPackage.setValue(shipmentPackage.getValue() + "_updated");
        shipmentPackage.setReadyTime(shipmentPackage.getReadyTime().plusHours(2));
        shipmentPackage.setSource(TriggeredFrom.SHP);

        PackageDimension packageDimension = shipmentPackage.getDimension();
        packageDimension.setId(packageDimension.getId() + "_updated");
        packageDimension.setMeasurementUnit(packageDimension.getMeasurementUnit());
        packageDimension.setLength(packageDimension.getLength().add(new BigDecimal("0.01")));
        packageDimension.setWidth(packageDimension.getWidth().add(new BigDecimal("0.01")));
        packageDimension.setHeight(packageDimension.getHeight().add(new BigDecimal("0.01")));
        packageDimension.setVolumeWeight(packageDimension.getVolumeWeight().add(new BigDecimal("0.01")));
        packageDimension.setGrossWeight(packageDimension.getGrossWeight().add(new BigDecimal("0.01")));
        packageDimension.setChargeableWeight(packageDimension.getChargeableWeight().add(new BigDecimal("0.01")));
        packageDimension.setCustom(!packageDimension.isCustom());
        shipmentPackage.setDimension(packageDimension);

        List<Commodity> commodityList = shipmentPackage.getCommodities();
        commodityList.forEach(commodity -> {
            commodity.setId(commodity.getId() + "_updated");
            commodity.setName(commodity.getName() + "_updated");
            commodity.setQuantity(commodity.getQuantity() + 1L);
            commodity.setValue(commodity.getValue().add(new BigDecimal("0.01")));
        });
        shipmentPackage.setCommodities(commodityList);

        PricingInfo pricingInfo = shipmentPackage.getPricingInfo();
        pricingInfo.setId(pricingInfo.getId() + "_updated");
        pricingInfo.setCurrency(pricingInfo.getCurrency() + "_updated");
        pricingInfo.setBaseTariff(pricingInfo.getBaseTariff().add(new BigDecimal("0.01")));
        pricingInfo.setServiceTypeCharge(pricingInfo.getServiceTypeCharge().add(new BigDecimal("0.01")));
        pricingInfo.setSurcharge(pricingInfo.getSurcharge().add(new BigDecimal("0.01")));
        pricingInfo.setInsuranceCharge(pricingInfo.getInsuranceCharge().add(new BigDecimal("0.01")));
        pricingInfo.setExtraCareCharge(pricingInfo.getExtraCareCharge().add(new BigDecimal("0.01")));
        pricingInfo.setDiscount(pricingInfo.getDiscount().add(new BigDecimal("0.01")));
        pricingInfo.setTax(pricingInfo.getTax().add(new BigDecimal("0.01")));
        pricingInfo.setCod(pricingInfo.getCod().add(new BigDecimal("0.01")));
        shipmentPackage.setPricingInfo(pricingInfo);
        domain.setShipmentPackage(shipmentPackage);

        ShipmentJourney shipmentJourney = domain.getShipmentJourney();
        shipmentJourney.setStatus(JourneyStatus.COMPLETED);

        List<PackageJourneySegment> packageJourneySegmentList = shipmentJourney.getPackageJourneySegments();
        for (PackageJourneySegment pjs : packageJourneySegmentList) {
            pjs.setOpsType(pjs.getOpsType() + "_updated");
            pjs.setStatus(SegmentStatus.COMPLETED);
            pjs.setTransportType(TransportType.SEA);
            pjs.setServicedBy(pjs.getServicedBy() + "_updated");
        }
        shipmentJourney.setPackageJourneySegments(packageJourneySegmentList);
        domain.setShipmentJourney(shipmentJourney);

        Organization organization = domain.getOrganization();
        organization.setId(organization.getId() + "_updated");
        organization.setCode(organization.getCode() + "_updated");
        organization.setName(organization.getName() + "_updated");
        domain.setOrganization(organization);

        Order order = domain.getOrder();
        order.setId(order.getId() + "_updated");
        order.setGroup(order.getGroup() + "_updated");

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(organization.getId());
        organizationEntity.setCode(organization.getCode());
        organizationEntity.setName(organization.getName());

        List<String> orderCustomerReferenceIdList = order.getCustomerReferenceId();
        orderCustomerReferenceIdList.replaceAll(e -> e + "_updated");
        orderCustomerReferenceIdList.add("new-reference");
        order.setCustomerReferenceId(orderCustomerReferenceIdList);

        order.setOrderIdLabel(order.getOrderIdLabel() + "_updated");
        order.setNotes(order.getNotes() + "_updated");

        List<String> orderTagList = order.getTags();
        orderTagList.replaceAll(e -> e + "_updated");
        orderTagList.add("new-tag");
        order.setTags(orderTagList);

        List<OrderAttachment> orderAttachmentList = order.getAttachments();
        orderAttachmentList.forEach(attachment -> {
            attachment.setFileName(attachment.getFileName() + "_updated");
            attachment.setFileUrl(attachment.getFileUrl() + "_updated");
        });
        order.setAttachments(orderAttachmentList);
        domain.setOrder(order);

        Customer customer = domain.getCustomer();
        customer.setId(customer.getId() + "_updated");
        customer.setCode(customer.getCode() + "_updated");
        customer.setName(customer.getName() + "_updated");
        customer.setOrganizationId(customer.getOrganizationId() + "_updated");
        domain.setCustomer(customer);

        domain.setStatus(ShipmentStatus.COMPLETED);

        List<Instruction> instructions = domain.getInstructions();
        instructions.forEach(instruction -> {
            instruction.setId(instruction.getId() + "_updated");
            instruction.setLabel(instruction.getLabel() + "_updated");
            instruction.setSource(instruction.getSource() + "_updated");
            instruction.setValue(instruction.getValue() + "_updated");
            instruction.setApplyTo(instruction.getApplyTo());
        });
        domain.setInstructions(instructions);
        domain.setNotes(domain.getNotes() + "_updated");

        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);

        when(organizationService.findOrCreateOrganization()).thenReturn(organizationEntity);
        when(orderService.findById(anyString())).thenReturn(null);
        when(customerService.findOrCreateCustomer(any(CustomerEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(serviceTypeService.findOrCreateServiceType(any(ServiceTypeEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        updateShipmentHelper.updateShipmentEntityFromDomain(domain, entity);

        mapperTestUtil.shipmentDomainToEntity_commonAsserts(domain, entity);
    }

    @Test
    void updateShipmentEntityFromDomain_shipmentDomainNoChanges_shouldNotUpdateShipmentEntity() {
        Shipment domain = testUtil.createSingleShipmentData();
        domain.setEtaStatus(EtaStatus.DELAYED);
        ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);
        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);
        domain.setUserId("7fcec673-cc5d-5f33-ae99-5981a517b0bf");
        domain.setPartnerId(domain.getPartnerId());

        when(organizationService.findOrCreateOrganization()).thenReturn(entity.getOrganization());
        when(customerService.findOrCreateCustomer(any(CustomerEntity.class))).thenReturn(entity.getCustomer());
        when(serviceTypeService.findOrCreateServiceType(any(ServiceTypeEntity.class))).thenReturn(entity.getServiceType());

        updateShipmentHelper.updateShipmentEntityFromDomain(domain, entity);

        assertThat(entity.getUserId())
                .isEqualTo(domain.getUserId());
        mapperTestUtil.shipmentDomainToEntity_commonAsserts(domain, entity);
    }

    @Test
    void updateShipmentEntityFromDomain_shipmentDomainNull_shouldNotUpdateShipmentEntity() {
        Shipment domain = testUtil.createSingleShipmentData();
        ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);
        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);
        assertThat(entity)
                .isNotNull();
        updateShipmentHelper.updateShipmentEntityFromDomain(null, entity);

        mapperTestUtil.shipmentDomainToEntity_commonAsserts(domain, entity);
    }

    @Test
    void updateShipmentEntityFromDomain_segmentsUpdatedIsFalse_shouldNotUpdateShipmentJourneyEntity() {
        Shipment domain = testUtil.createSingleShipmentData();
        domain.setEtaStatus(EtaStatus.DELAYED);
        ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);
        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);
        domain.setUserId("ff808081833cd35b01833ce14392000a");
        domain.setPartnerId(domain.getPartnerId());
        ShipmentJourney shipmentJourney = domain.getShipmentJourney();
        shipmentJourney.setStatus(JourneyStatus.FAILED);

        when(organizationService.findOrCreateOrganization()).thenReturn(entity.getOrganization());
        when(customerService.findOrCreateCustomer(any(CustomerEntity.class))).thenReturn(entity.getCustomer());
        when(serviceTypeService.findOrCreateServiceType(any(ServiceTypeEntity.class))).thenReturn(entity.getServiceType());

        updateShipmentHelper.updateShipmentEntityFromDomain(domain, entity);

        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getShipmentJourney().getStatus()).isNotEqualTo(domain.getShipmentJourney().getStatus());
    }

    @Test
    void updateShipmentEntityFromDomain_segmentsUpdatedIsTrue_shouldUpdateShipmentJourneyEntity() {
        Shipment domain = testUtil.createSingleShipmentData();
        domain.setEtaStatus(EtaStatus.DELAYED);
        ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);
        domain.setUserId("ff808081833cd35b01833ce14392000a");
        domain.setPartnerId(domain.getPartnerId());
        ShipmentJourney shipmentJourney = domain.getShipmentJourney();
        shipmentJourney.setStatus(JourneyStatus.FAILED);
        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);

        when(organizationService.findOrCreateOrganization()).thenReturn(entity.getOrganization());
        when(customerService.findOrCreateCustomer(any(CustomerEntity.class))).thenReturn(entity.getCustomer());
        when(serviceTypeService.findOrCreateServiceType(any(ServiceTypeEntity.class))).thenReturn(entity.getServiceType());

        updateShipmentHelper.updateShipmentEntityFromDomain(domain, entity);

        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getShipmentJourney().getStatus()).isEqualTo(domain.getShipmentJourney().getStatus());
    }

    @Test
    void updateShipmentEntityFromDomain_mapperNull_shouldNotUpdateShipmentEntity() {
        Shipment domain = testUtil.createSingleShipmentData();
        ShipmentEntity entity = ShipmentMapper.mapDomainToEntity(domain, objectMapper);
        ShipmentJourneyEntity shipmentJourneyEntity = ShipmentJourneyMapper.mapDomainToEntity(domain.getShipmentJourney());
        entity.setShipmentJourney(shipmentJourneyEntity);
        assertThat(entity).isNotNull();

        when(organizationService.findOrCreateOrganization()).thenReturn(entity.getOrganization());
        when(customerService.findOrCreateCustomer(any(CustomerEntity.class))).thenReturn(entity.getCustomer());
        when(serviceTypeService.findOrCreateServiceType(any(ServiceTypeEntity.class))).thenReturn(entity.getServiceType());

        updateShipmentHelper.updateShipmentEntityFromDomain(domain, entity);

        mapperTestUtil.shipmentDomainToEntity_commonAsserts(domain, entity);
    }

    @Test
    void updateEtaStatus_shipmentDomainCompleted_shouldSetEtaStatusToNull() {
        Shipment shipmentDomain = testUtil.createSingleShipmentData();
        shipmentDomain.setEtaStatus(EtaStatus.ON_TIME);
        shipmentDomain.setStatus(ShipmentStatus.COMPLETED);

        Shipment resultShipment = updateShipmentHelper.updateEtaStatus(shipmentDomain);

        assertThat(resultShipment).isNotNull();
        assertThat(resultShipment.getEtaStatus()).isNull();
    }

    @Test
    void updateEtaStatus_shipmentDomainCancelled_shouldSetEtaStatusToNull() {
        Shipment shipmentDomain = testUtil.createSingleShipmentData();
        shipmentDomain.setEtaStatus(EtaStatus.ON_TIME);
        shipmentDomain.setStatus(ShipmentStatus.CANCELLED);

        Shipment resultShipment = updateShipmentHelper.updateEtaStatus(shipmentDomain);

        assertThat(resultShipment).isNotNull();
        assertThat(resultShipment.getEtaStatus()).isNull();
    }

    @Test
    void shipmentStatusCancelled_isCancelled_shouldReturnTrue() {
        ShipmentEntity shipment = mock(ShipmentEntity.class);
        when(shipment.getStatus()).thenReturn(ShipmentStatus.CANCELLED);
        assertThat(updateShipmentHelper.isShipmentCancelled(shipment)).isTrue();
    }

    @Test
    void journeyStatusCancelled_isCancelled_shouldReturnTrue() {
        ShipmentEntity shipment = mock(ShipmentEntity.class);
        ShipmentJourneyEntity shipmentJourneyEntity = mock(ShipmentJourneyEntity.class);
        when(shipment.getShipmentJourney()).thenReturn(shipmentJourneyEntity);
        when(shipmentJourneyEntity.getStatus()).thenReturn(JourneyStatus.CANCELLED);
        assertThat(updateShipmentHelper.isShipmentCancelled(shipment)).isTrue();
    }

    @Test
    void shipmentHasCancelledMilestone_isCancelled_shouldReturnTrue() {
        ShipmentEntity shipment = mock(ShipmentEntity.class);
        Set<MilestoneEntity> milestoneEntities = new HashSet<>();
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setMilestoneCode(MilestoneCode.OM_ORDER_CANCELED);
        milestoneEntities.add(milestoneEntity);
        when(shipment.getMilestoneEvents()).thenReturn(milestoneEntities);
        assertThat(updateShipmentHelper.isShipmentCancelled(shipment)).isTrue();
    }

    @Test
    void givenShipmentIsNotCancelledWhenIsCancelledThenShouldReturnFalse() {
        ShipmentEntity shipment = mock(ShipmentEntity.class);
        when(shipment.getStatus()).thenReturn(ShipmentStatus.CREATED);

        ShipmentJourneyEntity shipmentJourneyEntity = mock(ShipmentJourneyEntity.class);
        when(shipment.getShipmentJourney()).thenReturn(shipmentJourneyEntity);
        when(shipmentJourneyEntity.getStatus()).thenReturn(JourneyStatus.IN_PROGRESS);

        Set<MilestoneEntity> milestoneEntities = new HashSet<>();
        MilestoneEntity milestoneEntity = new MilestoneEntity();
        milestoneEntity.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestoneEntities.add(milestoneEntity);
        when(shipment.getMilestoneEvents()).thenReturn(milestoneEntities);

        assertThat(updateShipmentHelper.isShipmentCancelled(shipment)).isFalse();
    }

    @Test
    void testGetShipmentById() {
        ShipmentEntity entity = new ShipmentEntity();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        entity.setShipmentJourney(shipmentJourneyEntity);
        entity.setId("123");

        Shipment shipment = new Shipment();
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipment.setShipmentJourney(shipmentJourney);

        when(shipmentFetchService.findByIdOrThrowException("123")).thenReturn(entity);

        Shipment result = updateShipmentHelper.getShipmentById("123");

        verify(shipmentFetchService, times(1)).findByIdOrThrowException("123");
        verify(milestoneService, times(1)).setMilestoneEventsForShipment(any(), any());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("123");
    }

    @Test
    void updateOrderEntityFromDomain_orderEntityVsDomainSameId_shouldUpdate() {
        String orderId = "abc-order-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setNotes("old-notes");
        InstructionEntity oldInstruction1 = new InstructionEntity();
        oldInstruction1.setId("instruction-1");
        oldInstruction1.setExternalId("abcd-instruction-1");
        oldInstruction1.setSource("customer");
        oldInstruction1.setLabel("Customer Instruction");
        oldInstruction1.setValue("Handle Customer with Care");
        InstructionEntity oldInstruction2 = new InstructionEntity();
        oldInstruction2.setId("instruction-2");
        oldInstruction2.setExternalId("abcd-instruction-2");
        oldInstruction2.setSource("vendor");
        oldInstruction2.setLabel("Vendor Instruction");
        oldInstruction2.setValue("Handle Vendor with Care");
        List<InstructionEntity> oldInstructions = new ArrayList<>(List.of(oldInstruction1, oldInstruction2));
        orderEntity.setInstructions(oldInstructions);
        Order orderDomain = new Order();
        orderDomain.setId(orderId);
        orderDomain.setNotes("new-notes");
        Instruction newInstruction = new Instruction();
        newInstruction.setExternalId(oldInstruction1.getExternalId());
        newInstruction.setSource("client");
        newInstruction.setLabel("Client Instruction");
        newInstruction.setValue("Handle Client with Care");
        List<Instruction> newInstructions = new ArrayList<>(List.of(newInstruction));
        orderDomain.setInstructions(newInstructions);

        OrderEntity updatedEntity = updateShipmentHelper.updateOrderEntityFromDomain(orderDomain, orderEntity);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getId()).isEqualTo(orderId);
        assertThat(updatedEntity.getNotes()).isEqualTo(orderDomain.getNotes());
        assertThat(updatedEntity.getInstructions()).hasSize(1);
        InstructionEntity updatedInstruction = updatedEntity.getInstructions().get(0);
        assertThat(updatedInstruction.getId()).isEqualTo(oldInstruction1.getId());
        assertThat(updatedInstruction.getExternalId()).isEqualTo(oldInstruction1.getExternalId());
        assertThat(updatedInstruction.getSource()).isEqualTo(newInstruction.getSource());
        assertThat(updatedInstruction.getLabel()).isEqualTo(newInstruction.getLabel());
        assertThat(updatedInstruction.getValue()).isEqualTo(newInstruction.getValue());
    }

    @Test
    void updateOrderEntityFromDomain_orderEntityNewInstructions_shouldUpdate() {
        String orderId = "abc-order-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setNotes("old-notes");
        orderEntity.setInstructions(new ArrayList<>());
        Order orderDomain = new Order();
        orderDomain.setId(orderId);
        orderDomain.setNotes("new-notes");
        Instruction newInstruction = new Instruction();
        newInstruction.setExternalId("abcd-instruction-x-1");
        newInstruction.setSource("client");
        newInstruction.setLabel("Client Instruction");
        newInstruction.setValue("Handle Client with Care");
        List<Instruction> newInstructions = new ArrayList<>(List.of(newInstruction));
        orderDomain.setInstructions(newInstructions);

        OrderEntity updatedEntity = updateShipmentHelper.updateOrderEntityFromDomain(orderDomain, orderEntity);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getId()).isEqualTo(orderId);
        assertThat(updatedEntity.getNotes()).isEqualTo(orderDomain.getNotes());
        assertThat(updatedEntity.getInstructions()).hasSize(1);
        InstructionEntity updatedInstruction = updatedEntity.getInstructions().get(0);
        assertThat(updatedInstruction.getExternalId()).isEqualTo(newInstruction.getExternalId());
        assertThat(updatedInstruction.getSource()).isEqualTo(newInstruction.getSource());
        assertThat(updatedInstruction.getLabel()).isEqualTo(newInstruction.getLabel());
        assertThat(updatedInstruction.getValue()).isEqualTo(newInstruction.getValue());
    }

    @Test
    void updateOrderEntityFromDomain_noExistingOrder_shouldCreateNewOrder() {
        String orderId = "abc-order-1";
        Order orderDomain = new Order();
        orderDomain.setId(orderId);
        orderDomain.setNotes("new-notes");

        OrderEntity updatedEntity = updateShipmentHelper.updateOrderEntityFromDomain(orderDomain, null);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getId()).isEqualTo(orderId);
        assertThat(updatedEntity.getNotes()).isEqualTo(orderDomain.getNotes());
    }

    @Test
    void updateOrderEntityFromDomain_otherExistingOrder_shouldLookupExistingOrder() {
        String orderId = "abc-order-1";
        String orderId2 = "other-order-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setNotes("old-notes");
        Order orderDomain = new Order();
        orderDomain.setId(orderId2);
        orderDomain.setNotes("new-notes");

        OrderEntity otherOrder = new OrderEntity();
        otherOrder.setId(orderId2);
        otherOrder.setNotes("other-notes");

        when(orderService.findById(orderId2)).thenReturn(otherOrder);

        OrderEntity updatedEntity = updateShipmentHelper.updateOrderEntityFromDomain(orderDomain, orderEntity);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getId()).isEqualTo(orderId2);
        assertThat(updatedEntity.getNotes()).isEqualTo(otherOrder.getNotes());

        verify(orderService, times(1)).findById(anyString());
    }

    @Test
    void updateOrderEntityFromDomain_lookupFailed_shouldCreateNewOrder() {
        String orderId = "abc-order-1";
        String orderId2 = "other-order-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setNotes("old-notes");
        Order orderDomain = new Order();
        orderDomain.setId(orderId2);
        orderDomain.setNotes("new-notes");

        when(orderService.findById(orderId2)).thenReturn(null);

        OrderEntity updatedEntity = updateShipmentHelper.updateOrderEntityFromDomain(orderDomain, orderEntity);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getId()).isEqualTo(orderId2);
        assertThat(updatedEntity.getNotes()).isEqualTo(orderDomain.getNotes());

        verify(orderService, times(1)).findById(anyString());
    }

    @Test
    void updateOrderEntityFromDomain_orderIdAbsent_shouldCreateNewOrder() {
        String orderId = "abc-order-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setNotes("old-notes");
        Order orderDomain = new Order();
        orderDomain.setNotes("new-notes");

        OrderEntity updatedEntity = updateShipmentHelper.updateOrderEntityFromDomain(orderDomain, orderEntity);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getNotes()).isEqualTo(orderDomain.getNotes());
    }

    @Test
    void updateOrderEntityFromDomain_noOrder_shouldSkip() {
        String orderId = "abc-order-1";
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setNotes("old-notes");

        assertThat(updateShipmentHelper.updateOrderEntityFromDomain(null, orderEntity)).isNull();
    }
}
