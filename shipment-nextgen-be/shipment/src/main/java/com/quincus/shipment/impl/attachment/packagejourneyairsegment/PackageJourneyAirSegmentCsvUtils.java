package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_AIRLINE_CODE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_AIR_WAY_BILL;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_ARRIVAL_DATE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_ARRIVAL_DATE_TIMEZONE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_DEPARTURE_DATE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_DEPARTURE_DATE_TIMEZONE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_FLIGHT_NUMBER;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_GROSS_WEIGHT;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_INSTRUCTION_CONTENT;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_LOCK_OUT_DATE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_LOCK_OUT_DATE_TIMEZONE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_MEASUREMENT_UNIT;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_ORIGIN_CITY;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_ORIGIN_COUNTRY;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_ORIGIN_FACILITY;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_ORIGIN_STATE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_RECOVERY_DATE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_RECOVERY_DATE_TIMEZONE;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_SHIPMENT_ID;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_VENDOR;
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.CSV_FIELD_VOLUME_WEIGHT;

@UtilityClass
@Slf4j
public class PackageJourneyAirSegmentCsvUtils {
    public static final String CSV_TEMPLATE = "csv-template/package-journey-segment-flight-details.csv";
    public static final String PRE_POPULATED_CSV_TEMPLATE = "csv-template/package-journey-segment-flight-details-pre-populated.csv";
    public static final String SAMPLE_SHIPMENT_ID = "EXAMPLE";
    private static final String DELIMITER = ",";
    static final String[] PRE_POPULATED_CSV_HEADERS = {CSV_FIELD_SHIPMENT_ID, CSV_FIELD_MEASUREMENT_UNIT, CSV_FIELD_VOLUME_WEIGHT, CSV_FIELD_GROSS_WEIGHT
            , CSV_FIELD_AIRLINE_CODE, CSV_FIELD_FLIGHT_NUMBER, CSV_FIELD_DEPARTURE_DATE, CSV_FIELD_DEPARTURE_DATE_TIMEZONE
            , CSV_FIELD_ORIGIN_FACILITY, CSV_FIELD_ORIGIN_COUNTRY, CSV_FIELD_ORIGIN_STATE, CSV_FIELD_ORIGIN_CITY
            , CSV_FIELD_AIR_WAY_BILL, CSV_FIELD_VENDOR, CSV_FIELD_LOCK_OUT_DATE, CSV_FIELD_LOCK_OUT_DATE_TIMEZONE
            , CSV_FIELD_ARRIVAL_DATE, CSV_FIELD_ARRIVAL_DATE_TIMEZONE, CSV_FIELD_RECOVERY_DATE
            , CSV_FIELD_RECOVERY_DATE_TIMEZONE, CSV_FIELD_INSTRUCTION_CONTENT};

    static final String[] CSV_HEADERS = {CSV_FIELD_SHIPMENT_ID, CSV_FIELD_AIRLINE_CODE, CSV_FIELD_FLIGHT_NUMBER
            , CSV_FIELD_DEPARTURE_DATE, CSV_FIELD_DEPARTURE_DATE_TIMEZONE
            , CSV_FIELD_ORIGIN_FACILITY, CSV_FIELD_ORIGIN_COUNTRY, CSV_FIELD_ORIGIN_STATE, CSV_FIELD_ORIGIN_CITY
            , CSV_FIELD_AIR_WAY_BILL, CSV_FIELD_VENDOR, CSV_FIELD_LOCK_OUT_DATE, CSV_FIELD_LOCK_OUT_DATE_TIMEZONE
            , CSV_FIELD_ARRIVAL_DATE, CSV_FIELD_ARRIVAL_DATE_TIMEZONE, CSV_FIELD_RECOVERY_DATE
            , CSV_FIELD_RECOVERY_DATE_TIMEZONE, CSV_FIELD_INSTRUCTION_CONTENT};
    public static final CSVFormat PRE_POPULATED_CSV_FORMAT = CSVFormat.Builder.create().setHeader(PRE_POPULATED_CSV_HEADERS).setDelimiter(DELIMITER).build();
    public static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create().setHeader(CSV_HEADERS).setDelimiter(DELIMITER).build();

    public static String readExampleFromCsvTemplate(InputStream is) {
        if (is == null) {
            log.error("Sample CSV template file not provided");
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                log.error("CSV template is empty or corrupted");
                return null;
            }
            // Now read and return the second line (the example data)
            String secondLine = reader.readLine();
            if (secondLine == null || !secondLine.contains(SAMPLE_SHIPMENT_ID)) {
                log.error("CSV template does not contain example data after header");
                return null;
            }
            return secondLine;
        } catch (IOException e) {
            log.error("Error reading example data from CSV template", e);
            return null;
        }
    }

}