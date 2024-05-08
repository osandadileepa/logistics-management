package com.quincus.shipment.api.dto.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.ERROR_MSG_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;

class PackageJourneyAirSegmentCsvTest {
    private PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv;

    @BeforeEach
    void setUp() {
        packageJourneyAirSegmentCsv = new PackageJourneyAirSegmentCsv();
    }

    @Test
    void testAddErrorMessage() {
        // Arrange
        String errorMessage = "Error message 1";

        // Act
        packageJourneyAirSegmentCsv.addErrorMessage(errorMessage);

        // Assert
        assertThat(packageJourneyAirSegmentCsv.getErrorMessages()).isNotNull();
        assertThat(packageJourneyAirSegmentCsv.getErrorMessages()).containsExactly(errorMessage);
    }

    @Test
    void testBuildFailedReason() {
        // Arrange
        List<String> errorMessages = new ArrayList<>();
        errorMessages.add("Error message 1");
        errorMessages.add("Error message 2");
        packageJourneyAirSegmentCsv.setErrorMessages(errorMessages);

        // Act
        packageJourneyAirSegmentCsv.buildFailedReason();

        // Assert
        assertThat(packageJourneyAirSegmentCsv.getFailedReason()).isNotNull();
        assertThat(packageJourneyAirSegmentCsv.getFailedReason()).isEqualTo("Validation Error:Error message 1" + ERROR_MSG_DELIMITER + "Error message 2");
    }

    @Test
    void testBuildFailedReasonWithNoErrorMessages() {
        // Arrange
        packageJourneyAirSegmentCsv.setErrorMessages(null);

        // Act
        packageJourneyAirSegmentCsv.buildFailedReason();

        // Assert
        assertThat(packageJourneyAirSegmentCsv.getFailedReason()).isNull();
    }

    @Test
    void testBuildFailedReasonWithBlankFailedReason() {
        // Arrange
        packageJourneyAirSegmentCsv.setErrorMessages(new ArrayList<>());
        packageJourneyAirSegmentCsv.setFailedReason(null); // Blank failed reason

        // Act
        packageJourneyAirSegmentCsv.buildFailedReason();

        // Assert
        assertThat(packageJourneyAirSegmentCsv.getFailedReason()).isNull();
    }
}
