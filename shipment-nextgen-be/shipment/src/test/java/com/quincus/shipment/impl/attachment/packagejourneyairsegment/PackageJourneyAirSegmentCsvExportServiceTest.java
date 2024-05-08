package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.impl.enricher.LocationCoverageCriteriaEnricher;
import com.quincus.shipment.impl.enricher.UserPartnerCriteriaEnricher;
import com.quincus.shipment.impl.mapper.ShipmentCriteriaMapper;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.projection.ShipmentProjectionExport;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentCsvExportServiceTest {
    @InjectMocks
    private PackageJourneyAirSegmentCsvExportService service;

    @Mock
    private ShipmentCriteriaMapper shipmentCriteriaMapper;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private LocationCoverageCriteriaEnricher locationCoverageCriteriaEnricher;

    @Mock
    private UserPartnerCriteriaEnricher userPartnerCriteriaEnricher;

    @Mock
    private ShipmentProjectionExport projectionExport;

    @Test
    void testExport() {
        ExportFilter exportFilter = new ExportFilter();
        StringWriter writer = new StringWriter();

        ShipmentCriteria criteria = new ShipmentCriteria();
        Map<String, String> expected = new HashMap<>();
        expected.put(ShipmentEntity_.SHIPMENT_TRACKING_ID, "EXAMPLE1");
        expected.put(PackageDimensionEntity_.MEASUREMENT_UNIT, "measurementUnit");
        expected.put(PackageDimensionEntity_.VOLUME_WEIGHT, "volumeWeight");
        expected.put(PackageDimensionEntity_.GROSS_WEIGHT, "grossWeight");
        when(shipmentCriteriaMapper.mapFilterToCriteria(any(), any(), any())).thenReturn(criteria);
        when(projectionExport.findAllShipmentTrackingIds(any(), any())).thenReturn(List.of(expected));

        service.export(exportFilter, writer);
        writer.flush();

        assertThat(writer.toString()).contains("EXAMPLE1");
        assertThat(writer.toString()).contains("measurementUnit");
        assertThat(writer.toString()).contains("volumeWeight");
        assertThat(writer.toString()).contains("grossWeight");
        verify(locationCoverageCriteriaEnricher).enrichCriteriaWithUserLocationCoverage(criteria);
        verify(userPartnerCriteriaEnricher).enrichCriteriaByPartners(criteria);
    }
}
