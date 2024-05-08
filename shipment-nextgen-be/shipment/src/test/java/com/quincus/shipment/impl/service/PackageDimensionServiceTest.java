package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionErrorRecord;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.exception.PackageDimensionException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.repository.PackageDimensionRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.validator.PackageDimensionValidator;
import com.quincus.web.common.exception.model.OperationNotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDimensionServiceTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final TestUtil testUtil = TestUtil.getInstance();

    @InjectMocks
    private PackageDimensionService packageDimensionService;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private PackageDimensionRepository packageDimensionRepository;
    @Mock
    private QLoggerAPI qLoggerAPI;
    @Mock
    private MessageApi messageApi;
    @Mock
    private QPortalApi qPortalApi;
    @Mock
    private PackageDimensionValidator packageDimensionValidator;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    @Mock
    private UpdateShipmentHelper updateShipmentHelper;
    @Mock
    private PackageDimensionPostProcessService packageDimensionPostProcessService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PackageLogService packageLogService;

    @Test
    void shouldThrowException_WhenInvalidShipmentTrackingIdAndOrganizationId() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), updatePackageDimensionRequest.getOrganizationId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> packageDimensionService.updateShipmentPackageDimension(updatePackageDimensionRequest))
                .isInstanceOf(PackageDimensionException.class);
    }

    @Test
    void shouldThrowException_WhenSegmentLocationNotCovered() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal oldValue = new BigDecimal("10.123");
        packageDimensionEntity.setGrossWeight(oldValue);
        packageDimensionEntity.setLength(oldValue);
        packageDimensionEntity.setHeight(oldValue);
        packageDimensionEntity.setWidth(oldValue);
        packageDimensionEntity.setVolumeWeight(oldValue);
        packageDimensionEntity.setChargeableWeight(oldValue);
        packageDimensionEntity.setCreateTime(Instant.now());
        packageDimensionEntity.setModifyTime(Instant.now());
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDimension(packageDimensionEntity);
        shipmentEntity.setShipmentPackage(packageEntity);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), updatePackageDimensionRequest.getOrganizationId())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(false);

        assertThatThrownBy(() -> packageDimensionService.updateShipmentPackageDimension(updatePackageDimensionRequest))
                .isInstanceOf(SegmentLocationNotAllowedException.class);

        verify(messageApi, never()).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, never()).publishPackageDimensionUpdateEvent(any(), any(), any());
    }

    @Test
    void shouldReturnResponse_WhenUpdateShipmentPackageDimension() {
        String packageTypeName = "Package 1";
        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        updatePackageDimensionRequest.setMeasurementUnit(MeasurementUnit.METRIC);
        updatePackageDimensionRequest.setPackageTypeName(packageTypeName);

        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal oldValue = new BigDecimal("10.123");
        BigDecimal newValue = new BigDecimal("1.000");
        packageDimensionEntity.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimensionEntity.setGrossWeight(oldValue);
        packageDimensionEntity.setLength(oldValue);
        packageDimensionEntity.setHeight(oldValue);
        packageDimensionEntity.setWidth(oldValue);
        packageDimensionEntity.setVolumeWeight(oldValue);
        packageDimensionEntity.setChargeableWeight(oldValue);
        packageDimensionEntity.setCreateTime(Instant.now());
        packageDimensionEntity.setModifyTime(Instant.now());
        packageDimensionEntity.setCustom(false);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDimension(packageDimensionEntity);
        packageEntity.setTypeRefId("test-type-ref-id");
        shipmentEntity.setShipmentPackage(packageEntity);

        QPortalPackageType packageType = new QPortalPackageType();
        packageType.setName(packageTypeName);

        String organizationId = "5292c9e2-6758-11ee-8c99-0242ac120002";
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(qPortalApi.listPackageTypes(anyString())).thenReturn(List.of(packageType));
        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), organizationId)).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        PackageDimensionUpdateResponse response = packageDimensionService.updateShipmentPackageDimension(updatePackageDimensionRequest);
        PackageDimension oldPackageDimension = response.getPreviousPackageDimension();
        PackageDimension newPackageDimension = response.getNewPackageDimension();

        assertThat(oldPackageDimension.getGrossWeight()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getLength()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getWidth()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getHeight()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getMeasurementUnit()).isEqualTo(packageDimensionEntity.getMeasurementUnit());
        assertThat(oldPackageDimension.isCustom()).isEqualTo(packageDimensionEntity.isCustom());

        assertThat(newPackageDimension.getGrossWeight()).isEqualTo(newValue);
        assertThat(newPackageDimension.getLength()).isEqualTo(newValue);
        assertThat(newPackageDimension.getWidth()).isEqualTo(newValue);
        assertThat(newPackageDimension.getHeight()).isEqualTo(newValue);
        assertThat(newPackageDimension.getMeasurementUnit()).isEqualTo(updatePackageDimensionRequest.getMeasurementUnit());
        assertThat(newPackageDimension.isCustom()).isFalse();

        assertThat(response.getOrganizationId()).isEqualTo(organizationId);
        assertThat(response.getUpdateBy()).isEqualTo(updatePackageDimensionRequest.getUserId());
        assertThat(response.getUpdatedAt()).isEqualTo(updatePackageDimensionRequest.getUserLocationId());
        assertThat(response.getUpdatedAt()).isNotNull();

        Package shipmentPackage = response.getShipmentPackage();
        assertThat(shipmentPackage).isNotNull();
        assertThat(shipmentPackage.getId()).isEqualTo(packageEntity.getId());
        assertThat(shipmentPackage.getRefId()).isEqualTo(packageEntity.getRefId());
        assertThat(shipmentPackage.getTypeRefId()).isEqualTo(packageEntity.getTypeRefId());
        assertThat(shipmentPackage.getType()).isEqualTo(packageEntity.getType());

        verify(messageApi, times(1)).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, times(1)).publishPackageDimensionUpdateEvent(any(), any(), any());
        verify(packageDimensionPostProcessService, times(1)).createAndSendDimsAndWeightMilestoneForShipment(any(Shipment.class));
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    @Test
    void givenCustomDimensionRequest_whenUpdateShipmentPackageDimension_thenPackageNameShouldBeCustomType() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        updatePackageDimensionRequest.setPackageTypeName("");
        updatePackageDimensionRequest.setPackageTypeId("");
        updatePackageDimensionRequest.setMeasurementUnit(MeasurementUnit.METRIC);
        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal oldValue = new BigDecimal("10.123");
        BigDecimal newValue = new BigDecimal("1.000");
        packageDimensionEntity.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimensionEntity.setGrossWeight(oldValue);
        packageDimensionEntity.setLength(oldValue);
        packageDimensionEntity.setHeight(oldValue);
        packageDimensionEntity.setWidth(oldValue);
        packageDimensionEntity.setVolumeWeight(oldValue);
        packageDimensionEntity.setChargeableWeight(oldValue);
        packageDimensionEntity.setCreateTime(Instant.now());
        packageDimensionEntity.setModifyTime(Instant.now());
        packageDimensionEntity.setCustom(false);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDimension(packageDimensionEntity);
        packageEntity.setTypeRefId("test-type-ref-id");
        shipmentEntity.setShipmentPackage(packageEntity);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), updatePackageDimensionRequest.getOrganizationId())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        PackageDimensionUpdateResponse response = packageDimensionService.updateShipmentPackageDimension(updatePackageDimensionRequest);

        Package shipmentPackage = response.getShipmentPackage();
        assertThat(shipmentPackage).isNotNull();
        assertThat(shipmentPackage.getId()).isEqualTo(packageEntity.getId());
        assertThat(shipmentPackage.getRefId()).isEqualTo(packageEntity.getRefId());
        assertThat(shipmentPackage.getTypeRefId()).isEqualTo(packageEntity.getTypeRefId());
        assertThat(shipmentPackage.getType()).isEqualTo("Custom Type");

        verify(messageApi, times(1)).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, times(1)).publishPackageDimensionUpdateEvent(any(), any(), any());
        verify(packageDimensionPostProcessService, times(1)).createAndSendDimsAndWeightMilestoneForShipment(any(Shipment.class));
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    @Test
    void shouldReturnResponse_WhenUpdateShipmentPackageDimensionViaBatch() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        updatePackageDimensionRequest.setMeasurementUnit(MeasurementUnit.METRIC);
        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal oldValue = new BigDecimal("10.123");
        BigDecimal newValue = new BigDecimal("1.000");
        packageDimensionEntity.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimensionEntity.setGrossWeight(oldValue);
        packageDimensionEntity.setLength(oldValue);
        packageDimensionEntity.setHeight(oldValue);
        packageDimensionEntity.setWidth(oldValue);
        packageDimensionEntity.setVolumeWeight(oldValue);
        packageDimensionEntity.setChargeableWeight(oldValue);
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDimension(packageDimensionEntity);
        shipmentEntity.setShipmentPackage(packageEntity);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), updatePackageDimensionRequest.getOrganizationId())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        PackageDimensionUpdateResponse response = packageDimensionService.updateShipmentPackageDimension(updatePackageDimensionRequest);
        PackageDimension oldPackageDimension = response.getPreviousPackageDimension();
        PackageDimension newPackageDimension = response.getNewPackageDimension();

        assertThat(oldPackageDimension.getGrossWeight()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getLength()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getWidth()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getHeight()).isEqualTo(oldValue);
        assertThat(oldPackageDimension.getMeasurementUnit()).isEqualTo(MeasurementUnit.METRIC);

        assertThat(newPackageDimension.getGrossWeight()).isEqualTo(newValue);
        assertThat(newPackageDimension.getLength()).isEqualTo(newValue);
        assertThat(newPackageDimension.getWidth()).isEqualTo(newValue);
        assertThat(newPackageDimension.getHeight()).isEqualTo(newValue);
        assertThat(newPackageDimension.getMeasurementUnit()).isEqualTo(MeasurementUnit.METRIC);

        verify(messageApi, times(1)).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, times(1)).publishPackageDimensionUpdateEvent(any(), any(), any());
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    private PackageDimensionUpdateRequest createUpdatePackageDimensionRequest() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = new PackageDimensionUpdateRequest();
        updatePackageDimensionRequest.setUserId("userId");
        updatePackageDimensionRequest.setUserLocationId("userLocationId");
        updatePackageDimensionRequest.setShipmentTrackingId("shipmentTrackingId");
        BigDecimal value = new BigDecimal("1.000");
        updatePackageDimensionRequest.setLength(value);
        updatePackageDimensionRequest.setWidth(value);
        updatePackageDimensionRequest.setHeight(value);
        updatePackageDimensionRequest.setGrossWeight(value);
        updatePackageDimensionRequest.setPackageTypeId("ace15fc8-36b4-11ee-be56-0242ac120002");
        updatePackageDimensionRequest.setPackageTypeName("test");
        return updatePackageDimensionRequest;
    }

    @Test
    void shouldNotThrowException_WhenRequiredFieldsAreSupplied() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = new PackageDimensionUpdateRequest();
        assertThat(hasViolation(updatePackageDimensionRequest)).isTrue();
        updatePackageDimensionRequest.setOrganizationId("Org");
        assertThat(hasViolation(updatePackageDimensionRequest)).isTrue();
        updatePackageDimensionRequest.setUserId(UUID.randomUUID().toString());
        assertThat(hasViolation(updatePackageDimensionRequest)).isTrue();
        updatePackageDimensionRequest.setHeight(new BigDecimal("10.0"));
        assertThat(hasViolation(updatePackageDimensionRequest)).isTrue();
        updatePackageDimensionRequest.setLength(new BigDecimal("10.0"));
        assertThat(hasViolation(updatePackageDimensionRequest)).isTrue();
        updatePackageDimensionRequest.setWidth(new BigDecimal("10.0"));
        assertThat(hasViolation(updatePackageDimensionRequest)).isTrue();
        updatePackageDimensionRequest.setGrossWeight(new BigDecimal("10.0"));
        assertThat(hasViolation(updatePackageDimensionRequest)).isFalse();
        verify(messageApi, never()).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, never()).publishPackageDimensionUpdateEvent(any(), any(), any());
    }

    @Test
    void shouldGetPackageDimension() {
        String shipmentTrackingId = UUID.randomUUID().toString();
        String organizationId = UUID.randomUUID().toString();

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(organizationId);
        shipmentEntity.setOrganization(organizationEntity);
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        PackageEntity packageEntity = new PackageEntity();
        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal bigDecimalValue = new BigDecimal("1.000");
        packageDimensionEntity.setHeight(bigDecimalValue);
        packageDimensionEntity.setLength(bigDecimalValue);
        packageDimensionEntity.setWidth(bigDecimalValue);
        packageDimensionEntity.setVolumeWeight(bigDecimalValue);
        packageDimensionEntity.setChargeableWeight(bigDecimalValue);
        packageDimensionEntity.setGrossWeight(bigDecimalValue);
        packageEntity.setType("Small Box");
        packageEntity.setDimension(packageDimensionEntity);
        shipmentEntity.setShipmentPackage(packageEntity);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(facilityLocationPermissionChecker.isShipmentTrackingIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        Shipment shipmentWithPackageInfo = packageDimensionService.findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(shipmentTrackingId);

        assertThat(shipmentWithPackageInfo.getShipmentTrackingId()).isEqualTo(shipmentEntity.getShipmentTrackingId());
        assertThat(shipmentWithPackageInfo.getShipmentPackage().getType()).isEqualTo(shipmentEntity.getShipmentPackage().getType());
        assertThat(shipmentWithPackageInfo.getShipmentPackage().getDimension().getHeight()).isEqualTo(shipmentEntity.getShipmentPackage().getDimension().getHeight());
        assertThat(shipmentWithPackageInfo.getShipmentPackage().getDimension().getLength()).isEqualTo(shipmentEntity.getShipmentPackage().getDimension().getLength());
        assertThat(shipmentWithPackageInfo.getShipmentPackage().getDimension().getWidth()).isEqualTo(shipmentEntity.getShipmentPackage().getDimension().getWidth());

        verify(shipmentRepository, times(1)).findByShipmentTrackingIdAndOrganizationId(anyString(), anyString());
        verify(messageApi, never()).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, never()).publishPackageDimensionUpdateEvent(any(), any(), any());
    }

    @Test
    void shouldThrowShipmentNotFoundExceptionWhenGetPackageDimension() {
        String shipmentTrackingId = UUID.randomUUID().toString();
        String organizationId = UUID.randomUUID().toString();

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        doThrow(new ShipmentNotFoundException("Not an exception. Test only.")).when(shipmentRepository).findByShipmentTrackingIdAndOrganizationId(anyString(), anyString());

        assertThatThrownBy(() -> packageDimensionService.findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(shipmentTrackingId))
                .isInstanceOf(ShipmentNotFoundException.class);

        verify(shipmentRepository, times(1)).findByShipmentTrackingIdAndOrganizationId(anyString(), anyString());
        verify(messageApi, never()).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, never()).publishPackageDimensionUpdateEvent(any(), any(), any());
    }

    @Test
    void shouldThrowShipmentNotFoundExceptionWhenLocationNotCovered() {
        String shipmentTrackingId = UUID.randomUUID().toString();
        String organizationId = UUID.randomUUID().toString();

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId(organizationId);
        shipmentEntity.setOrganization(organizationEntity);
        shipmentEntity.setShipmentTrackingId(shipmentTrackingId);
        PackageEntity packageEntity = new PackageEntity();
        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal bigDecimalValue = new BigDecimal("1.000");
        packageDimensionEntity.setHeight(bigDecimalValue);
        packageDimensionEntity.setLength(bigDecimalValue);
        packageDimensionEntity.setWidth(bigDecimalValue);
        packageDimensionEntity.setVolumeWeight(bigDecimalValue);
        packageDimensionEntity.setChargeableWeight(bigDecimalValue);
        packageDimensionEntity.setGrossWeight(bigDecimalValue);
        packageEntity.setType("Small Box");
        packageEntity.setDimension(packageDimensionEntity);
        shipmentEntity.setShipmentPackage(packageEntity);

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(anyString(), anyString()))
                .thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentTrackingIdAnySegmentLocationCovered(anyString())).thenReturn(false);

        assertThatThrownBy(() -> packageDimensionService.findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(shipmentTrackingId))
                .isInstanceOf(SegmentLocationNotAllowedException.class);

        verify(shipmentRepository, times(1)).findByShipmentTrackingIdAndOrganizationId(anyString(), anyString());
        verify(messageApi, never()).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, never()).publishPackageDimensionUpdateEvent(any(), any(), any());
    }

    private boolean hasViolation(PackageDimensionUpdateRequest updatePackageDimensionRequest) {
        Set<ConstraintViolation<PackageDimensionUpdateRequest>> violations = validator.validate(updatePackageDimensionRequest);
        return !violations.isEmpty();
    }

    @Test
    void testWriteAllErrorsFromBulkUpdate_shouldBeEmpty() {
        assertThatThrownBy(() -> new BulkPackageDimensionUpdateRequest(null))
                .isInstanceOf(PackageDimensionException.class);
        String[] cells = new String[]{"test", "test", "test", "1", "1", "1", "1"};
        List<BulkPackageDimensionUpdateRequest> bulkResult = new ArrayList<>();

        List<PackageDimensionErrorRecord> records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(null);
        assertThat(records).isEmpty();

        records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        assertThat(records).isEmpty();
        BulkPackageDimensionUpdateRequest record1 = new BulkPackageDimensionUpdateRequest(cells);
        bulkResult.add(record1);
        records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        assertThat(records).isEmpty();

        BulkPackageDimensionUpdateRequest record2 = new BulkPackageDimensionUpdateRequest(new String[]{"test", "test", "test", "1", "1", "1", "1"});
        record2.setPackageTypeError("packageerror");
        record1.setHeightError("heighterror");
        bulkResult.add(record2);
        records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        assertThat(records).hasSize(2);

    }

    @Test
    void testWriteAllErrorsFromBulkUpdate_shouldHaveContent() {
        String[] cells = new String[]{"test", "test", "test", "1", "1", "1", "1"};
        List<BulkPackageDimensionUpdateRequest> bulkResult = new ArrayList<>();
        BulkPackageDimensionUpdateRequest record1 = new BulkPackageDimensionUpdateRequest(cells);
        record1.setErrorMessages("error");
        bulkResult.add(record1);
        List<PackageDimensionErrorRecord> records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        assertThat(records).hasSize(1);

        BulkPackageDimensionUpdateRequest record2 = new BulkPackageDimensionUpdateRequest(cells);
        bulkResult.add(record2);
        records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        assertThat(records).hasSize(1);

        BulkPackageDimensionUpdateRequest record3 = new BulkPackageDimensionUpdateRequest(cells);
        record3.setShipmentError("shiperror");
        bulkResult.add(record3);
        records = packageDimensionService.getAllErrorsFromBulkPackageDimensionUpdate(bulkResult);
        assertThat(records).hasSize(2);

    }

    @Test
    void testGetErrors() {
        String[] cells = new String[]{"test", "test", "test", "1", "1", "1", "1"};
        BulkPackageDimensionUpdateRequest record1 = new BulkPackageDimensionUpdateRequest(cells);
        assertThat(record1.getErrorMessages()).isNull();
        assertThat(record1.isError()).isFalse();
        BulkPackageDimensionUpdateRequest record2 = new BulkPackageDimensionUpdateRequest(cells);
        record2.setErrorMessages("generic error");
        assertThat(record2.getErrorMessages()).isNotNull();
        assertThat(record2.isError()).isTrue();
        BulkPackageDimensionUpdateRequest record3 = new BulkPackageDimensionUpdateRequest(cells);
        record3.setHeightError("height error");
        record3.setWeightError("weight error");
        assertThat(record3.getErrorMessages()).isNotNull();
        assertThat(record3.isError()).isTrue();
        assertThat(record3.getErrorMessages()).isEqualTo("height error | weight error");

    }

    @Test
    void testBulkPackageDimensionUpdate_shouldHaveError() throws IOException {
        File file = testUtil.getFile("bulkupload/bulk-package-update-template.csv");

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalApi.listPackageTypes(anyString())).thenReturn(createPackageTypes(false));
        List<BulkPackageDimensionUpdateRequest> result = packageDimensionService.bulkPackageDimensionUpdate(file);
        assertThat(result.stream().anyMatch(BulkPackageDimensionUpdateRequest::isError)).isTrue();

        when(qPortalApi.listPackageTypes(anyString())).thenReturn(createPackageTypes(true));
        result = packageDimensionService.bulkPackageDimensionUpdate(file);
        assertThat(result.stream().anyMatch(BulkPackageDimensionUpdateRequest::isError)).isTrue();
    }

    @Test
    void testBulkPackageDimensionUpdate_shouldNotHaveError() throws IOException {
        File file = testUtil.getFile("bulkupload/bulk-package-update-template.csv");

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalApi.listPackageTypes(anyString())).thenReturn(createPackageTypes(true));
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        PackageDimensionEntity packageDimension = new PackageDimensionEntity();
        packageDimension.setHeight(new BigDecimal(1));
        packageDimension.setLength(new BigDecimal(1));
        packageDimension.setWidth(new BigDecimal(1));
        packageDimension.setGrossWeight(new BigDecimal(1));
        packageDimension.setVolumeWeight(new BigDecimal(1));
        packageDimension.setChargeableWeight(new BigDecimal(1));
        PackageEntity shipmentPackage = new PackageEntity();
        shipmentPackage.setDimension(packageDimension);
        shipmentEntity.setShipmentPackage(shipmentPackage);
        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);
        List<BulkPackageDimensionUpdateRequest> result = packageDimensionService.bulkPackageDimensionUpdate(file);
        assertThat(result.stream().anyMatch(BulkPackageDimensionUpdateRequest::isError)).isFalse();
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    @Test
    void shouldThrowException_WhenUpdatingCancelledShipment() {
        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal oldValue = new BigDecimal("10.123");
        packageDimensionEntity.setGrossWeight(oldValue);
        packageDimensionEntity.setLength(oldValue);
        packageDimensionEntity.setHeight(oldValue);
        packageDimensionEntity.setWidth(oldValue);
        packageDimensionEntity.setVolumeWeight(oldValue);
        packageDimensionEntity.setChargeableWeight(oldValue);
        packageDimensionEntity.setCreateTime(Instant.now());
        packageDimensionEntity.setModifyTime(Instant.now());
        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        shipmentEntity.setStatus(ShipmentStatus.CANCELLED);
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDimension(packageDimensionEntity);
        shipmentEntity.setShipmentPackage(packageEntity);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), updatePackageDimensionRequest.getOrganizationId())).thenReturn(Optional.of(shipmentEntity));
        when(updateShipmentHelper.isShipmentCancelled(shipmentEntity)).thenReturn(true);

        assertThatThrownBy(() -> packageDimensionService.updateShipmentPackageDimension(updatePackageDimensionRequest))
                .isInstanceOf(OperationNotAllowedException.class);
    }

    private List<QPortalPackageType> createPackageTypes(boolean isValid) {
        QPortalPackageType packageType = new QPortalPackageType();
        packageType.setId("8fc4a5bc-35d8-11ee-be56-0242ac120002");
        packageType.setName("test");

        if (!isValid) {
            return List.of(packageType);
        }
        QPortalPackageType.Dimension dimension = new QPortalPackageType.Dimension();
        dimension.setHeight(new BigDecimal(1));
        dimension.setLength(new BigDecimal(1));
        dimension.setWidth(new BigDecimal(1));
        dimension.setMetric(MeasurementUnit.METRIC.toString());
        packageType.setDimension(dimension);
        packageType.setMaxGrossWeight(new BigDecimal(1));
        return List.of(packageType);
    }

    @Test
    void shouldOverrideDimensionValuesAndPackageTypeFromQPortal() throws IOException {
        File file = testUtil.getFile("bulkupload/bulk-package-update-template.csv");

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalApi.listPackageTypes(anyString())).thenReturn(createPackageTypes(true));

        PackageDimensionEntity packageDimension = new PackageDimensionEntity();
        packageDimension.setHeight(new BigDecimal(1));
        packageDimension.setLength(new BigDecimal(1));
        packageDimension.setWidth(new BigDecimal(1));
        packageDimension.setGrossWeight(new BigDecimal(1));
        packageDimension.setVolumeWeight(new BigDecimal(1));
        packageDimension.setChargeableWeight(new BigDecimal(1));

        PackageEntity shipmentPackage = new PackageEntity();
        shipmentPackage.setTypeRefId("");
        shipmentPackage.setDimension(packageDimension);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("bf0f67a0-35d6-11ee-be56-0242ac120002");
        shipmentEntity.setShipmentPackage(shipmentPackage);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        List<BulkPackageDimensionUpdateRequest> result = packageDimensionService.bulkPackageDimensionUpdate(file);
        BulkPackageDimensionUpdateRequest request = result.get(0);
        PackageDimensionUpdateResponse response = request.getPackageDimensionUpdateResponse();

        assertThat(response.getNewPackageDimension().getHeight()).isEqualTo(new BigDecimal(1));
        assertThat(response.getShipmentPackage().getType()).isEqualTo("test");
        assertThat(shipmentEntity.getShipmentPackage().getType()).isEqualTo("test");
        assertThat(response.getNewPackageDimension().getLength()).isEqualTo(new BigDecimal(1));
        assertThat(response.getNewPackageDimension().getWidth()).isEqualTo(new BigDecimal(1));
        assertThat(request.getPackageDimensionUpdateRequest().getPackageTypeId()).isEqualTo("8fc4a5bc-35d8-11ee-be56-0242ac120002");
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    @Test
    void shouldNotOverrideDimensionValuesForPackageCustomType() throws IOException {
        File file = testUtil.getFile("bulkupload/bulk-custom-package-update-template.csv");

        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("organizationId");
        when(qPortalApi.listPackageTypes(anyString())).thenReturn(createPackageTypes(true));

        PackageDimensionEntity packageDimension = new PackageDimensionEntity();
        packageDimension.setHeight(new BigDecimal(1));
        packageDimension.setLength(new BigDecimal(1));
        packageDimension.setWidth(new BigDecimal(1));
        packageDimension.setGrossWeight(new BigDecimal(1));
        packageDimension.setVolumeWeight(new BigDecimal(1));
        packageDimension.setChargeableWeight(new BigDecimal(1));

        PackageEntity shipmentPackage = new PackageEntity();
        shipmentPackage.setDimension(packageDimension);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("bf0f67a0-35d6-11ee-be56-0242ac120002");
        shipmentEntity.setShipmentPackage(shipmentPackage);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(anyString(), anyString())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        List<BulkPackageDimensionUpdateRequest> result = packageDimensionService.bulkPackageDimensionUpdate(file);
        BulkPackageDimensionUpdateRequest request = result.get(0);
        PackageDimensionUpdateResponse response = request.getPackageDimensionUpdateResponse();

        assertThat(response.getNewPackageDimension().getHeight()).isEqualTo(new BigDecimal(1));
        assertThat(response.getNewPackageDimension().getLength()).isEqualTo(new BigDecimal(1));
        assertThat(response.getNewPackageDimension().getWidth()).isEqualTo(new BigDecimal(1));
        assertThat(request.getPackageDimensionUpdateRequest().getPackageTypeId()).isNull();
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

    @Test
    void shouldReturnEmptyList_WhenInputListIsEmpty() {
        List<PackageDimensionUpdateRequest> emptyRequestList = Collections.emptyList();

        List<PackageDimensionUpdateResponse> actualResponses = packageDimensionService.updateShipmentsPackageDimension(emptyRequestList);

        assertThat(actualResponses).isEmpty();
    }

    @Test
    void shouldProcessDimsWeightUpdateRequestsSuccessfully() {

        PackageDimensionUpdateRequest updatePackageDimensionRequest = createUpdatePackageDimensionRequest();
        updatePackageDimensionRequest.setSource(TriggeredFrom.APIG);
        updatePackageDimensionRequest.setMeasurementUnit(MeasurementUnit.METRIC);
        List<PackageDimensionUpdateRequest> requestList = List.of(updatePackageDimensionRequest);

        PackageDimensionEntity packageDimensionEntity = new PackageDimensionEntity();
        BigDecimal oldValue = new BigDecimal("10.123");
        packageDimensionEntity.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimensionEntity.setGrossWeight(oldValue);
        packageDimensionEntity.setLength(oldValue);
        packageDimensionEntity.setHeight(oldValue);
        packageDimensionEntity.setWidth(oldValue);
        packageDimensionEntity.setVolumeWeight(oldValue);
        packageDimensionEntity.setChargeableWeight(oldValue);
        packageDimensionEntity.setCreateTime(Instant.now());
        packageDimensionEntity.setModifyTime(Instant.now());
        packageDimensionEntity.setCustom(false);

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId("shp-1");
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setDimension(packageDimensionEntity);
        packageEntity.setTypeRefId("test-type-ref-id");
        shipmentEntity.setShipmentPackage(packageEntity);

        when(shipmentRepository.findByShipmentTrackingIdAndOrganizationId(updatePackageDimensionRequest.getShipmentTrackingId(), updatePackageDimensionRequest.getOrganizationId())).thenReturn(Optional.of(shipmentEntity));
        when(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(anyString())).thenReturn(true);

        List<PackageDimensionUpdateResponse> actualResponses = packageDimensionService.updateShipmentsPackageDimension(requestList);

        assertThat(actualResponses).hasSize(1);
        assertThat(actualResponses.get(0).getShipmentPackage().getSource()).isEqualTo(TriggeredFrom.APIG);
        verify(messageApi, times(1)).sendUpdatedPackageDimensionsForShipment(any(Shipment.class));
        verify(qLoggerAPI, times(1)).publishPackageDimensionUpdateEvent(any(), any(), any());
        verify(packageDimensionPostProcessService, times(1)).createAndSendDimsAndWeightMilestoneForShipment(any(Shipment.class));
        verify(packageLogService, times(1)).upsertPackageLogForShipmentPackage(anyString(), any(Package.class));
    }

}
