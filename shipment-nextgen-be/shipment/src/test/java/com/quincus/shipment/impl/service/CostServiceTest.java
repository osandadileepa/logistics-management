package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.CostSegment;
import com.quincus.shipment.api.domain.CostShipment;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.User;
import com.quincus.shipment.api.dto.CostSearchResponse;
import com.quincus.shipment.api.exception.InvalidCostException;
import com.quincus.shipment.api.exception.QPortalUpsertException;
import com.quincus.shipment.api.filter.CostAmountRange;
import com.quincus.shipment.api.filter.CostDateRange;
import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.api.filter.CostFilterResult;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.mapper.CostCriteriaMapper;
import com.quincus.shipment.impl.mapper.CostMapper;
import com.quincus.shipment.impl.repository.CostRepository;
import com.quincus.shipment.impl.repository.criteria.CostCriteria;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import com.quincus.shipment.impl.repository.specification.CostSpecification;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.validator.ProofOfCostValidator;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostServiceTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private CostService costService;
    @Mock
    private CostRepository costRepository;
    @Mock
    private QPortalService qPortalService;

    @Mock
    private ShipmentService shipmentService;

    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();
    @Spy
    private CostMapper costMapper;
    @Spy
    private CostCriteriaMapper costCriteriaMapper;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;
    @Mock
    private ProofOfCostValidator proofOfCostValidator;
    @Mock
    private CostPostProcessService costPostProcessService;

    @Test
    @DisplayName("given valid cost request then do not throw error")
    void shouldCreate() throws JsonProcessingException {
        Cost cost = dummyCostRequest();

        String costTypeId = cost.getCostType().getId();
        String currencyId = cost.getCurrency().getId();
        String driverId = cost.getDriverId();

        Shipment shipment = testUtil.createSingleShipmentData();
        shipment.setId(cost.getShipments().get(0).getId());
        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setSegmentId(
                cost.getShipments().get(0).getSegments().get(0).getSegmentId()
        );

        doNothing().when(proofOfCostValidator).validate(anyList());

        when(qPortalService.getCostType(costTypeId))
                .thenReturn(cost.getCostType());
        when(qPortalService.getCurrency(currencyId))
                .thenReturn(cost.getCurrency());
        when(qPortalService.getUser(driverId))
                .thenReturn(new User());
        when(shipmentService.findAllByIds(any()))
                .thenReturn(List.of(shipment));
        when(costMapper.toDomain(any())).thenReturn(cost);

        Cost result = costService.create(cost);
        assertThat(result).isNotNull();
        verify(costPostProcessService, times(1))
                .publishCostCreatedEventAndSendAdditionalCharges(any());
    }

    @Test
    @DisplayName("given cost update non-time based request then do not throw error")
    void shouldUpdateCost() throws JsonProcessingException {
        Cost existingCost = dummyCostRequest();
        existingCost.setIssuedTimezone("UTC 9:00");
        Cost updateCostRequest = dummyCostRequest();
        updateCostRequest.setIssuedTimezone("UTC 19:00");

        String costTypeId = updateCostRequest.getCostType().getId();
        String currencyId = updateCostRequest.getCurrency().getId();
        String driverId = updateCostRequest.getDriverId();

        Shipment shipment = testUtil.createSingleShipmentData();
        shipment.setId(updateCostRequest.getShipments().get(0).getId());
        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setSegmentId(
                updateCostRequest.getShipments().get(0).getSegments().get(0).getSegmentId()
        );

        doNothing().when(proofOfCostValidator).validate(anyList());

        when(qPortalService.getCostType(costTypeId))
                .thenReturn(updateCostRequest.getCostType());
        when(qPortalService.getCurrency(currencyId))
                .thenReturn(new Currency());
        when(qPortalService.getUser(driverId))
                .thenReturn(new User());
        when(shipmentService.findAllByIds(any()))
                .thenReturn(List.of(shipment));
        when(costRepository.findById(any())).thenReturn(Optional.of(Mappers.getMapper(CostMapper.class).toEntity(existingCost)));
        when(costMapper.toDomain(any())).thenReturn(updateCostRequest);

        Cost result = costService.update(updateCostRequest, updateCostRequest.getId());
        assertThat(result).isNotNull();
        verify(costPostProcessService, times(1))
                .publishCostUpdatedEventAndSendAdditionalCharges(any());
    }

    @Test
    @DisplayName("given cost update time-based request then do not throw error")
    void shouldUpdateCostWhenTimeBased() throws JsonProcessingException {
        Cost cost = dummyCostRequest();
        CostType costType = cost.getCostType();
        costType.setCategory(CostCategory.TIME_BASED);
        cost.setCostType(costType);
        cost.setCostAmount(BigDecimal.ONE);

        String driverId = cost.getDriverId();

        Shipment shipment = testUtil.createSingleShipmentData();
        shipment.setId(cost.getShipments().get(0).getId());
        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setSegmentId(
                cost.getShipments().get(0).getSegments().get(0).getSegmentId()
        );

        doNothing().when(proofOfCostValidator).validate(anyList());

        when(qPortalService.getCostType(any()))
                .thenReturn(costType);
        when(qPortalService.getUser(driverId))
                .thenReturn(new User());
        when(shipmentService.findAllByIds(any()))
                .thenReturn(List.of(shipment));
        when(costRepository.findById(any())).thenReturn(Optional.of(Mappers.getMapper(CostMapper.class).toEntity(cost)));


        assertThatNoException().isThrownBy(() -> costService.update(cost, cost.getId()));
        verify(qPortalService, times(0)).getCurrency(any());
        verify(costPostProcessService, times(1))
                .publishCostUpdatedEventAndSendAdditionalCharges(any());
    }

    @Test
    @DisplayName("given valid cost when upsert failed then throw qPortalUpsertException")
    void throwQPortalUpsertExceptionWhenUpsertFailed() throws JsonProcessingException {
        Cost cost = dummyCostRequest();
        assertThatThrownBy(() -> costService.create(cost))
                .isInstanceOf(QPortalUpsertException.class);
        verify(costPostProcessService, times(0))
                .publishCostCreatedEventAndSendAdditionalCharges(any());
    }


    @Test
    @DisplayName("given amount with decimal when cost category is TIME_BASED then throw invalid cost exception")
    void throwInvalidCostExceptionWhenInvalidAmount() throws JsonProcessingException {

        Cost cost = dummyCostRequest();

        String costTypeId = cost.getCostType().getId();

        Shipment shipment = testUtil.createSingleShipmentData();
        shipment.setId(cost.getShipments().get(0).getId());
        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setSegmentId(
                cost.getShipments().get(0).getSegments().get(0).getSegmentId()
        );

        CostType timeBaseCostType = new CostType();
        timeBaseCostType.setCategory(CostCategory.TIME_BASED);

        when(qPortalService.getCostType(costTypeId))
                .thenReturn(timeBaseCostType);

        assertThatThrownBy(() -> costService.create(cost))
                .isInstanceOf(InvalidCostException.class);
        verify(costPostProcessService, times(0))
                .publishCostCreatedEventAndSendAdditionalCharges(any());
    }

    @Test
    @DisplayName("given proof of cost not supplied when cost type proof is mandatory then throw invalid cost exception")
    void throwInvalidCostExceptionWhenMissingProofOfCost() throws JsonProcessingException {
        Cost cost = dummyCostRequest();

        String costTypeId = cost.getCostType().getId();

        Shipment shipment = testUtil.createSingleShipmentData();
        shipment.setId(cost.getShipments().get(0).getId());
        shipment.getShipmentJourney().getPackageJourneySegments().get(0).setSegmentId(
                cost.getShipments().get(0).getSegments().get(0).getSegmentId()
        );

        CostType timeBaseCostType = new CostType();
        timeBaseCostType.setProof("mandatory");
        cost.setProofOfCost(Collections.emptyList());

        when(qPortalService.getCostType(costTypeId))
                .thenReturn(timeBaseCostType);

        assertThatThrownBy(() -> costService.create(cost))
                .isInstanceOf(InvalidCostException.class);
        verify(costPostProcessService, times(0))
                .publishCostCreatedEventAndSendAdditionalCharges(any());
    }

    @Test
    @DisplayName("given existing cost entities when find all by filter then return response")
    void shouldFindAllByFilter() {
        CostFilter costFilter = createCostFilter();

        CostEntity costEntity1 = new CostEntity();
        costEntity1.setId(UUID.randomUUID().toString());
        CostEntity costEntity2 = new CostEntity();
        costEntity2.setId(UUID.randomUUID().toString());

        CostSearchResponse cost1 = new CostSearchResponse();
        cost1.setId(UUID.randomUUID().toString());
        cost1.setCostAmount(BigDecimal.valueOf(1000.00));

        CostSearchResponse cost2 = new CostSearchResponse();
        cost2.setId(UUID.randomUUID().toString());
        cost2.setCostAmount(BigDecimal.valueOf(2000.00));

        Page<CostEntity> page = new PageImpl<>(List.of(costEntity1, costEntity2), PageRequest.of(0, 10), 2L);

        when(userDetailsProvider.getCurrentLocationCoverageIds()).thenReturn(List.of("1234", "2345"));
        when(userDetailsProvider.getCurrentPartnerId()).thenReturn(UUID.randomUUID().toString());
        when(costRepository.findAll(any(CostSpecification.class), any(PageRequest.class))).thenReturn(page);
        when(costMapper.toCostResponse(costEntity1)).thenReturn(cost1);
        when(costMapper.toCostResponse(costEntity2)).thenReturn(cost2);
        when(costCriteriaMapper.mapFilterToCriteria(any(), any())).thenReturn(createCostCriteria());

        CostFilterResult result = costService.findAllByFilter(costFilter);

        assertThat(result).isNotNull();
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.result()).hasSize(2);
    }

    @Test
    @DisplayName("given incurred date range is provided but both start and end dates are null, when performing a filter")
    void shouldThrowQuincusValidationExceptionWhenDateRangeProvidedButBothDatesAreNull() {

        CostFilter costFilter = createCostFilter();
        costFilter.setIncurredDateRange(new CostDateRange());

        assertThatThrownBy(() -> costService.findAllByFilter(costFilter))
                .isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining("Both 'Incurred date from' and 'Incurred date to' must be provided");
    }

    @Test
    @DisplayName("given non-existing cost entities when find all by filter then return empty response")
    void shouldReturnEmptyResponse() {
        CostFilter costFilter = createCostFilter();

        when(costCriteriaMapper.mapFilterToCriteria(any(), any())).thenReturn(createCostCriteria());

        CostFilterResult result = costService.findAllByFilter(costFilter);

        assertThat(result).isNotNull();
        assertThat(result.totalPages()).isZero();
        assertThat(result.page()).isOne();
        assertThat(result.totalElements()).isZero();
        assertThat(result.result()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideDummyPackageJourneySegments")
    @DisplayName("given x number of segments then return expected isFirstSegment and isLastSegment value")
    void returnExpectedIsFirstSegmentAndIsLastSegment(
            boolean expectedIsFirstSegment,
            boolean expectedIsLastSegment,
            int index,
            List<PackageJourneySegment> segments
    ) throws JsonProcessingException {
        Cost cost = dummyCostRequest();

        String costTypeId = cost.getCostType().getId();
        String driverId = cost.getDriverId();

        Shipment shipment = testUtil.createSingleShipmentData();
        shipment.getShipmentJourney().setPackageJourneySegments(segments);

        CostShipment costShipment = new CostShipment();
        costShipment.setId(shipment.getId());

        List<CostSegment> costSegments = new ArrayList<>();
        segments.forEach(segment -> {
            CostSegment costSegment = new CostSegment();
            costSegment.setSegmentId(segment.getSegmentId());
            costSegments.add(costSegment);
        });
        costShipment.setSegments(costSegments);

        cost.setShipments(List.of(costShipment));

        doNothing().when(proofOfCostValidator).validate(anyList());

        when(qPortalService.getCostType(costTypeId))
                .thenReturn(new CostType());
        when(qPortalService.getUser(driverId))
                .thenReturn(new User());
        when(shipmentService.findAllByIds(any()))
                .thenReturn(List.of(shipment));

        when(costMapper.toDomain(any())).thenReturn(cost);

        Cost result = costService.create(cost);

        verify(costPostProcessService, times(1))
                .publishCostCreatedEventAndSendAdditionalCharges(any());

        assertThat(result.getSource()).isNotNull();
        assertThat(result.getShipments().get(0).getSegments().get(index).isFirstSegment()).isEqualTo(expectedIsFirstSegment);
        assertThat(result.getShipments().get(0).getSegments().get(index).isLastSegment()).isEqualTo(expectedIsLastSegment);
    }

    private CostFilter createCostFilter() {
        CostFilter costFilter = new CostFilter();
        costFilter.setPageNumber(1);
        costFilter.setSize(10);
        costFilter.setSortDir("asc");
        costFilter.setSortBy("amount");
        costFilter.setKeys(List.of("key1", "key2"));
        costFilter.setCostTypes(List.of("type1", "type2"));
        costFilter.setVendors(List.of("vendor1", "vendor2"));
        costFilter.setDrivers(List.of("by1", "by2"));

        CostAmountRange costAmountRange = new CostAmountRange();
        costAmountRange.setMinCostAmount(new BigDecimal("0.00"));
        costAmountRange.setMaxCostAmount(new BigDecimal("1000.00"));
        costFilter.setCostAmountRange(costAmountRange);

        CostDateRange incurredDateRange = new CostDateRange();
        incurredDateRange.setIncurredDateFrom(LocalDateTime.now().minusDays(10));
        incurredDateRange.setIncurredDateTo(LocalDateTime.now());
        costFilter.setIncurredDateRange(incurredDateRange);

        return costFilter;
    }

    private CostCriteria createCostCriteria() {
        CostCriteria criteria = new CostCriteria();

        criteria.setKeys(List.of("key1", "key2"));
        criteria.setCostTypes(List.of("type1", "type2"));
        criteria.setVendors(List.of("vendor1", "vendor2"));
        criteria.setDrivers(List.of("by1", "by2"));

        CostAmountRange costAmountRange = new CostAmountRange();
        costAmountRange.setMinCostAmount(new BigDecimal("0.00"));
        costAmountRange.setMaxCostAmount(new BigDecimal("1000.00"));
        criteria.setCostAmountRange(costAmountRange);

        CostDateRange incurredDateRange = new CostDateRange();
        incurredDateRange.setIncurredDateFrom(LocalDateTime.now().minusDays(10));
        incurredDateRange.setIncurredDateTo(LocalDateTime.now());
        criteria.setIncurredDateRange(incurredDateRange);

        criteria.setOrganizationId("1L");
        criteria.setPartnerId("1L");

        criteria.setUserLocationsCoverage(new HashSet<>(List.of("location1", "location2")));
        criteria.setUserPartners(List.of("partner1", "partner2"));

        criteria.setPage(1);
        criteria.setPerPage(10);

        return criteria;
    }

    private static Stream<Arguments> provideDummyPackageJourneySegments() {
        return Stream.of(
                Arguments.of(true, true, 0, dummyPackageJourneySegments(1)),
                Arguments.of(true, false, 0, dummyPackageJourneySegments(2)),
                Arguments.of(false, true, 1, dummyPackageJourneySegments(2)),
                Arguments.of(true, false, 0, dummyPackageJourneySegments(3)),
                Arguments.of(false, false, 1, dummyPackageJourneySegments(3)),
                Arguments.of(false, true, 2, dummyPackageJourneySegments(3))
        );
    }

    private static List<PackageJourneySegment> dummyPackageJourneySegments(int size) {
        List<PackageJourneySegment> dummyList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            PackageJourneySegment segment = new PackageJourneySegment();
            if (i == 0) {
                segment.setType(SegmentType.FIRST_MILE);
            } else if (i == size - 1) {
                segment.setType(SegmentType.LAST_MILE);
            } else {
                segment.setType(SegmentType.MIDDLE_MILE);
            }
            segment.setRefId(String.valueOf(i));
            segment.setSequence(String.valueOf(i));
            segment.setSegmentId(UUID.randomUUID().toString());
            dummyList.add(segment);
        }
        return dummyList;
    }

    private Cost dummyCostRequest() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/request/costRequest.json");
        return objectMapper.readValue(data.get("data").toString(), Cost.class);
    }
}
