package com.quincus.apigateway.service;

import com.quincus.apigateway.api.ApiGatewayWebhookClient;
import com.quincus.apigateway.api.dto.APIGAdditionalCharge;
import com.quincus.apigateway.api.dto.APIGCommodity;
import com.quincus.apigateway.api.dto.APIGCustomerInfo;
import com.quincus.apigateway.api.dto.APIGLocationInfo;
import com.quincus.apigateway.api.dto.APIGPackage;
import com.quincus.apigateway.api.dto.APIGPicInfo;
import com.quincus.apigateway.web.model.ApiGatewayCheckInRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderAdditionalChargesRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderProgressRequest;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.shipment.api.PartnerApi;
import com.quincus.shipment.api.ShipmentFetchApi;
import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.exception.UpdateOrderAdditionalChargesException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
@Slf4j
public class ApiGatewayService {

    private static final String ADDITIONAL_DATA_1_DELIMITER = "_";
    private static final String SUCCESSFUL = "successful";
    private static final String FAILED = "failed";
    private static final String UNIT_TYPE_MONEY = "money";
    private static final String UNIT_TYPE_TIME = "time";
    private static final String DEFAULT_TIME_UNIT = "min";
    private static final String ERR_UPDATE_ORDER_ADDITIONAL_CHARGES_FAILED = "Failed to send updateOrderAdditionalCharges webhook to API-G";
    private final PartnerApi partnerApi;
    private final ApiGatewayWebhookClient apiGatewayWebHookClient;
    private final AssignToVendorService assignToVendorService;
    private final ShipmentFetchApi shipmentFetchApi;
    private final QPortalApi qPortalApi;

    public ApiGatewayWebhookResponse sendUpdateOrderProgress(Shipment shipment, Milestone milestone) {
        ApiGatewayWebhookResponse response = null;
        ApiGatewayUpdateOrderProgressRequest request = createUpdateOrderProgressRequest(shipment, milestone);
        if (StringUtils.isNotEmpty(request.getOrderNo())) {
            response = apiGatewayWebHookClient.updateOrderProgress(milestone.getOrganizationId(), request);
        }
        return response;
    }

    public ApiGatewayWebhookResponse sendUpdateOrderProgress(Shipment shipment, PackageJourneySegment segment, Milestone milestone) {
        ApiGatewayWebhookResponse response = null;
        ApiGatewayUpdateOrderProgressRequest request = createUpdateOrderProgressRequest(shipment, segment, milestone);
        if (StringUtils.isNotEmpty(request.getOrderNo())) {
            response = apiGatewayWebHookClient.updateOrderProgress(milestone.getOrganizationId(), request);
        }
        return response;
    }

    public List<ApiGatewayWebhookResponse> sendUpdateOrderAdditionalCharges(Cost cost) {
        List<ApiGatewayUpdateOrderAdditionalChargesRequest> requests = createUpdateOrderAdditionalChargesRequests(cost);
        List<ApiGatewayWebhookResponse> responses = new ArrayList<>();
        requests.forEach(request -> {
            try {
                ApiGatewayWebhookResponse response = apiGatewayWebHookClient.updateOrderAdditionalCharges(cost.getOrganizationId(), request);
                response.setRequest(request);
                responses.add(response);
            } catch (Exception exception) {
                log.warn(ERR_UPDATE_ORDER_ADDITIONAL_CHARGES_FAILED);
                throw new UpdateOrderAdditionalChargesException(ERR_UPDATE_ORDER_ADDITIONAL_CHARGES_FAILED);
            }
        });
        return responses;
    }

    public ApiGatewayWebhookResponse sendAssignVendorDetails(Shipment shipment, Milestone milestone) {
        return assignToVendorService.sendAssignVendorDetails(shipment, milestone);
    }

    public ApiGatewayWebhookResponse sendAssignVendorDetails(Shipment shipment, PackageJourneySegment packageJourneySegment) {
        return assignToVendorService.sendAssignVendorDetails(shipment, packageJourneySegment);
    }

    public ApiGatewayWebhookResponse sendCheckInDetails(Shipment shipment, Milestone milestone) {
        PackageJourneySegment segment = findSegmentBySegmentId(shipment.getShipmentJourney().getPackageJourneySegments(), milestone.getSegmentId());
        return sendCheckInDetails(shipment, segment, milestone);
    }

    public ApiGatewayWebhookResponse sendCheckInDetails(Shipment shipment, PackageJourneySegment segment, Milestone milestone) {
        if (isNull(segment) || segment.getTransportType() == TransportType.AIR
                || (StringUtils.isEmpty(shipment.getExternalOrderId()) && StringUtils.isEmpty(shipment.getOrder().getOrderIdLabel())))
            return null;
        ApiGatewayCheckInRequest request = createCheckInRequest(shipment, milestone, segment);
        return apiGatewayWebHookClient.checkIn(milestone.getOrganizationId(), request);
    }

    private List<ApiGatewayUpdateOrderAdditionalChargesRequest> createUpdateOrderAdditionalChargesRequests(Cost cost) {
        List<ApiGatewayUpdateOrderAdditionalChargesRequest> requests = new ArrayList<>();
        cost.getShipments()
                .stream().filter(shipment -> StringUtils.isNotEmpty(shipment.getExternalOrderId())
                        || StringUtils.isNotEmpty(shipment.getOrderIdLabel()))
                .forEach(shipment ->
                        shipment.getSegments().forEach(segment -> {
                            ApiGatewayUpdateOrderAdditionalChargesRequest request = new ApiGatewayUpdateOrderAdditionalChargesRequest();
                            request.setOrderNo(getOrderNumber(shipment.getExternalOrderId(), shipment.getOrderIdLabel()));
                            request.setSegmentId(segment.getRefId());
                            request.setIsFirstSegment(segment.isFirstSegment());
                            request.setIsLastSegment(segment.isLastSegment());
                            request.setOrderStatus(shipment.getOrderStatus());
                            request.setAdditionalCharges(List.of(createAdditionalCharge(cost)));

                            requests.add(request);
                        })
                );
        return requests;
    }

    private APIGAdditionalCharge createAdditionalCharge(Cost cost) {
        APIGAdditionalCharge additionalCharge = new APIGAdditionalCharge();
        additionalCharge.setChargeCode(cost.getCostType().getName());
        additionalCharge.setChargeAmount(cost.getCostAmount().toString());
        additionalCharge.setTimestamp(cost.getIssuedDate().toString());
        if (cost.getCostType().getCategory() == CostCategory.TIME_BASED) {
            additionalCharge.setUnitType(UNIT_TYPE_TIME);
            additionalCharge.setTimeUnit(DEFAULT_TIME_UNIT);
        } else if (cost.getCostType().getCategory() == CostCategory.NON_TIME_BASED) {
            additionalCharge.setUnitType(UNIT_TYPE_MONEY);
            additionalCharge.setCurrency(cost.getCurrency().getCode());
        }
        return additionalCharge;
    }

    private ApiGatewayUpdateOrderProgressRequest createUpdateOrderProgressRequest(Shipment shipment, Milestone milestone) {
        PackageJourneySegment segment = findSegmentBySegmentId(shipment.getShipmentJourney().getPackageJourneySegments(), milestone.getSegmentId());
        return createUpdateOrderProgressRequest(shipment, segment, milestone);
    }

    public ApiGatewayUpdateOrderProgressRequest createUpdateOrderProgressRequest(Shipment shipment, PackageJourneySegment segment, Milestone milestone) {
        ApiGatewayUpdateOrderProgressRequest request = new ApiGatewayUpdateOrderProgressRequest();
        request.setOrderNo(extractOrderNumberFromShipment(shipment));
        if (nonNull(segment)) {
            request.setSegmentId(segment.getRefId());
        }
        setFirstAndLastSegment(segment, request);
        request.setMilestoneTime(milestone.getMilestoneTime().toString());
        request.setTimestamp(OffsetDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        request.setOrderTotalCodAmt(shipment.getShipmentPackage().getPricingInfo().getCod());
        request.setPicInfo(createAPIGPicInfo(milestone));
        request.setCustomerInfo(createAPIGCustomerInfo(milestone));
        request.setLocationInfo(createAPIGLocationInfo(milestone));
        request.setPackages(createAPIGPackages(milestone, segment, shipment));
        return request;
    }

    private void setFirstAndLastSegment(PackageJourneySegment segment, ApiGatewayUpdateOrderProgressRequest request) {
        if (nonNull(segment)) {
            if (segment.getType() == SegmentType.FIRST_MILE) {
                request.setIsFirstSegment(true);
                request.setIsLastSegment(false);
            } else if (segment.getType() == SegmentType.LAST_MILE) {
                request.setIsFirstSegment(Integer.parseInt(segment.getSequence()) == 0);
                request.setIsLastSegment(true);
            } else if (segment.getType() == SegmentType.MIDDLE_MILE) {
                request.setIsFirstSegment(false);
                request.setIsLastSegment(false);
            }
        }
    }

    private APIGPicInfo createAPIGPicInfo(Milestone milestone) {
        if (isNull(milestone)) return null;

        APIGPicInfo apigPicInfo = new APIGPicInfo();

        Partner partner = getPartner(milestone.getPartnerId(), milestone.getOrganizationId());
        if (nonNull(partner)) {
            apigPicInfo.setVendorCode(partner.getCode());
            apigPicInfo.setVendorName(partner.getName());
        } else {
            QPortalPartner qPortalPartner = getQPortalPartner(milestone.getPartnerId(), milestone.getOrganizationId());
            apigPicInfo.setVendorCode(qPortalPartner.getPartnerCode());
            apigPicInfo.setVendorName(qPortalPartner.getName());
        }

        QPortalUser qPortalUser = getQPortalDriver(milestone.getDriverId(), milestone.getOrganizationId());
        String driverName = StringUtils.defaultIfBlank(milestone.getDriverName(), null);
        apigPicInfo.setDriverName(Optional.ofNullable(driverName).orElse(qPortalUser.getFullName()));

        String driverPhoneNumber = StringUtils.defaultIfBlank(milestone.getDriverPhoneNumber(), null);
        apigPicInfo.setDriverPhoneNo(Optional.ofNullable(driverPhoneNumber).orElse(qPortalUser.getMobileNo()));

        String userName = StringUtils.defaultIfBlank(milestone.getUserName(), null);
        apigPicInfo.setUserName(Optional.ofNullable(userName).orElse(qPortalUser.getUsername()));

        return apigPicInfo;
    }

    private Partner getPartner(String partnerId, String organizationId) {
        if (StringUtils.isBlank(partnerId)) return null;
        return partnerApi.findByIdAndOrganizationId(partnerId, organizationId);
    }

    private QPortalUser getQPortalDriver(String driverId, String organizationId) {
        if (StringUtils.isBlank(driverId)) return new QPortalUser();
        return Optional.ofNullable(qPortalApi.getUser(organizationId, driverId)).orElse(new QPortalUser());
    }

    private QPortalPartner getQPortalPartner(String partnerId, String organizationId) {
        if (StringUtils.isBlank(partnerId)) return new QPortalPartner();
        return Optional.ofNullable(qPortalApi.getPartner(organizationId, partnerId)).orElse(new QPortalPartner());
    }

    private APIGCustomerInfo createAPIGCustomerInfo(Milestone milestone) {
        if (isNull(milestone)) return null;
        APIGCustomerInfo apigCustomerInfo = new APIGCustomerInfo();
        apigCustomerInfo.setSender(milestone.getSenderName());
        apigCustomerInfo.setConsignee(milestone.getReceiverName());
        apigCustomerInfo.setActualSender(milestone.getSenderName());
        apigCustomerInfo.setActualRecipient(milestone.getReceiverName());
        if (milestone.getAdditionalInfo() != null) {
            apigCustomerInfo.setCustomerComment(milestone.getAdditionalInfo().getRemarks());
        }
        apigCustomerInfo.setCustomerRating(new BigDecimal(0)); // TODO: update mapping
        return apigCustomerInfo;
    }

    private APIGLocationInfo createAPIGLocationInfo(Milestone milestone) {
        if (isNull(milestone)) return null;
        APIGLocationInfo apigLocationInfo = new APIGLocationInfo();
        String locationId = Optional.ofNullable(milestone.getToLocationId())
                .filter(StringUtils::isNotBlank)
                .orElse(milestone.getToCityId());
        apigLocationInfo.setLocationId(locationId);
        if (milestone.getMilestoneCoordinates() != null) {
            apigLocationInfo.setLatitude(milestone.getMilestoneCoordinates().getLat());
            apigLocationInfo.setLongitude(milestone.getMilestoneCoordinates().getLon());
        }
        return apigLocationInfo;
    }

    //TODO: Fix the query from native to HQL to filter soft deleted segment packages
    private List<APIGPackage> createAPIGPackages(Milestone milestone, PackageJourneySegment segment, Shipment referenceShipment) {
        List<Shipment> shipmentList = shipmentFetchApi.findAllShipmentsByOrderId(referenceShipment.getOrder().getId());
        Set<APIGPackage> apigPackages = new TreeSet<>();
        for (Shipment shipment : shipmentList) {
            milestone = getLatestMilestoneIfUnrelatedToShipment(milestone, shipment);
            Package shipmentPackage = shipment.getShipmentPackage();
            APIGPackage apigPackage = new APIGPackage();

            String additionalData1 = extractAdditionalData1(shipment.getShipmentReferenceId());
            apigPackage.setAdditionalData1(additionalData1);
            apigPackage.setPackageNo(generatePackageNoFromAdditionalData1(additionalData1));
            apigPackage.setDescription(shipment.getDescription());
            apigPackage.setActualCollectedAmt(shipmentPackage.getTotalValue());

            if (milestone.getAdditionalInfo() != null) {
                apigPackage.setRemark(milestone.getAdditionalInfo().getRemarks());
            }
            if (milestone.isFailureClassification()) {
                String reason = milestone.getFailedReason();
                apigPackage.setReasonCode(reason);
                apigPackage.setReason(reason);
            }
            apigPackage.setTimestamp(OffsetDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            apigPackage.setMilestone(milestone.getMilestoneCode().toString());
            apigPackage.setMilestoneClassification(milestone.isFailureClassification() ? FAILED : SUCCESSFUL);
            apigPackage.setPackagingType(shipmentPackage.getType());
            apigPackage.setPackageValue(shipmentPackage.getTotalValue());
            apigPackage.setUom(shipmentPackage.getDimension().getMeasurementUnit().toString().toLowerCase());
            apigPackage.setWeight(shipmentPackage.getDimension().getGrossWeight());
            apigPackage.setVolume(shipmentPackage.getDimension().getVolumeWeight());
            apigPackage.setLength(shipmentPackage.getDimension().getLength());
            apigPackage.setWidth(shipmentPackage.getDimension().getWidth());
            apigPackage.setHeight(shipmentPackage.getDimension().getHeight());
            apigPackage.setCurrencyType(shipmentPackage.getPricingInfo().getCurrency());
            apigPackage.setCodAmt(shipmentPackage.getPricingInfo().getCod());
            if (nonNull(segment)) {
                apigPackage.setTransportationType(segment.getTransportType().toString().toLowerCase());
                apigPackage.setWaybillNumber(segment.getMasterWaybill());
            }
            apigPackage.setCommodities(getCommoditiesFromPackage(shipmentPackage));

            apigPackages.add(apigPackage);
        }
        return apigPackages.stream().toList();
    }

    private Milestone getLatestMilestoneIfUnrelatedToShipment(Milestone milestone, Shipment shipment) {
        if (!StringUtils.equals(milestone.getShipmentId(), shipment.getId())) {
            List<Milestone> milestoneEvents = shipment.getMilestoneEvents();
            if (!CollectionUtils.isEmpty(milestoneEvents)) {
                milestone = milestoneEvents.get(0);
            }
        }
        return milestone;
    }

    private String extractAdditionalData1(List<String> referenceIds) {
        return Optional.ofNullable(referenceIds)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElse("");
    }

    private String generatePackageNoFromAdditionalData1(String additionalData1) {
        if (StringUtils.isBlank(additionalData1)) {
            return null; // or any default value
        }
        return additionalData1.split(ADDITIONAL_DATA_1_DELIMITER)[0];
    }

    private List<APIGCommodity> getCommoditiesFromPackage(Package shipmentPackage) {
        if (isNull(shipmentPackage) || shipmentPackage.getCommodities().isEmpty()) {
            return Collections.emptyList();
        }

        List<APIGCommodity> apigCommodities = new ArrayList<>();
        shipmentPackage.getCommodities().forEach(c -> {
            APIGCommodity apigCommodity = new APIGCommodity();
            apigCommodity.setQuantity(c.getQuantity());
            apigCommodity.setCode(c.getCode());
            apigCommodity.setName(c.getName());
            apigCommodity.setDesc(c.getDescription());
            apigCommodity.setShCode(c.getHsCode());
            apigCommodity.setPackagingType(c.getPackagingType());
            apigCommodity.setNote(c.getNote());
            apigCommodities.add(apigCommodity);
        });
        return apigCommodities;
    }

    private PackageJourneySegment findSegmentBySegmentId(List<PackageJourneySegment> segments, String segmentId) {
        if (CollectionUtils.isEmpty(segments) || StringUtils.isEmpty(segmentId)) return null;
        return segments.stream().filter(packageJourneySegment -> StringUtils.equals(segmentId, packageJourneySegment.getSegmentId()))
                .findAny().orElse(null);
    }

    private ApiGatewayCheckInRequest createCheckInRequest(Shipment shipment, Milestone milestone, PackageJourneySegment segment) {
        ApiGatewayCheckInRequest request = new ApiGatewayCheckInRequest();
        request.setOrderNo(extractOrderNumberFromShipment(shipment));
        setRequestJobType(request, milestone.getJobType(), milestone.getMilestoneCode());
        request.setSegmentId(new BigDecimal(segment.getRefId()));
        request.setLocationType("LOCATION_TYPE"); //TODO update mapping
        request.setDriverID(milestone.getDriverId());
        request.setDriverPhoneNumber(milestone.getDriverPhoneNumber());
        request.setDriverName(milestone.getDriverName());
        request.setCheckInTime(milestone.getMilestoneTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (milestone.getMilestoneCoordinates() != null) {
            request.setCheckInLatitude(milestone.getMilestoneCoordinates().getLat());
            request.setCheckInLongitude(milestone.getMilestoneCoordinates().getLon());
        }
        request.setTimestamp(OffsetDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return request;
    }

    private String extractOrderNumberFromShipment(Shipment shipment) {
        String internalOrderId = shipment.getOrder().getOrderIdLabel();
        return getOrderNumber(shipment.getExternalOrderId(), internalOrderId);
    }

    private String getOrderNumber(String externalOrderNumber, String internalOrderNumber) {
        return (StringUtils.isBlank(externalOrderNumber)) ? internalOrderNumber : externalOrderNumber;
    }

    private void setRequestJobType(ApiGatewayCheckInRequest request, String milestoneJobType,
                                   MilestoneCode refMilestoneCode) {
        if (milestoneJobType == null) {
            if (MilestoneCode.DSP_DRIVER_ARRIVED_FOR_PICKUP == refMilestoneCode) {
                request.setJobType("pickup");
            } else if (MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY == refMilestoneCode) {
                request.setJobType("delivery");
            }
        } else {
            request.setJobType(milestoneJobType);
        }
    }
}
