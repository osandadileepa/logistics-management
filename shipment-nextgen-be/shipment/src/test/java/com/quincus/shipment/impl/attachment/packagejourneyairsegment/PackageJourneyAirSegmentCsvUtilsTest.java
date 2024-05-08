package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PackageJourneyAirSegmentCsvUtilsTest {
    private InputStream validCsvStream;
    private InputStream invalidCsvStream;
    private InputStream emptyCsvStream;

    @BeforeEach
    void setUp() {
        validCsvStream = new ByteArrayInputStream(("Shipment ID,Airline Code,Flight Number\n" +
                "EXAMPLE1,SG,1923").getBytes());
        invalidCsvStream = new ByteArrayInputStream(("Shipment ID,Airline Code,Flight Number").getBytes());
        emptyCsvStream = new ByteArrayInputStream(("").getBytes());
    }

    @Test
    void testReadExampleFromCsvTemplate_Valid() {
        String result = PackageJourneyAirSegmentCsvUtils.readExampleFromCsvTemplate(validCsvStream);
        assertThat(result).isEqualTo("EXAMPLE1,SG,1923");
    }

    @Test
    void testReadExampleFromCsvTemplate_NoExampleData() {
        String result = PackageJourneyAirSegmentCsvUtils.readExampleFromCsvTemplate(invalidCsvStream);
        assertThat(result).isNull();
    }

    @Test
    void testReadExampleFromCsvTemplate_EmptyTemplate() {
        String result = PackageJourneyAirSegmentCsvUtils.readExampleFromCsvTemplate(emptyCsvStream);
        assertThat(result).isNull();
    }
}