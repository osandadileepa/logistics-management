package com.quincus.shipment.impl.attachment.milestone;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import com.quincus.shipment.impl.attachment.AbstractAttachmentService;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.quincus.shipment.api.constant.AttachmentType.MILESTONE;

@Service
@Slf4j
public class MilestoneAttachmentService extends AbstractAttachmentService<MilestoneCsv> {

    static final String CSV_TEMPLATE = "csv-template/milestone.csv";
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create()
            .setHeader(MilestoneCsv.getCsvHeaders()).build();
    private static final String ERROR_LOG_PARSING = "Unable to parse to file `{}` due to the error: `{}`";
    private static final String ERROR_MSG_PARSING = "Unable to parse to file `%s` due to the error: `%s`";

    public MilestoneAttachmentService(JobMetricsService<MilestoneCsv> jobMetricsService,
                                      JobTemplateStrategy<MilestoneCsv> jobTemplateStrategy,
                                      JobMetricsMapper<MilestoneCsv> milestoneJobMetricsMapper,
                                      UserDetailsProvider userDetailsProvider) {
        super(jobMetricsService, jobTemplateStrategy, milestoneJobMetricsMapper, userDetailsProvider);
    }

    @Override
    public List<MilestoneCsv> parseToDomain(MultipartFile multipartFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSV_FORMAT.parse(reader);

            return StreamSupport.stream(csvParser.spliterator(), false)
                    .filter(csvRecord -> !isHeaderRecord(csvRecord))
                    .map(this::parseCsvRecord).toList();
        } catch (Exception e) {
            log.warn(ERROR_LOG_PARSING, multipartFile.getOriginalFilename(), e.getMessage(), e);
            throw new QuincusValidationException(String.format(ERROR_MSG_PARSING, multipartFile.getOriginalFilename(), e.getMessage()), e);
        }
    }

    @Override
    public AttachmentType getAttachmentType() {
        return MILESTONE;
    }

    @Override
    public String getCsvTemplate() {
        return CSV_TEMPLATE;
    }

    private boolean isHeaderRecord(CSVRecord csvRecord) {
        return csvRecord.getRecordNumber() == 1;
    }

    private MilestoneCsv parseCsvRecord(CSVRecord csvRecord) {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setRecordNumber(csvRecord.getRecordNumber());
        milestoneCsv.setSize(csvRecord.size());
        milestoneCsv.setShipmentTrackingId(parseStringEntry(csvRecord, MilestoneCsv.HEADER_SHIPMENT_TRACKING_ID));
        milestoneCsv.setMilestoneCode(parseStringEntry(csvRecord, MilestoneCsv.HEADER_MILESTONE_CODE));
        milestoneCsv.setMilestoneName(parseStringEntry(csvRecord, MilestoneCsv.HEADER_MILESTONE_NAME));
        milestoneCsv.setMilestoneTime(parseStringEntry(csvRecord, MilestoneCsv.HEADER_MILESTONE_DATE_TIME));
        milestoneCsv.setFromCountry(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_COUNTRY));
        milestoneCsv.setFromState(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_STATE));
        milestoneCsv.setFromCity(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_CITY));
        milestoneCsv.setFromWard(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_WARD));
        milestoneCsv.setFromDistrict(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_DISTRICT));
        milestoneCsv.setFromLatitude(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_LATITUDE));
        milestoneCsv.setFromLongitude(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_LONGITUDE));
        milestoneCsv.setFromFacility(parseStringEntry(csvRecord, MilestoneCsv.HEADER_FROM_LOCATION));
        milestoneCsv.setToCountry(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_COUNTRY));
        milestoneCsv.setToState(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_STATE));
        milestoneCsv.setToCity(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_CITY));
        milestoneCsv.setToWard(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_WARD));
        milestoneCsv.setToDistrict(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_DISTRICT));
        milestoneCsv.setToLatitude(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_LATITUDE));
        milestoneCsv.setToLongitude(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_LONGITUDE));
        milestoneCsv.setToFacility(parseStringEntry(csvRecord, MilestoneCsv.HEADER_TO_LOCATION));
        milestoneCsv.setLatitude(parseStringEntry(csvRecord, MilestoneCsv.HEADER_LATITUDE));
        milestoneCsv.setLongitude(parseStringEntry(csvRecord, MilestoneCsv.HEADER_LONGITUDE));
        milestoneCsv.setHub(parseStringEntry(csvRecord, MilestoneCsv.HEADER_HUB_ID));
        milestoneCsv.setDriverName(parseStringEntry(csvRecord, MilestoneCsv.HEADER_DRIVER_NAME));
        milestoneCsv.setDriverPhoneCode(parseStringEntry(csvRecord, MilestoneCsv.HEADER_DRIVER_PHONE_CODE));
        milestoneCsv.setDriverPhoneNumber(parseStringEntry(csvRecord, MilestoneCsv.HEADER_DRIVER_PHONE_NUM));
        milestoneCsv.setDriverEmail(parseStringEntry(csvRecord, MilestoneCsv.HEADER_DRIVER_EMAIL));
        milestoneCsv.setVehicleType(parseStringEntry(csvRecord, MilestoneCsv.HEADER_VEHICLE_TYPE));
        milestoneCsv.setVehicleName(parseStringEntry(csvRecord, MilestoneCsv.HEADER_VEHICLE_NAME));
        milestoneCsv.setVehicleNumber(parseStringEntry(csvRecord, MilestoneCsv.HEADER_VEHICLE_NUM));
        milestoneCsv.setSenderName(parseStringEntry(csvRecord, MilestoneCsv.HEADER_SENDER_NAME));
        milestoneCsv.setSenderCompany(parseStringEntry(csvRecord, MilestoneCsv.HEADER_SENDER_COMPANY));
        milestoneCsv.setSenderDepartment(parseStringEntry(csvRecord, MilestoneCsv.HEADER_SENDER_DEPARTMENT));
        milestoneCsv.setReceiverName(parseStringEntry(csvRecord, MilestoneCsv.HEADER_RECEIVER_NAME));
        milestoneCsv.setReceiverCompany(parseStringEntry(csvRecord, MilestoneCsv.HEADER_RECEIVER_COMPANY));
        milestoneCsv.setReceiverDepartment(parseStringEntry(csvRecord, MilestoneCsv.HEADER_RECEIVER_DEPARTMENT));
        milestoneCsv.setEta(parseStringEntry(csvRecord, MilestoneCsv.HEADER_ETA));
        milestoneCsv.setNotes(parseStringEntry(csvRecord, MilestoneCsv.HEADER_NOTES));
        milestoneCsv.setOrganizationId(userDetailsProvider.getCurrentOrganizationId());
        milestoneCsv.setPartnerId(userDetailsProvider.getCurrentPartnerId());
        milestoneCsv.setUserId(userDetailsProvider.getCurrentUserId());
        return milestoneCsv;
    }

    private String parseStringEntry(CSVRecord csvRecord, String key) {
        String rec = csvRecord.get(key);
        return StringUtils.isBlank(rec) ? null : rec.trim();
    }
}
