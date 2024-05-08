package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.mapper.ShipmentCriteriaMapper;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionExport;
import com.quincus.shipment.impl.repository.specification.ShipmentSpecification;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.PRE_POPULATED_CSV_FORMAT;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.PRE_POPULATED_CSV_TEMPLATE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.readExampleFromCsvTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageJourneyAirSegmentCsvExportService {
    private static final String CSV_EXPORT_ERROR = "Unable to export csv file for package air journey segment";
    private static final String CSV_SAMPLE_TEMPLATE_CONTENT = generateExampleFromCsvTemplate();
    private final ObjectMapper objectMapper;
    private final ShipmentCriteriaMapper shipmentCriteriaMapper;
    private final LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;
    private final UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;
    private final UserDetailsProvider userDetailsProvider;
    private final List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates;
    private final ShipmentProjectionExport projectionExport;

    public void export(ExportFilter exportFilter, Writer writer) {
        ShipmentCriteria shipmentCriteria = shipmentCriteriaMapper.mapFilterToCriteria(
                exportFilter,
                objectMapper,
                shipmentLocationCoveragePredicates
        );
        shipmentCriteria.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        locationCoverageCriteriaEnricher.enrichCriteriaWithUserLocationCoverage(shipmentCriteria);
        userPartnerCriteriaEnricher.enrichCriteriaByPartners(shipmentCriteria);
        ShipmentSpecification shipmentSpecification = shipmentCriteria.buildSpecification();
        List<Map<String, String>> shipmentDetails = projectionExport.findAllShipmentTrackingIds(shipmentSpecification, shipmentSpecification.buildPageable());
        writeShipmentTrackingIdsToCsv(shipmentDetails, writer);
    }

    private void writeShipmentTrackingIdsToCsv(List<Map<String, String>> shipmentDetails, Writer writer) {
        try {
            CSVPrinter printer = new CSVPrinter(writer, PRE_POPULATED_CSV_FORMAT);
            appendSampleContent(writer);
            for (Map<String, String> shipmentDetail : shipmentDetails) {
                printer.printRecord(shipmentDetail.get(ShipmentEntity_.SHIPMENT_TRACKING_ID), shipmentDetail.get(PackageDimensionEntity_.MEASUREMENT_UNIT)
                        , shipmentDetail.get(PackageDimensionEntity_.GROSS_WEIGHT), shipmentDetail.get(PackageDimensionEntity_.VOLUME_WEIGHT));
            }
        } catch (Exception e) {
            log.error(CSV_EXPORT_ERROR);
            throw new QuincusException(CSV_EXPORT_ERROR, e);
        }
    }

    private void appendSampleContent(Writer writer) throws IOException {
        if (CSV_SAMPLE_TEMPLATE_CONTENT != null) {
            writer.write(CSV_SAMPLE_TEMPLATE_CONTENT);
            writer.write(System.lineSeparator());
        }
    }

    private static String generateExampleFromCsvTemplate() {
        return readExampleFromCsvTemplate(PackageJourneyAirSegmentCsvUtils.class.getClassLoader().getResourceAsStream(PRE_POPULATED_CSV_TEMPLATE));
    }
}
