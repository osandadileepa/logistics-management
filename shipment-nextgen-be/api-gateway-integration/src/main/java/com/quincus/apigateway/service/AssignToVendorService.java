package com.quincus.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.apigateway.api.ApiGatewayWebhookClient;
import com.quincus.apigateway.web.model.ApiGatewayAssignVendorDetailRequest;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.shipment.api.ArchivedApi;
import com.quincus.shipment.api.domain.Archived;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AssignToVendorService {
    private static final String SUCCESS_RESULT = "Success";
    private final ArchivedApi archivedApi;
    private final ApiGatewayWebhookClient apiGatewayWebHookClient;
    private final ObjectMapper objectMapper;

    public ApiGatewayWebhookResponse sendAssignVendorDetails(Shipment shipment, Milestone milestone) {
        ApiGatewayWebhookResponse response = null;
        String orderLabel = getOrderLabelFromShipment(shipment);
        PackageJourneySegment packageJourneySegment = findSegmentBySegmentId(shipment.getShipmentJourney().getPackageJourneySegments(), milestone.getSegmentId());
        if (packageJourneySegment == null) {
            log.error("Unable to send assignVendorDetails webhook due to null segment from milestone segment id: `{}` shipment: `{}`", milestone.getSegmentId(), shipment.getId());
            return null;
        }
        if (StringUtils.isNoneEmpty(orderLabel)) {
            ApiGatewayAssignVendorDetailRequest request = buildAssignVendorDetailsRequest(shipment, packageJourneySegment, milestone);
            response = apiGatewayWebHookClient.assignVendorDetails(milestone.getOrganizationId(), request);
            if (StringUtils.equals(response.getMessage(), SUCCESS_RESULT)) {
                archivePackageJourneySegment(shipment, packageJourneySegment);
            }
        }
        return response;
    }

    public ApiGatewayWebhookResponse sendAssignVendorDetails(Shipment shipment, PackageJourneySegment packageJourneySegment) {
        ApiGatewayWebhookResponse response = null;
        String orderLabel = getOrderLabelFromShipment(shipment);
        if (StringUtils.isNoneEmpty(orderLabel)) {
            ApiGatewayAssignVendorDetailRequest request = buildAssignVendorDetailsRequest(shipment, packageJourneySegment, null);
            response = apiGatewayWebHookClient.assignVendorDetails(packageJourneySegment.getOrganizationId(), request);
            if (StringUtils.equals(response.getMessage(), SUCCESS_RESULT)) {
                archivePackageJourneySegment(shipment, packageJourneySegment);
            }
        }
        return response;
    }

    private ApiGatewayAssignVendorDetailRequest buildAssignVendorDetailsRequest(Shipment shipment,
                                                                                PackageJourneySegment packageJourneySegment,
                                                                                Milestone milestone) {
        ApiGatewayAssignVendorDetailRequest request = new ApiGatewayAssignVendorDetailRequest();
        request.setOrderNo(getOrderLabelFromShipment(shipment));
        request.setAssignedAt(OffsetDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        Optional.ofNullable(packageJourneySegment.getPartner()).ifPresent(partner -> request.setVendorId(partner.getId()));
        request.setSegmentId(packageJourneySegment.getRefId());
        request.setVendorReassigned(isVendorReassigned(shipment, packageJourneySegment));
        request.setDriverPhoneCode(extractDriverPhoneCode(packageJourneySegment, milestone));
        request.setDriverPhoneNumber(extractDriverPhoneNumber(packageJourneySegment, milestone));
        return request;
    }

    public boolean isVendorReassigned(Shipment shipment, PackageJourneySegment currentPackageJourneySegment) {
        String referenceId = constructReferenceId(shipment, currentPackageJourneySegment);
        log.info("Generated reference id :`{}` for shipment id: `{}` and package journey segment: `{}`",
                referenceId, shipment.getId(), currentPackageJourneySegment.getSegmentId());
        Optional<Archived> latestArchivedPackageJourneySegment = archivedApi.findLatestByReferenceId(referenceId);
        if (latestArchivedPackageJourneySegment.isEmpty()) {
            return false;
        }
        try {
            PackageJourneySegment previousPackageJourneySegment =
                    objectMapper.readValue(latestArchivedPackageJourneySegment.get().getData(), PackageJourneySegment.class);

            String currentPartnerId = Optional.ofNullable(currentPackageJourneySegment.getPartner())
                    .map(Partner::getId)
                    .orElse(null);

            String previousPartnerId = Optional.ofNullable(previousPackageJourneySegment.getPartner())
                    .map(Partner::getId)
                    .orElse(null);

            log.info("Current Partner Id :`{}` and Previous Partner Id :`{}` for shipment id: `{}` and package journey segment: `{}`",
                    currentPartnerId, previousPartnerId, shipment.getId(), currentPackageJourneySegment.getSegmentId());

            return !StringUtils.equals(currentPartnerId, previousPartnerId);

        } catch (Exception e) {
            log.error("Failed to deserialize archived PackageJourneySegment data for segment id `{}`.", currentPackageJourneySegment.getSegmentId(), e);
            return false;
        }
    }

    private void archivePackageJourneySegment(Shipment shipment, PackageJourneySegment packageJourneySegment) {
        try {
            Archived archivedPackageJourneySegment = new Archived();
            archivedPackageJourneySegment.setReferenceId(constructReferenceId(shipment, packageJourneySegment));
            archivedPackageJourneySegment.setOrganizationId(packageJourneySegment.getOrganizationId());
            archivedPackageJourneySegment.setClassName(packageJourneySegment.getClass().getName());
            archivedPackageJourneySegment.setData(objectMapper.writeValueAsString(packageJourneySegment));
            archivedApi.save(archivedPackageJourneySegment);
        } catch (Exception e) {
            log.error("Error occurred while converting to JSON String or saving Package JourneySegment with id `{}`", packageJourneySegment.getSegmentId(), e);
        }
    }

    private String extractDriverPhoneCode(PackageJourneySegment packageJourneySegment, Milestone milestone) {
        return Optional.ofNullable(milestone)
                .map(Milestone::getDriverPhoneCode)
                .filter(phoneCode -> !StringUtils.isBlank(phoneCode))
                .orElseGet(() -> Optional.ofNullable(packageJourneySegment.getDriver())
                        .map(Driver::getPhoneCode)
                        .orElse(null));
    }

    private String extractDriverPhoneNumber(PackageJourneySegment packageJourneySegment, Milestone milestone) {
        return Optional.ofNullable(milestone)
                .map(Milestone::getDriverPhoneNumber)
                .filter(phoneNumber -> !StringUtils.isBlank(phoneNumber))
                .orElseGet(() -> Optional.ofNullable(packageJourneySegment.getDriver())
                        .map(Driver::getPhoneNumber)
                        .orElse(null));
    }

    private String constructReferenceId(Shipment shipment, PackageJourneySegment segment) {
        return shipment.getOrder().getOrderIdLabel() + "_" + segment.getStartFacility().getId() + "_" + segment.getEndFacility().getId();
    }

    private String getOrderLabelFromShipment(Shipment shipment) {
        return getOrderLabel(shipment.getExternalOrderId(), shipment.getOrder().getOrderIdLabel());
    }

    private String getOrderLabel(String externalOrderNumber, String internalOrderNumber) {
        return StringUtils.defaultIfBlank(externalOrderNumber, internalOrderNumber);
    }

    private PackageJourneySegment findSegmentBySegmentId(List<PackageJourneySegment> segments, String segmentId) {
        return segments.stream()
                .filter(segment -> StringUtils.equals(segmentId, segment.getSegmentId()))
                .findFirst().orElse(null);
    }
}
