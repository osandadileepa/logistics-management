package com.quincus.shipment.impl.attachment.networklane;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.shipment.impl.attachment.AbstractAttachmentService;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.quincus.shipment.api.constant.AttachmentType.NETWORK_LANE;


@Service
@Slf4j
public class NetworkLaneAttachmentService extends AbstractAttachmentService<NetworkLaneCsv> {

    private static final int NETWORK_LANE_CSV_SEGMENT_LENGTH = 20;
    private static final int NETWORK_LANE_CSV_LENGTH = 14;
    private static final String ERROR_PARSING = "There was an error parsing the Network Lane csv";
    private static final String NETWORK_LANE_INVALID_CSV_LENGTH = "Network Lane CSV Invalid Length";
    private static final String NETWORK_LANE_SEGMENT_INVALID_CSV_LENGTH = "Network Lane Segment CSV Invalid Length";
    private static final String CSV_TEMPLATE = "csv-template/network-lane-template.csv";
    private static final String CSV_RECORDS_ERROR_MESSAGE = "The CSV file does not contain any valid records.";

    public NetworkLaneAttachmentService(JobMetricsService<NetworkLaneCsv> jobMetricsService,
                                        JobTemplateStrategy<NetworkLaneCsv> jobTemplateStrategy,
                                        JobMetricsMapper<NetworkLaneCsv> jobMetricsMapper,
                                        UserDetailsProvider userDetailsProvider) {
        super(jobMetricsService, jobTemplateStrategy, jobMetricsMapper, userDetailsProvider);
    }

    @Override
    public List<NetworkLaneCsv> parseToDomain(@NonNull MultipartFile multipartFile) {
        List<NetworkLaneCsv> networkLanes = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);
            StreamSupport.stream(csvParser.spliterator(), false)
                    .forEach(csvRecord -> {
                        if (isHeader(csvRecord)) {
                            return;
                        }
                        String[] csvRow = csvRecord.values();
                        validateCsvLineLength(csvRow);
                        NetworkLaneCsv networkLaneData = new NetworkLaneCsv();
                        networkLaneData.setLaneId(csvRow[0]);
                        networkLaneData.setServiceType(csvRow[1]);
                        networkLaneData.setOriginLocationTreeLevel1(csvRow[2]);
                        networkLaneData.setOriginLocationTreeLevel2(csvRow[3]);
                        networkLaneData.setOriginLocationTreeLevel3(csvRow[4]);
                        networkLaneData.setOriginLocationTreeLevel4(csvRow[5]);
                        networkLaneData.setOriginLocationTreeLevel5(csvRow[6]);
                        networkLaneData.setOriginFacilityId(csvRow[7]);
                        networkLaneData.setDestinationLocationTreeLevel1(csvRow[8]);
                        networkLaneData.setDestinationLocationTreeLevel2(csvRow[9]);
                        networkLaneData.setDestinationLocationTreeLevel3(csvRow[10]);
                        networkLaneData.setDestinationLocationTreeLevel4(csvRow[11]);
                        networkLaneData.setDestinationLocationTreeLevel5(csvRow[12]);
                        networkLaneData.setDestinationFacilityId(csvRow[13]);
                        networkLaneData.setNetworkLaneSegments(parseLineToNetworkLaneSegmentsCsv(csvRow));
                        networkLaneData.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
                        networkLaneData.setRow(csvRecord.getRecordNumber());
                        networkLanes.add(networkLaneData);
                    });
            checkIfValidCsvRecords(networkLanes);
            return networkLanes;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new QuincusException(String.format(ERROR_PARSING), e);
        }
    }

    private List<NetworkLaneSegmentCsv> parseLineToNetworkLaneSegmentsCsv(String[] csvRow) {
        List<NetworkLaneSegmentCsv> networkLaneSegmentList = new ArrayList<>();
        for (int i = 14; i < csvRow.length; i += 20) {
            NetworkLaneSegmentCsv networkLaneSegment = new NetworkLaneSegmentCsv();
            networkLaneSegment.setSequenceNumber(csvRow[i]);
            networkLaneSegment.setTransportCategory(csvRow[i + 1]);
            networkLaneSegment.setPartnerName(csvRow[i + 2]);
            networkLaneSegment.setVehicleInfo(csvRow[i + 3]);
            networkLaneSegment.setFlightNumber(csvRow[i + 4]);
            networkLaneSegment.setAirline(csvRow[i + 5]);
            networkLaneSegment.setAirlineCode(csvRow[i + 6]);
            networkLaneSegment.setMasterWaybill(csvRow[i + 7]);
            networkLaneSegment.setPickupFacilityName(csvRow[i + 8]);
            networkLaneSegment.setDropOffFacilityName(csvRow[i + 9]);
            networkLaneSegment.setPickupInstruction(csvRow[i + 10]);
            networkLaneSegment.setDropOffInstruction(csvRow[i + 11]);
            networkLaneSegment.setDuration(csvRow[i + 12]);
            networkLaneSegment.setDurationUnit(csvRow[i + 13]);
            networkLaneSegment.setLockOutTime(csvRow[i + 14]);
            networkLaneSegment.setDepartureTime(csvRow[i + 15]);
            networkLaneSegment.setArrivalTime(csvRow[i + 16]);
            networkLaneSegment.setRecoveryTime(csvRow[i + 17]);
            networkLaneSegment.setCalculatedMileage(csvRow[i + 18]);
            networkLaneSegment.setCalculatedMileageUnit(csvRow[i + 19]);
            networkLaneSegment.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
            networkLaneSegmentList.add(networkLaneSegment);
        }
        return networkLaneSegmentList;
    }

    @Override
    public AttachmentType getAttachmentType() {
        return NETWORK_LANE;
    }

    @Override
    public String getCsvTemplate() {
        return CSV_TEMPLATE;
    }

    private void validateCsvLineLength(String[] csvRow) {
        if (csvRow.length < NETWORK_LANE_CSV_LENGTH) {
            throw new QuincusException(NETWORK_LANE_INVALID_CSV_LENGTH);
        }

        int networkLaneSegmentCsvRowLength = csvRow.length - NETWORK_LANE_CSV_LENGTH;
        if (networkLaneSegmentCsvRowLength % NETWORK_LANE_CSV_SEGMENT_LENGTH != 0) {
            throw new QuincusException(NETWORK_LANE_SEGMENT_INVALID_CSV_LENGTH);
        }
    }

    private void checkIfValidCsvRecords(List<NetworkLaneCsv> networkLaneCsvList) {
        if (CollectionUtils.isEmpty(networkLaneCsvList)) {
            throw new QuincusValidationException(CSV_RECORDS_ERROR_MESSAGE);
        }
    }

    private boolean isHeader(CSVRecord csvRecord) {
        return csvRecord.getRecordNumber() == 1;
    }
}
