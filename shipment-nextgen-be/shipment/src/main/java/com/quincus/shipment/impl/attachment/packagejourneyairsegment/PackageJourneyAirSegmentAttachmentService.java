package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.impl.attachment.AbstractAttachmentService;
import com.quincus.shipment.impl.attachment.ExportableAttachmentService;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.quincus.shipment.api.constant.AttachmentType.PACKAGE_JOURNEY_AIR_SEGMENT;
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
import static com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv.FAILED_REASON_IDENTIFIER;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.CSV_FORMAT;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.CSV_TEMPLATE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.PRE_POPULATED_CSV_FORMAT;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvUtils.SAMPLE_SHIPMENT_ID;

@Service
@Slf4j
public class PackageJourneyAirSegmentAttachmentService extends AbstractAttachmentService<PackageJourneyAirSegmentCsv> implements ExportableAttachmentService {
    private static final String CSV_TO_PACKAGE_AIR_JOURNEY_SEGMENT_CSV_ERROR_MESSAGE = "Unable to parse CSV to package journey air segment from file : `%s`.";
    private static final String CSV_RECORDS_ERROR_MESSAGE = "The CSV file does not contain any valid records.";
    private final PackageJourneyAirSegmentCsvExportService csvExportService;

    public PackageJourneyAirSegmentAttachmentService(JobMetricsService<PackageJourneyAirSegmentCsv> jobMetricsService,
                                                     JobTemplateStrategy<PackageJourneyAirSegmentCsv> jobTemplateStrategy,
                                                     JobMetricsMapper<PackageJourneyAirSegmentCsv> jobMetricsMapper,
                                                     UserDetailsProvider userDetailsProvider,
                                                     PackageJourneyAirSegmentCsvExportService csvExportService) {
        super(jobMetricsService, jobTemplateStrategy, jobMetricsMapper, userDetailsProvider);
        this.csvExportService = csvExportService;
    }

    @Override
    public AttachmentType getAttachmentType() {
        return PACKAGE_JOURNEY_AIR_SEGMENT;
    }

    @Override
    public String getCsvTemplate() {
        return CSV_TEMPLATE;
    }

    @Override
    public List<PackageJourneyAirSegmentCsv> parseToDomain(MultipartFile multipartFile) {
        List<PackageJourneyAirSegmentCsv> packageAirJourneySegments = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            String firstLine = reader.readLine();
            CSVParser csvParser = PRE_POPULATED_CSV_FORMAT.parse(reader);
            boolean isPrepopulatedTemplate = StringUtils.containsAnyIgnoreCase(firstLine, CSV_FIELD_MEASUREMENT_UNIT, CSV_FIELD_VOLUME_WEIGHT, CSV_FIELD_GROSS_WEIGHT);
            if (!isPrepopulatedTemplate) {
                csvParser = CSV_FORMAT.parse(reader);
            }

            StreamSupport.stream(csvParser.spliterator(), false)
                    .filter(csvRecord -> !isHeaderOrSampleDataRecord(csvRecord))
                    .forEach(csvRecord -> {
                        PackageJourneyAirSegmentCsv airJourneySegmentAttachment = new PackageJourneyAirSegmentCsv();
                        airJourneySegmentAttachment.setShipmentId(csvRecord.get(CSV_FIELD_SHIPMENT_ID));
                        if (isPrepopulatedTemplate) {
                            airJourneySegmentAttachment.setMeasurementUnit(csvRecord.get(CSV_FIELD_MEASUREMENT_UNIT));
                            airJourneySegmentAttachment.setVolumeWeight(csvRecord.get(CSV_FIELD_VOLUME_WEIGHT));
                            airJourneySegmentAttachment.setGrossWeight(csvRecord.get(CSV_FIELD_GROSS_WEIGHT));
                        }
                        airJourneySegmentAttachment.setAirlineCode(csvRecord.get(CSV_FIELD_AIRLINE_CODE));
                        airJourneySegmentAttachment.setFlightNumber(csvRecord.get(CSV_FIELD_FLIGHT_NUMBER));
                        airJourneySegmentAttachment.setDepartureDatetime(csvRecord.get(CSV_FIELD_DEPARTURE_DATE));
                        airJourneySegmentAttachment.setDepartureTimezone(csvRecord.get(CSV_FIELD_DEPARTURE_DATE_TIMEZONE));
                        airJourneySegmentAttachment.setOriginFacility(csvRecord.get(CSV_FIELD_ORIGIN_FACILITY));
                        airJourneySegmentAttachment.setOriginCountry(csvRecord.get(CSV_FIELD_ORIGIN_COUNTRY));
                        airJourneySegmentAttachment.setOriginState(csvRecord.get(CSV_FIELD_ORIGIN_STATE));
                        airJourneySegmentAttachment.setOriginCity(csvRecord.get(CSV_FIELD_ORIGIN_CITY));
                        airJourneySegmentAttachment.setAirWayBill(csvRecord.get(CSV_FIELD_AIR_WAY_BILL));
                        airJourneySegmentAttachment.setVendor(csvRecord.get(CSV_FIELD_VENDOR));
                        airJourneySegmentAttachment.setLockoutDatetime(csvRecord.get(CSV_FIELD_LOCK_OUT_DATE));
                        airJourneySegmentAttachment.setLockoutTimezone(csvRecord.get(CSV_FIELD_LOCK_OUT_DATE_TIMEZONE));
                        airJourneySegmentAttachment.setArrivalDatetime(csvRecord.get(CSV_FIELD_ARRIVAL_DATE));
                        airJourneySegmentAttachment.setArrivalTimezone(csvRecord.get(CSV_FIELD_ARRIVAL_DATE_TIMEZONE));
                        airJourneySegmentAttachment.setRecoveryDatetime(csvRecord.get(CSV_FIELD_RECOVERY_DATE));
                        airJourneySegmentAttachment.setRecoveryTimezone(csvRecord.get(CSV_FIELD_RECOVERY_DATE_TIMEZONE));
                        airJourneySegmentAttachment.setInstructionContent(csvRecord.get(CSV_FIELD_INSTRUCTION_CONTENT));
                        airJourneySegmentAttachment.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
                        concatExtraFieldsToInstructions(airJourneySegmentAttachment, csvRecord);
                        packageAirJourneySegments.add(airJourneySegmentAttachment);
                    });
            checkIfValidCsvRecords(packageAirJourneySegments);
            return packageAirJourneySegments;
        } catch (QuincusValidationException e) {
            log.warn(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.warn(String.format(CSV_TO_PACKAGE_AIR_JOURNEY_SEGMENT_CSV_ERROR_MESSAGE, multipartFile.getOriginalFilename()), e);
            throw new QuincusValidationException(String.format(CSV_TO_PACKAGE_AIR_JOURNEY_SEGMENT_CSV_ERROR_MESSAGE, multipartFile.getOriginalFilename()), e);
        }
    }

    private void concatExtraFieldsToInstructions(PackageJourneyAirSegmentCsv airJourneySegmentAttachment, CSVRecord csvRecord) {
        int csvExpectedColumnCount = PackageJourneyAirSegmentCsvUtils.PRE_POPULATED_CSV_HEADERS.length;
        if (csvRecord.size() <= csvExpectedColumnCount) {
            return;
        }
        String exceededStringInCsv = IntStream.range(csvExpectedColumnCount, csvRecord.size()).mapToObj(csvRecord::get)
                .filter(csvString -> !csvString.startsWith(FAILED_REASON_IDENTIFIER)).collect(Collectors.joining(","));
        if (StringUtils.isBlank(exceededStringInCsv)) {
            return;
        }
        airJourneySegmentAttachment.setInstructionContent(airJourneySegmentAttachment.getInstructionContent().concat(",").concat(exceededStringInCsv));
    }

    private void checkIfValidCsvRecords(List<PackageJourneyAirSegmentCsv> packageAirJourneySegments) {
        if (CollectionUtils.isEmpty(packageAirJourneySegments)) {
            throw new QuincusValidationException(CSV_RECORDS_ERROR_MESSAGE);
        }
    }

    private boolean isHeaderOrSampleDataRecord(CSVRecord csvRecord) {
        return StringUtils.containsIgnoreCase(csvRecord.toString(), CSV_FIELD_SHIPMENT_ID) || csvRecord.get(CSV_FIELD_SHIPMENT_ID).contains(SAMPLE_SHIPMENT_ID);
    }

    @Override
    public void export(ExportFilter exportFilter, Writer writer) {
        csvExportService.export(exportFilter, writer);
    }
}
