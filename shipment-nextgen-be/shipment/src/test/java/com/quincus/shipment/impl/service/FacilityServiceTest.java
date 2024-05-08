package com.quincus.shipment.impl.service;

import com.quincus.qportal.model.QPortalLocation;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.SegmentException;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {
    static String sFacilityName = "StartFacility";
    static String sFacilityCountry = "US";
    static String sFacilityState = "MA";
    static String sFacilityCity = "Boston";
    static String eFacilityName = "EndFacility";
    static String eFacilityCountry = "CN";
    static String eFacilityState = "BJ";
    static String eFacilityCity = "Beijing";
    static String organizationId = UUID.randomUUID().toString();
    @InjectMocks
    private FacilityService facilityService;
    @Mock
    private LocationHierarchyService locationHierarchyService;
    @Mock
    private QPortalService qPortalService;
    @Mock
    private AddressService addressService;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    private static Facility createDummyFacility(String name, String country,
                                                String state, String city) {
        Facility facility = new Facility();
        facility.setName(name);
        facility.setExternalId("EXTERNAL-ID");
        facility.setGroup("GROUP");
        facility.setCode("CODE");
        facility.setType("TYPE");
        facility.setFunction("FUNCTION");
        facility.setStatus("STATUS");
        facility.setTag("TAG");
        facility.setNote("NOTE");
        facility.setLocationCode("MNL");

        Address address = new Address();
        address.setCountry(country);
        address.setState(state);
        address.setCity(city);
        address.setLine1("LINE 1");
        address.setLine2("LINE 2");
        address.setLine3("LINE 3");
        facility.setLocation(address);
        return facility;
    }

    private static Facility createDummyFacilityWithNullAddress(String name) {
        Facility facility = new Facility();
        facility.setName(name);
        facility.setExternalId("EXTERNAL-ID");
        facility.setGroup("GROUP");
        facility.setCode("CODE");
        facility.setType("TYPE");
        facility.setFunction("FUNCTION");
        facility.setStatus("STATUS");
        facility.setTag("TAG");
        facility.setNote("NOTE");
        return facility;
    }

    private static Shipment createShipmentDomainWithPackageJourneySegment() {
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney()
                .setPackageJourneySegments(List.of(new PackageJourneySegment()));
        String opsType = "opsType1";
        String servicedBy = "servicedBy1";
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).setOpsType(opsType);
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0)
                .setStatus(SegmentStatus.PLANNED);
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0)
                .setTransportType(TransportType.AIR);
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0)
                .setType(SegmentType.LAST_MILE);
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).setServicedBy(servicedBy);
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0)
                .setStartFacility(createDummyFacility(sFacilityName, sFacilityCountry, sFacilityState, sFacilityCity));
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0)
                .setEndFacility(createDummyFacility(eFacilityName, eFacilityCountry, eFacilityState, eFacilityCity));
        return shipmentDomain;
    }

    private static void assertOrganizationIdsAreAllPresentInPackageJourneySegments(List<PackageJourneySegmentEntity> entityPackageJourneySegments) {
        assertThat(entityPackageJourneySegments.stream().filter(packageJourneySegmentEntity -> packageJourneySegmentEntity.getOrganizationId() != null).toList())
                .withFailMessage("Organization ids are not present at some packageJourneySegmentEntity!")
                .hasSameSizeAs(entityPackageJourneySegments);
    }

    @Test
    void assignFacilityToPackageJourneySegments_validArguments_shouldSetEntityPackageJourneySegmentsToTheShipmentEntity() {
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        ShipmentEntity shipmentEntity = createShipmentEntity();
        Shipment shipmentDomain = createShipmentDomainWithPackageJourneySegment();
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), shipmentDomain.getShipmentJourney());

        List<PackageJourneySegmentEntity> entityPackageJourneySegments = shipmentEntity.getShipmentJourney().getPackageJourneySegments();

        assertThat(entityPackageJourneySegments).isNotNull();
        assertOrganizationIdsAreAllPresentInPackageJourneySegments(entityPackageJourneySegments);

        verify(locationHierarchyService, times(1)).setUpLocationHierarchies(any(), any(), any(), any(), any());
        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_segmentsHasNullAddress_shouldCallQPortal() {
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        ShipmentEntity shipmentEntity = createShipmentEntity();

        Shipment shipmentDomain = createShipmentDomainWithPackageJourneySegment();
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).getStartFacility().setLocation(null);
        shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).getEndFacility().setLocation(null);

        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setAncestors("COUNTRY, STATE, CITY");
        when(qPortalService.getLocation(any())).thenReturn(qPortalLocation);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), shipmentDomain.getShipmentJourney());
        List<PackageJourneySegmentEntity> entityPackageJourneySegments = shipmentEntity.getShipmentJourney().getPackageJourneySegments();

        assertThat(entityPackageJourneySegments).isNotNull();
        assertOrganizationIdsAreAllPresentInPackageJourneySegments(entityPackageJourneySegments);

        verify(locationHierarchyService, times(1)).setUpLocationHierarchies(any(), any(), any(), any(), any());
        verify(qPortalService, times(2)).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_locationDoesNotExistInQPortal_shouldThrowSegmentException() {
        ShipmentEntity shipmentEntity = createShipmentEntity();
        Shipment shipmentDomain = createShipmentDomainWithPackageJourneySegment();
        ShipmentJourney domainShipmentJourney = shipmentDomain.getShipmentJourney();
        domainShipmentJourney.getPackageJourneySegments().get(0).getStartFacility().setLocation(null);
        when(qPortalService.getLocation(any())).thenReturn(null);

        ShipmentJourneyEntity shipmentJourney = shipmentEntity.getShipmentJourney();

        assertThatThrownBy(() -> facilityService.assignFacilityToPackageJourneySegments(shipmentJourney, domainShipmentJourney))
                .isInstanceOf(SegmentException.class);

        verify(qPortalService, times(1)).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_exceptionCallingQPortal_shouldThrowSegmentException() {
        ShipmentEntity shipmentEntity = createShipmentEntity();
        Shipment shipmentDomain = createShipmentDomainWithPackageJourneySegment();
        ShipmentJourney domainShipmentJourney = shipmentDomain.getShipmentJourney();
        domainShipmentJourney.getPackageJourneySegments().get(0).getStartFacility().setLocation(null);
        when(qPortalService.getLocation(any())).thenThrow(RuntimeException.class);
        ShipmentJourneyEntity shipmentJourney = shipmentEntity.getShipmentJourney();

        assertThatThrownBy(() -> facilityService.assignFacilityToPackageJourneySegments(shipmentJourney, domainShipmentJourney))
                .isInstanceOf(SegmentException.class);
        verify(qPortalService, times(1)).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_domainHasNoPackageJourneySegment_shouldNotExecute() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentJourney(new ShipmentJourneyEntity());

        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setShipmentJourney(new com.quincus.shipment.api.domain.ShipmentJourney());

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), shipmentDomain.getShipmentJourney());

        List<PackageJourneySegmentEntity> entityPackageJourneySegments = shipmentEntity.getShipmentJourney().getPackageJourneySegments();
        assertThat(entityPackageJourneySegments).isNull();

        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void givenFacility_whenEnrichFacilityWithLocationFromQPortal_thenShouldTriggerFromQPortalIfDataIsNotPresent() {
        //GIVEN:
        Facility facility = new Facility();
        facility.setExternalId("facilityExtId");

        QPortalLocation qPortalLocation = new QPortalLocation();
        qPortalLocation.setAddress1("address1");
        qPortalLocation.setAddress2("address2");
        qPortalLocation.setAddress3("address3");
        qPortalLocation.setName("facilityName");
        qPortalLocation.setCode("facilityCode");
        qPortalLocation.setLocationCode("facilityLocationCode");
        qPortalLocation.setCountryId("countryId");
        qPortalLocation.setStateProvinceId("stateId");
        qPortalLocation.setCityId("cityId");
        qPortalLocation.setAncestors("COUNTRY, STATE, CITY");
        when(qPortalService.getLocation("facilityExtId")).thenReturn(qPortalLocation);
        //WHEN:
        facilityService.enrichFacilityWithLocationFromQPortal(facility);
        //THEN:
        assertThat(facility).isNotNull();
        assertThat(facility.getCode()).isEqualTo("facilityCode");
        assertThat(facility.getLocationCode()).isEqualTo("facilityLocationCode");
        assertThat(facility.getName()).isEqualTo("facilityName");
        assertThat(facility.getLocation()).isNotNull();
        assertThat(facility.getLocation().getCountryId()).isEqualTo("countryId");
        assertThat(facility.getLocation().getCountryName()).isEqualTo("Country");
        assertThat(facility.getLocation().getStateId()).isEqualTo("stateId");
        assertThat(facility.getLocation().getStateName()).isEqualTo("State");
        assertThat(facility.getLocation().getCityId()).isEqualTo("cityId");
        assertThat(facility.getLocation().getCityName()).isEqualTo("City");
        assertThat(facility.getLocation().getLine1()).isEqualTo("address1");
        assertThat(facility.getLocation().getLine2()).isEqualTo("address2");
        assertThat(facility.getLocation().getLine3()).isEqualTo("address3");
    }

    @Test
    void assignFacilityToPackageJourneySegments_domainHasEmptyPackageJourneySegment_shouldNotExecute() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentJourney(new ShipmentJourneyEntity());

        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney().setPackageJourneySegments(Collections.emptyList());

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), shipmentDomain.getShipmentJourney());

        List<PackageJourneySegmentEntity> entityPackageJourneySegments = shipmentEntity.getShipmentJourney().getPackageJourneySegments();
        assertThat(entityPackageJourneySegments).isNull();

        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_nullArguments_shouldNotExecute() {
        facilityService.assignFacilityToPackageJourneySegments(null, null);

        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_argumentNullEntity_shouldNotExecute() {
        Shipment shipmentDomain = createShipmentDomainWithPackageJourneySegment();

        facilityService.assignFacilityToPackageJourneySegments(null, shipmentDomain.getShipmentJourney());

        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_argumentNullEntityPackageJourneySegment_shouldNotExecute() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        Shipment shipmentDomain = createShipmentDomainWithPackageJourneySegment();

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), shipmentDomain.getShipmentJourney());
        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_argumentNullDomain_shouldNotExecute() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentJourney(new ShipmentJourneyEntity());

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), null);
        verify(qPortalService, never()).getLocation(any());
    }

    @Test
    void assignFacilityToPackageJourneySegments_argumentNullDomainPackageJourneySegment_shouldNotExecute() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentJourney(new ShipmentJourneyEntity());
        Shipment shipmentDomain = new Shipment();

        facilityService.assignFacilityToPackageJourneySegments(shipmentEntity.getShipmentJourney(), shipmentDomain.getShipmentJourney());
        verify(qPortalService, never()).getLocation(any());
    }

    private ShipmentEntity createShipmentEntity() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        ShipmentJourneyEntity shipmentJourneyEntity = new ShipmentJourneyEntity();
        shipmentJourneyEntity.addPackageJourneySegment(new PackageJourneySegmentEntity());
        shipmentEntity.setShipmentJourney(shipmentJourneyEntity);
        return shipmentEntity;
    }
}