package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.api.filter.ShipmentExportFilterResult;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.mapper.AddressMapper;
import com.quincus.shipment.impl.mapper.CustomerMapper;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.mapper.ServiceTypeMapper;
import com.quincus.shipment.impl.mapper.ShipmentCriteriaMapper;
import com.quincus.shipment.impl.mapper.ShipmentJourneyMapper;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionExport;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static com.quincus.shipment.api.constant.SegmentType.FIRST_MILE;
import static com.quincus.shipment.api.constant.SegmentType.LAST_MILE;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShipmentListingCsvExportService {
    static final String[] CSV_HEADERS = {
            "Shipment Tracking ID", "Origin Country", "Origin State", "Origin City", "Destination Country", "Destination State", "Destination City",
            "Sequence No.", "Transport Category", "Partner Name", "Vehicle Info", "Flight Number", "Airline", "Airline Code", "Master Waybill",
            "Pick Up Facility", "Drop Off Facility", "Pick Up Instruction", "Drop Off Instruction", "Segment Instruction", "Duration", "Duration Unit", "Pick Up Time", "Drop Off Time", "Lockout Time",
            "Departure Time", "Arrival Time", "Recovery Time", "Calculated Mileage", "Calculated Mileage Unit", "Latest Milestone", "Service Type", "Customer", "Status"
    };
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create().setHeader(CSV_HEADERS).build();
    private final ShipmentProjectionExport shipmentProjectionExport;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneService milestoneService;
    private final ObjectMapper objectMapper;
    private final ShipmentCriteriaMapper shipmentCriteriaMapper;
    private final LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;
    private final UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;
    private final UserDetailsProvider userDetailsProvider;
    private final List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;

    public void writeShipmentToCsv(List<Shipment> shipments, Writer writer) throws IOException {
        CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT);
        for (Shipment shipment : shipments) {
            String pickupInstruction = ShipmentUtil.getPickupInstruction(shipment).orElse(StringUtils.EMPTY);
            String deliveryInstruction = ShipmentUtil.getDeliveryInstruction(shipment).orElse(StringUtils.EMPTY);
            String customerName = getCustomerName(shipment);
            String milestoneName = getMilestoneName(shipment);
            for (PackageJourneySegment segment : shipment.getShipmentJourney().getPackageJourneySegments()) {
                String partnerName = getPartnerName(segment);
                String transportType = getTransportType(segment);
                printer.printRecord(
                        shipment.getShipmentTrackingId(),
                        shipment.getOrigin().getCountryName(),
                        shipment.getOrigin().getStateName(),
                        shipment.getOrigin().getCityName(),
                        shipment.getDestination().getCountryName(),
                        shipment.getDestination().getStateName(),
                        shipment.getDestination().getCityName(),
                        segment.getRefId(),
                        transportType,
                        partnerName,
                        segment.getVehicleInfo(),
                        segment.getFlightNumber(),
                        segment.getAirline(),
                        segment.getAirlineCode(),
                        segment.getMasterWaybill(),
                        segment.getStartFacility().getName(),
                        segment.getEndFacility().getName(),
                        FIRST_MILE.equals(segment.getType()) ? pickupInstruction : StringUtils.EMPTY,
                        LAST_MILE.equals(segment.getType()) ? deliveryInstruction : StringUtils.EMPTY,
                        segment.getInstruction(),
                        segment.getDuration(),
                        segment.getDurationUnit(),
                        segment.getPickUpTime(),
                        segment.getDropOffTime(),
                        segment.getLockOutTime(),
                        segment.getDepartureTime(),
                        segment.getArrivalTime(),
                        segment.getRecoveryTime(),
                        segment.getCalculatedMileage(),
                        segment.getCalculatedMileageUnit(),
                        milestoneName,
                        shipment.getServiceType().getName(),
                        customerName,
                        shipment.getEtaStatus()
                );
            }
        }
    }

    public ShipmentExportFilterResult export(ExportFilter exportFilter) {
        ShipmentCriteria shipmentCriteria = shipmentCriteriaMapper.mapFilterToCriteria(
                exportFilter,
                objectMapper,
                shipmentLocationCoveragePredicates
        );
        shipmentCriteria.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        locationCoverageCriteriaEnricher.enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        userPartnerCriteriaEnricher.enrichCriteriaByPartners(shipmentCriteria);
        ShipmentSpecification shipmentSpecification = shipmentCriteria.buildSpecification();
        List<Shipment> shipments = shipmentProjectionExport.findAll(shipmentSpecification).stream().map(this::toShipment).toList();
        return new ShipmentExportFilterResult(shipments).filter(exportFilter);
    }

    private String getCustomerName(Shipment shipment) {
        return shipment.getCustomer() != null ? shipment.getCustomer().getName() : StringUtils.EMPTY;
    }

    private String getMilestoneName(Shipment shipment) {
        return shipment.getMilestone() != null ? shipment.getMilestone().getMilestoneName() : StringUtils.EMPTY;
    }

    private String getPartnerName(PackageJourneySegment segment) {
        return segment.getPartner() != null ? segment.getPartner().getName() : StringUtils.EMPTY;
    }

    private String getTransportType(PackageJourneySegment segment) {
        return segment.getTransportType().name();
    }

    private Shipment toShipment(ShipmentEntity shipmentEntity) {
        Shipment shipment = new Shipment();
        shipment.setShipmentTrackingId(shipmentEntity.getShipmentTrackingId());
        shipment.setOrigin(AddressMapper.mapEntityToDomain(shipmentEntity.getOrigin()));
        shipment.setDestination(AddressMapper.mapEntityToDomain(shipmentEntity.getDestination()));
        shipment.setShipmentJourney(ShipmentJourneyMapper.mapEntityToDomain(shipmentEntity.getShipmentJourney()));
        shipment.setInstructions(shipmentEntity.getInstructions());
        shipment.setMilestoneEvents(
                shipmentEntity.getMilestoneEvents()
                        .stream()
                        .map(milestoneMapper::toDomain)
                        .toList()
        );
        milestoneService.setMostRecentMilestone(shipment);
        shipment.setServiceType(ServiceTypeMapper.mapEntityToDomain(shipmentEntity.getServiceType()));
        shipment.setCustomer(CustomerMapper.mapEntityToDomain(shipmentEntity.getCustomer()));
        shipment.setEtaStatus(shipmentEntity.getEtaStatus());
        return shipment;
    }
}
