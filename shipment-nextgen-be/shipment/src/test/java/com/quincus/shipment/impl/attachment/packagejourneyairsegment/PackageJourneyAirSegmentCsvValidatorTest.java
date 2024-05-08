package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.AIRLINE_CODE_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.DATE_FORMAT_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.EMPTY_DATE_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.FLIGHT_NUMBER_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.INSTRUCTION_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.SHIPMENT_ID_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.TIMEZONE_FORMAT_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentCsvValidatorTest {

    @Mock
    private QPortalApi qPortalApi;
    @InjectMocks
    private PackageJourneyAirSegmentCsvValidator validator;

    @Test
    @DisplayName("Given a valid PackageJourneyAirSegmentCsv, when validating, then it should be considered valid")
    void givenValidPackageJourneyAirSegmentCsvWhenValidatingThenConsideredValid() {
        PackageJourneyAirSegmentCsv csv = createPackageJourneyAirSegmentCsv(
                "123",
                "SG",
                "1234",
                "2023-05-13T10:30:00",
                "UTC+08:00");
        int limit = 4000;
        String instruction = RandomStringUtils.randomAlphabetic(limit);
        csv.setInstructionContent(instruction);

        boolean isValid = validator.isValid(csv);

        assertThat(isValid).isTrue();
        assertThat(csv.getErrorMessages()).isNull();
    }

    @DisplayName("Given an invalid PackageJourneyAirSegmentCsv with blank fields, when validating, then it should be considered invalid")
    @ParameterizedTest
    @MethodSource("invalidPackageJourneyAirSegmentCsvProvider")
    void givenInvalidPackageJourneyAirSegmentCsvWithBlankFieldsWhenValidatingThenConsideredInvalid(PackageJourneyAirSegmentCsv csv) {
        boolean isValid = validator.isValid(csv);

        assertThat(isValid).isFalse();
        assertThat(csv.getErrorMessages()).hasSize(4)
                .contains(SHIPMENT_ID_ERROR_MESSAGE)
                .contains(AIRLINE_CODE_ERROR_MESSAGE)
                .contains(FLIGHT_NUMBER_ERROR_MESSAGE)
                .contains(String.format(EMPTY_DATE_ERROR_MESSAGE, "Departure"));
    }

    @Test
    @DisplayName("Given an invalid PackageJourneyAirSegmentCsv with invalid departure date format, when validating, then it should be considered invalid")
    void givenInvalidPackageJourneyAirSegmentCsvWithInvalidDepartureDateFormatWhenValidatingThenConsideredInvalid() {
        PackageJourneyAirSegmentCsv csv = createPackageJourneyAirSegmentCsv(
                "123",
                "SG",
                "1234",
                "2023-05-13", //Invalid format
                "GMT+08:00" //Invalid format
        );
        int limit = 4000;
        String longInstruction = RandomStringUtils.randomAlphabetic(limit + 1);
        csv.setInstructionContent(longInstruction);

        boolean isValid = validator.isValid(csv);

        assertThat(isValid).isFalse();
        assertThat(csv.getErrorMessages()).hasSize(3)
                .contains(
                        String.format(DATE_FORMAT_ERROR_MESSAGE, "Departure"),
                        String.format(TIMEZONE_FORMAT_ERROR_MESSAGE, "Departure"),
                        INSTRUCTION_ERROR_MESSAGE);
    }

    private static PackageJourneyAirSegmentCsv createPackageJourneyAirSegmentCsv(String shipmentId,
                                                                                 String airlineCode,
                                                                                 String flightNumber,
                                                                                 String departureDate,
                                                                                 String departureTimezone) {
        PackageJourneyAirSegmentCsv csv = new PackageJourneyAirSegmentCsv();
        csv.setShipmentId(shipmentId);
        csv.setAirlineCode(airlineCode);
        csv.setFlightNumber(flightNumber);
        csv.setDepartureDatetime(departureDate);
        csv.setDepartureTimezone(departureTimezone);
        return csv;
    }

    private static Stream<PackageJourneyAirSegmentCsv> invalidPackageJourneyAirSegmentCsvProvider() {
        return Stream.of(
                createPackageJourneyAirSegmentCsv(null, null, null, null, null),
                createPackageJourneyAirSegmentCsv("", "", "", "", ""),
                createPackageJourneyAirSegmentCsv(" ", " ", " ", " ", " ")
        );
    }
}
