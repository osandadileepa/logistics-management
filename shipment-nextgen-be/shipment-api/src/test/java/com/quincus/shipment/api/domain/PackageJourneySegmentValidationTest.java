package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageJourneySegmentValidationTest extends ValidationTest {
    @Test
    void packageJourneySegment_ShouldHaveViolation() {
        assertThat(validateModel(new PackageJourneySegment())).isNotEmpty();
    }

    @Test
    void packageJourneySegment_ValidateShouldPass() {
        var pjs = new PackageJourneySegment();
        pjs.setRefId("1");
        pjs.setType(SegmentType.LAST_MILE);
        pjs.setStatus(SegmentStatus.PLANNED);
        pjs.setTransportType(TransportType.AIR);
        assertThat(validateModel(pjs)).isEmpty();
    }
}
