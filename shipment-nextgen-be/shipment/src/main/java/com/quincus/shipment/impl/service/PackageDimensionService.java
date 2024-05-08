package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.PackageDimensionUpdateImportHeader;
import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionErrorRecord;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import com.quincus.shipment.api.exception.PackageDimensionException;
import com.quincus.shipment.api.exception.SegmentLocationNotAllowedException;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.mapper.LocalDateMapper;
import com.quincus.shipment.impl.mapper.PackageMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.PackageDimensionRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.resolver.FacilityLocationPermissionChecker;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.PackageDimensionValidator;
import com.quincus.web.common.exception.model.OperationNotAllowedException;
import liquibase.repackaged.org.apache.commons.collections4.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PackageDimensionService {
    static final String ERR_READ_NOT_ALLOWED = "Read operation for package dimension not allowed for Shipment %s.";
    static final String ERR_UPDATE_NOT_ALLOWED = "Update operation for package dimension not allowed for Shipment %s.";
    private static final String ERR_SHIPMENT_NOT_FOUND_BY_SHIPMENT_TRACKING_ID_AND_ORGANIZATION_ID = "Shipment not found by shipment tracking id: `%s` and organization id: `%s`";
    private static final String QLOGGER_SOURCE_SINGLE_UPDATE = "PackageDimensionService#updateShipmentPackageDimension";
    private static final String QLOGGER_SOURCE_BULK_UPDATE = "PackageDimensionService#bulkUpdateImport";
    private static final String BULK_PACKAGE_DIMENSION_DELIMETER = ",";
    private static final int PACKAGE_DIMENSION_SCALE = 3;
    private static final String ERR_MSG_MUST_BE_NUMBER = "%s must be a number.";
    private static final String ERR_MSG_CANNOT_BE_EMPTY = "%s cannot be empty.";
    private static final String ERR_MSG_INVALID = "%s is invalid.";
    private static final String CUSTOM_TYPE_PACKAGE_NAME = "Custom Type";
    private static final String UPDATE_ON_CANCELLED_SHIPMENT_IS_NOT_ALLOWED = "Package Dimension Update on Cancelled shipment is not allowed!";
    private final ShipmentRepository shipmentRepository;
    private final PackageDimensionRepository packageDimensionRepository;
    private final PackageDimensionValidator packageDimensionValidator;
    private final QLoggerAPI qLoggerAPI;
    private final MessageApi messageApi;
    private final QPortalApi qPortalApi;
    private final UserDetailsProvider userDetailsProvider;
    private final FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    private final UpdateShipmentHelper updateShipmentHelper;
    private final PackageDimensionPostProcessService packageDimensionPostProcessService;
    private final ObjectMapper objectMapper;
    private final PackageLogService packageLogService;

    @Transactional
    public PackageDimensionUpdateResponse updateShipmentPackageDimension(PackageDimensionUpdateRequest updatePackageDimensionRequest) {
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        Optional<ShipmentEntity> optionalShipmentEntity = shipmentRepository.findByShipmentTrackingIdAndOrganizationId(
                updatePackageDimensionRequest.getShipmentTrackingId(), organizationId);

        if (optionalShipmentEntity.isEmpty()) {
            throw new PackageDimensionException(String.format(ERR_SHIPMENT_NOT_FOUND_BY_SHIPMENT_TRACKING_ID_AND_ORGANIZATION_ID,
                    updatePackageDimensionRequest.getShipmentTrackingId(), organizationId));
        }
        ShipmentEntity shipmentEntity = optionalShipmentEntity.get();
        if (updateShipmentHelper.isShipmentCancelled(shipmentEntity)) {
            throw new OperationNotAllowedException(UPDATE_ON_CANCELLED_SHIPMENT_IS_NOT_ALLOWED);
        }

        if (!facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(shipmentEntity.getId())) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_UPDATE_NOT_ALLOWED, shipmentEntity.getId()),
                    ShipmentErrorCode.PACKAGE_DIMENSION_ERROR);
        }

        PackageDimensionEntity packageDimensionEntity = shipmentEntity.getShipmentPackage().getDimension();
        PackageDimension previousPackageDimension = toPackageDimension(packageDimensionEntity);
        PackageDimension newPackageDimension = updatePackageDimensionThenLogAndSendUpdateToOtherModules(shipmentEntity, updatePackageDimensionRequest, packageDimensionEntity, previousPackageDimension);
        packageLogService.upsertPackageLogForShipmentPackage(shipmentEntity.getId(), PackageMapper.toDomain(shipmentEntity.getShipmentPackage()));
        return createUpdatePackageDimensionsResponse(updatePackageDimensionRequest, previousPackageDimension, newPackageDimension, PackageMapper.toDomain(shipmentEntity.getShipmentPackage()));
    }

    private PackageDimension updatePackageDimensionThenLogAndSendUpdateToOtherModules(final ShipmentEntity shipmentEntity,
                                                                                      final PackageDimensionUpdateRequest updatePackageDimensionRequest,
                                                                                      final PackageDimensionEntity packageDimensionEntity,
                                                                                      final PackageDimension previousPackageDimension) {
        updatePackageDimensionEntityFromUpdatePackageDimensionRequest(updatePackageDimensionRequest, packageDimensionEntity);
        updateShipmentPackageTypeAndSource(shipmentEntity, updatePackageDimensionRequest);
        Shipment shipment = toShipment(shipmentEntity, updatePackageDimensionRequest, packageDimensionEntity);
        PackageDimension newPackageDimension = shipment.getShipmentPackage().getDimension();
        logAndSendUpdatesToOtherModules(previousPackageDimension, shipment);
        packageDimensionPostProcessService.createAndSendDimsAndWeightMilestoneForShipment(ShipmentMapper.mapEntityToDomain(shipmentEntity, objectMapper));
        return newPackageDimension;
    }

    private void updateShipmentPackageTypeAndSource(ShipmentEntity shipmentEntity, PackageDimensionUpdateRequest packageDimensionUpdateRequest) {
        PackageEntity packageEntity = shipmentEntity.getShipmentPackage();
        String packageTypeName = Optional.ofNullable(packageDimensionUpdateRequest.getPackageTypeName())
                .filter(StringUtils::isNotBlank).orElse(CUSTOM_TYPE_PACKAGE_NAME);
        packageEntity.setType(packageTypeName);
        if (StringUtils.isNotBlank(packageDimensionUpdateRequest.getPackageTypeId())) {
            packageEntity.setTypeRefId(packageDimensionUpdateRequest.getPackageTypeId());
        }
        packageEntity.setSource(packageDimensionUpdateRequest.getSource());
    }

    private PackageDimensionUpdateResponse createUpdatePackageDimensionsResponse(final PackageDimensionUpdateRequest updatePackageDimensionRequest,
                                                                                 final PackageDimension previousPackageDimension,
                                                                                 final PackageDimension newPackageDimension,
                                                                                 final Package shipmentPackage) {
        return new PackageDimensionUpdateResponse(
                userDetailsProvider.getCurrentOrganizationId(),
                updatePackageDimensionRequest.getUserId(),
                updatePackageDimensionRequest.getUserLocationId(),
                newPackageDimension.getModifyDate(),
                previousPackageDimension,
                newPackageDimension,
                shipmentPackage);
    }

    private void updatePackageDimensionEntityFromUpdatePackageDimensionRequest(PackageDimensionUpdateRequest updatePackageDimensionRequest, PackageDimensionEntity packageDimensionEntity) {
        if (updatePackageDimensionRequest.getMeasurementUnit() != null) {
            packageDimensionEntity.setMeasurementUnit(updatePackageDimensionRequest.getMeasurementUnit());
        }

        packageDimensionEntity.setLength(roundOffToPackageDimensionScale(updatePackageDimensionRequest.getLength()));
        packageDimensionEntity.setWidth(roundOffToPackageDimensionScale(updatePackageDimensionRequest.getWidth()));
        if (nonNull(updatePackageDimensionRequest.getHeight())) {
            packageDimensionEntity.setHeight(roundOffToPackageDimensionScale(updatePackageDimensionRequest.getHeight()));
        }
        packageDimensionEntity.setGrossWeight(roundOffToPackageDimensionScale(updatePackageDimensionRequest.getGrossWeight()));

        Optional<QPortalPackageType> packageTypes = getPackageTypes().stream()
                                                    .filter(p -> p.getName().equalsIgnoreCase(updatePackageDimensionRequest.getPackageTypeName()))
                                                    .findFirst();

        packageTypes.ifPresentOrElse(type -> packageDimensionEntity.setCustom(false), () -> packageDimensionEntity.setCustom(true));

        packageDimensionRepository.save(packageDimensionEntity);
    }

    public Shipment findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(String shipmentTrackingId) {
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        Shipment shipment = shipmentRepository.findByShipmentTrackingIdAndOrganizationId(shipmentTrackingId, organizationId)
                .map(this::toShipmentPackageDimension)
                .orElseThrow(() -> new ShipmentNotFoundException(String.format(ERR_SHIPMENT_NOT_FOUND_BY_SHIPMENT_TRACKING_ID_AND_ORGANIZATION_ID, shipmentTrackingId, organizationId)));

        if (!facilityLocationPermissionChecker.isShipmentTrackingIdAnySegmentLocationCovered(shipmentTrackingId)) {
            throw new SegmentLocationNotAllowedException(String.format(ERR_READ_NOT_ALLOWED, shipmentTrackingId),
                    ShipmentErrorCode.PACKAGE_DIMENSION_ERROR);
        }
        return shipment;
    }

    private Shipment toShipment(final ShipmentEntity shipmentEntity, PackageDimensionUpdateRequest updatePackageDimensionRequest, PackageDimensionEntity packageDimensionEntity) {
        Shipment shipment = new Shipment();
        shipment.setUserId(updatePackageDimensionRequest.getUserId());
        shipment.setUserLocationId(updatePackageDimensionRequest.getUserLocationId());
        shipment.setOrganization(new Organization(userDetailsProvider.getCurrentOrganizationId()));
        Package shipmentPackage = new Package();
        PackageEntity shipmentPackageEntity = shipmentEntity.getShipmentPackage();
        shipmentPackage.setId(shipmentPackageEntity.getId());
        shipmentPackage.setRefId(shipmentPackageEntity.getRefId());
        shipmentPackage.setDimension(toPackageDimension(packageDimensionEntity));
        shipmentPackage.setTypeRefId(updatePackageDimensionRequest.getPackageTypeId());
        shipmentPackage.setType(updatePackageDimensionRequest.getPackageTypeName());
        shipmentPackage.setSource(shipmentPackageEntity.getSource());
        shipment.setShipmentPackage(shipmentPackage);
        return shipment;
    }

    private Shipment toShipmentPackageDimension(ShipmentEntity shipmentEntity) {
        Shipment shipment = new Shipment();
        shipment.setId(shipmentEntity.getId());
        shipment.setShipmentTrackingId(shipmentEntity.getShipmentTrackingId());
        Package packageDomain = new Package();
        if (shipmentEntity.getShipmentPackage().getTotalValue() != null) {
            packageDomain.setTotalValue(roundOffToPackageDimensionScale(shipmentEntity.getShipmentPackage().getTotalValue()));
        }
        packageDomain.setId(shipmentEntity.getShipmentPackage().getId());
        packageDomain.setRefId(shipmentEntity.getShipmentPackage().getRefId());
        packageDomain.setType(shipmentEntity.getShipmentPackage().getType());
        packageDomain.setTypeRefId(shipmentEntity.getShipmentPackage().getTypeRefId());
        packageDomain.setCurrency(shipmentEntity.getShipmentPackage().getCurrency());
        packageDomain.setValue(shipmentEntity.getShipmentPackage().getValue());
        packageDomain.setReadyTime(shipmentEntity.getShipmentPackage().getReadyTime());
        packageDomain.setDimension(toPackageDimension(shipmentEntity.getShipmentPackage().getDimension()));
        packageDomain.setCode(shipmentEntity.getShipmentPackage().getCode());
        packageDomain.setTotalItemsCount(shipmentEntity.getShipmentPackage().getTotalItemsCount());
        shipment.setShipmentPackage(packageDomain);
        return shipment;
    }

    private PackageDimension toPackageDimension(PackageDimensionEntity packageDimensionEntity) {
        PackageDimension packageDimension = new PackageDimension();
        packageDimension.setMeasurementUnit(packageDimensionEntity.getMeasurementUnit());
        packageDimension.setLength(roundOffToPackageDimensionScale(packageDimensionEntity.getLength()));
        packageDimension.setWidth(roundOffToPackageDimensionScale(packageDimensionEntity.getWidth()));
        packageDimension.setHeight(roundOffToPackageDimensionScale(packageDimensionEntity.getHeight()));
        packageDimension.setGrossWeight(roundOffToPackageDimensionScale(packageDimensionEntity.getGrossWeight()));
        packageDimension.setVolumeWeight(roundOffToPackageDimensionScale(packageDimensionEntity.getVolumeWeight()));
        packageDimension.setChargeableWeight(roundOffToPackageDimensionScale(packageDimensionEntity.getChargeableWeight()));
        packageDimension.setCustom(packageDimensionEntity.isCustom());
        packageDimension.setCreateDate(LocalDateMapper.toLocalDateTime(packageDimensionEntity.getCreateTime()));
        packageDimension.setModifyDate(LocalDateMapper.toLocalDateTime(packageDimensionEntity.getModifyTime()));
        return packageDimension;
    }

    private BigDecimal roundOffToPackageDimensionScale(BigDecimal bigDecimalValue) {
        if (bigDecimalValue.stripTrailingZeros().scale() <= 0) {
            return bigDecimalValue;
        }
        return bigDecimalValue.setScale(PACKAGE_DIMENSION_SCALE, RoundingMode.HALF_DOWN);
    }

    @Async("externalApiExecutor")
    public void logAndSendUpdatesToOtherModules(final PackageDimension previousPackageDimension, final Shipment shipment) {
        qLoggerAPI.publishPackageDimensionUpdateEvent(QLOGGER_SOURCE_SINGLE_UPDATE, previousPackageDimension, shipment);
        messageApi.sendUpdatedPackageDimensionsForShipment(shipment);
    }

    public void getPackageUpdateFileTemplate(Writer writer) throws IOException {
        String[] headers = Arrays.stream(PackageDimensionUpdateImportHeader.values())
                .map(PackageDimensionUpdateImportHeader::toString).toArray(String[]::new);
        CSVFormat csvFormat = CSVFormat.Builder.create().setHeader(headers).build();
        CSVPrinter printer = new CSVPrinter(writer, csvFormat);
        printer.printRecord(
                "sample-shipment-id",
                "sample-packaging-type",
                "METRIC/IMPERIAL",
                "1.00",
                "1.00",
                "1.00",
                "1.00");
    }

    @Transactional
    public List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdate(File file) {
        packageDimensionValidator.validatePackageDimensionUpdateFile(file);
        List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdates = convertFileToPackageDimensionUpdateRequests(file);
        for (BulkPackageDimensionUpdateRequest bulkUpdate : bulkPackageDimensionUpdates) {
            try {
                if (!bulkUpdate.isError()) {
                    bulkUpdate.setPackageDimensionUpdateResponse(updateShipmentPackageDimension(bulkUpdate.getPackageDimensionUpdateRequest()));
                }
            } catch (Exception e) {
                bulkUpdate.setErrorMessages(e.getMessage());
            }
        }
        sendBulkPackageDimensionUpdatesToQLogger(bulkPackageDimensionUpdates);
        return bulkPackageDimensionUpdates;
    }

    private List<QPortalPackageType> getPackageTypes() {
        return qPortalApi.listPackageTypes(userDetailsProvider.getCurrentOrganizationId());
    }

    private QPortalPackageType getPackageTypeFromQPortal(List<QPortalPackageType> packageTypes, String packageName) {
        return packageTypes.stream().filter(pkg -> StringUtils.equalsIgnoreCase(pkg.getName(), packageName)).findAny().orElse(null);
    }

    private void sendBulkPackageDimensionUpdatesToQLogger(List<BulkPackageDimensionUpdateRequest> bulkPackageDimensionUpdates) {
        if (CollectionUtils.isEmpty(bulkPackageDimensionUpdates)) {
            return;
        }
        List<BulkPackageDimensionUpdateRequest> successfulUpdates = bulkPackageDimensionUpdates.stream().filter(res -> !res.isError()).toList();
        if (CollectionUtils.isEmpty(successfulUpdates)) {
            return;
        }
        List<Package> shipmentPackages = new ArrayList<>();
        List<PackageDimension> oldDimensions = new ArrayList<>();
        List<PackageDimension> newDimensions = new ArrayList<>();
        successfulUpdates.forEach(e -> {
            shipmentPackages.add(e.getPackageDimensionUpdateResponse().getShipmentPackage());
            oldDimensions.add(e.getPackageDimensionUpdateResponse().getPreviousPackageDimension());
            newDimensions.add(e.getPackageDimensionUpdateResponse().getNewPackageDimension());
        });
        qLoggerAPI.publishBulkPackageDimensionUpdateEvent(QLOGGER_SOURCE_BULK_UPDATE, shipmentPackages, oldDimensions, newDimensions);
    }

    private List<BulkPackageDimensionUpdateRequest> convertFileToPackageDimensionUpdateRequests(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            List<BulkPackageDimensionUpdateRequest> bulkRequests = new ArrayList<>();
            List<QPortalPackageType> packageTypes = getPackageTypes();
            while ((line = reader.readLine()) != null) {
                if (!line.contains(PackageDimensionUpdateImportHeader.SHIPMENT_ID.toString())) {
                    String[] cell = line.split(BULK_PACKAGE_DIMENSION_DELIMETER, -1);
                    bulkRequests.add(createBulkPackageDimensionUpdateRequest(cell, packageTypes));
                }
            }
            return bulkRequests;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PackageDimensionException(PackageDimensionValidator.ERR_MSG_GENERIC_FILE_UPLOAD);
        }
    }

    private BulkPackageDimensionUpdateRequest createBulkPackageDimensionUpdateRequest(String[] cell, List<QPortalPackageType> packageTypes) {
        PackageDimensionUpdateRequest request = null;
        BulkPackageDimensionUpdateRequest bulkPackageDimensionUpdate = new BulkPackageDimensionUpdateRequest(cell);
        try {
            request = convertCellsToPackageDimensionUpdateRequest(cell, bulkPackageDimensionUpdate);
            if (StringUtils.isNotEmpty(cell[1])) {
                QPortalPackageType packageDTO = getPackageTypeFromQPortal(packageTypes, cell[1]);
                if (nonNull(packageDTO)) {
                    overridePackageDimensionsFromPackageType(packageDTO, request);
                } else {
                    bulkPackageDimensionUpdate.setPackageTypeError(String.format(ERR_MSG_INVALID, PackageDimensionUpdateImportHeader.PACKAGING_TYPE));
                }
            }
        } catch (Exception e) {
            bulkPackageDimensionUpdate.setErrorMessages(e.getMessage());
        }
        bulkPackageDimensionUpdate.setPackageDimensionUpdateRequest(request);
        return bulkPackageDimensionUpdate;
    }

    private void overridePackageDimensionsFromPackageType(QPortalPackageType packageDTO, PackageDimensionUpdateRequest request) {
        request.setHeight(packageDTO.getDimension().getHeight());
        request.setLength(packageDTO.getDimension().getLength());
        request.setWidth(packageDTO.getDimension().getWidth());
        request.setPackageTypeId(packageDTO.getId());
        request.setPackageTypeName(packageDTO.getName());
    }

    private PackageDimensionUpdateRequest convertCellsToPackageDimensionUpdateRequest(String[] cells, BulkPackageDimensionUpdateRequest bulkPackageDimensionUpdate) {
        PackageDimensionUpdateRequest request = new PackageDimensionUpdateRequest();
        request.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        if (StringUtils.isEmpty(cells[0])) {
            bulkPackageDimensionUpdate.setShipmentError(String.format(ERR_MSG_CANNOT_BE_EMPTY, PackageDimensionUpdateImportHeader.SHIPMENT_ID));
        } else {
            Optional<ShipmentEntity> shipment = shipmentRepository.findByShipmentTrackingIdAndOrganizationId(cells[0], request.getOrganizationId());
            if (shipment.isEmpty()) {
                bulkPackageDimensionUpdate.setShipmentError(String.format(ERR_MSG_INVALID, PackageDimensionUpdateImportHeader.SHIPMENT_ID));
            } else {
                request.setShipmentTrackingId(cells[0]);
            }
        }

        if (StringUtils.isEmpty(cells[2])) {
            bulkPackageDimensionUpdate.setUnitError(String.format(ERR_MSG_CANNOT_BE_EMPTY, PackageDimensionUpdateImportHeader.UNIT));
        } else {
            try {
                request.setMeasurementUnit(MeasurementUnit.fromValue(cells[2]));
            } catch (InvalidEnumValueException e) {
                bulkPackageDimensionUpdate.setUnitError(String.format(ERR_MSG_INVALID, PackageDimensionUpdateImportHeader.UNIT));
            }
        }
        setNumberColumns(cells, bulkPackageDimensionUpdate, request);
        return request;
    }

    private void setNumberColumns(String[] cells, BulkPackageDimensionUpdateRequest bulkPackageDimensionUpdate, PackageDimensionUpdateRequest request) {
        if (StringUtils.isEmpty(cells[3])) {
            request.setHeight(null);
        } else {
            String heightError = isNumberColumnValid(cells[3], false);
            if (isNull(heightError)) {
                request.setHeight(convertStringToBigDecimal(cells[3]));
            } else {
                bulkPackageDimensionUpdate.setHeightError(String.format(heightError, PackageDimensionUpdateImportHeader.HEIGHT));
            }
        }

        String widthError = isNumberColumnValid(cells[4], true);
        if (isNull(widthError)) {
            request.setWidth(convertStringToBigDecimal(cells[4]));
        } else {
            bulkPackageDimensionUpdate.setWidthError(String.format(widthError, PackageDimensionUpdateImportHeader.WIDTH));
        }

        String lengthError = isNumberColumnValid(cells[5], true);
        if (isNull(lengthError)) {
            request.setLength(convertStringToBigDecimal(cells[5]));
        } else {
            bulkPackageDimensionUpdate.setLengthError(String.format(lengthError, PackageDimensionUpdateImportHeader.LENGTH));
        }

        String weightError = isNumberColumnValid(cells[6], true);
        if (isNull(weightError)) {
            request.setGrossWeight(convertStringToBigDecimal(cells[6]));
        } else {
            bulkPackageDimensionUpdate.setWeightError(String.format(weightError, PackageDimensionUpdateImportHeader.WEIGHT));
        }
    }

    private String isNumberColumnValid(String cell, boolean isMandatory) {
        if (isMandatory && StringUtils.isEmpty(cell)) {
            return ERR_MSG_CANNOT_BE_EMPTY;
        } else {
            try {
                convertStringToBigDecimal(cell);
                return null;
            } catch (NumberFormatException e) {
                return ERR_MSG_MUST_BE_NUMBER;
            }
        }
    }

    private BigDecimal convertStringToBigDecimal(String num) {
        if (!NumberUtils.isCreatable(num)) {
            throw new NumberFormatException(ERR_MSG_MUST_BE_NUMBER);
        }
        return new BigDecimal(num);
    }

    public List<PackageDimensionErrorRecord> getAllErrorsFromBulkPackageDimensionUpdate(List<BulkPackageDimensionUpdateRequest> bulkResult) {
        List<PackageDimensionErrorRecord> records = new ArrayList<>();
        if (nonNull(bulkResult) && bulkResult.stream().anyMatch(BulkPackageDimensionUpdateRequest::isError)) {
            for (BulkPackageDimensionUpdateRequest result : bulkResult) {
                if (result.isError()) {
                    PackageDimensionErrorRecord packageDimensionErrorRecord = new PackageDimensionErrorRecord(
                            result.getShipmentTrackingIdCell(),
                            result.getPackageTypeCell(),
                            result.getUnitCell(),
                            result.getHeightCell(),
                            result.getWidthCell(),
                            result.getLengthCell(),
                            result.getWeightCell(),
                            result.getErrorMessages());
                    records.add(packageDimensionErrorRecord);
                }
            }
        }
        return records;
    }

    public boolean isPackageDimensionUpdated(PackageDimensionEntity oldDimension, PackageDimension newDimension) {
        if (isNull(oldDimension) || isNull(newDimension)) {
            return false;
        }
        return Stream.of(oldDimension.getGrossWeight().compareTo(newDimension.getGrossWeight()),
                oldDimension.getLength().compareTo(newDimension.getLength()),
                oldDimension.getWidth().compareTo(newDimension.getWidth()),
                oldDimension.getHeight().compareTo(newDimension.getHeight())
        ).anyMatch(e -> e != 0);
    }

    @Transactional
    public List<PackageDimensionUpdateResponse> updateShipmentsPackageDimension(
            final List<PackageDimensionUpdateRequest> shipmentPackageDimensionUpdateRequests) {
        if (CollectionUtils.isEmpty(shipmentPackageDimensionUpdateRequests)) {
            return Collections.emptyList();
        }

        return shipmentPackageDimensionUpdateRequests.stream()
                .map(this::updateShipmentPackageDimension)
                .toList();
    }
}
