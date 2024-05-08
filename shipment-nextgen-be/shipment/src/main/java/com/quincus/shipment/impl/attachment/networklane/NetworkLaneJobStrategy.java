package com.quincus.shipment.impl.attachment.networklane;

import com.quincus.shipment.api.domain.JobMetrics;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.shipment.api.exception.JobRecordExecutionException;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.mapper.NetworkLaneMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentMapper;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.shipment.impl.service.NetworkLaneService;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class NetworkLaneJobStrategy extends JobTemplateStrategy<NetworkLaneCsv> {

    private static final String UNEXPECTED_ERROR = "There was an unexpected error on saving Network Lane";
    private final NetworkLaneMapper networkLaneMapper;
    private final NetworkLaneSegmentMapper networkLaneSegmentMapper;
    private final NetworkLaneService networkLaneService;
    private final NetworkLaneCsvValidator networkLaneCsvValidator;

    NetworkLaneJobStrategy(JobMetricsService<NetworkLaneCsv> jobMetricsService
            , NetworkLaneSegmentMapper networkLaneSegmentMapper
            , NetworkLaneService networkLaneService
            , NetworkLaneMapper networkLaneMapper
            , NetworkLaneCsvValidator networkLaneCsvValidator) {
        super(jobMetricsService);
        this.networkLaneSegmentMapper = networkLaneSegmentMapper;
        this.networkLaneService = networkLaneService;
        this.networkLaneMapper = networkLaneMapper;
        this.networkLaneCsvValidator = networkLaneCsvValidator;
    }

    @Transactional(noRollbackFor = JobRecordExecutionException.class)
    public void execute(NetworkLaneCsv data) {
        try {
            networkLaneCsvValidator.validate(data);
            NetworkLane domain = networkLaneMapper.mapCsvToDomain(data);
            domain.setNetworkLaneSegments(data.getNetworkLaneSegments().stream()
                    .filter(networkLaneSegmentCsv -> !networkLaneSegmentCsv.isIgnoreRecord())
                    .map(networkLaneSegmentMapper::mapCsvToDomain).toList());
            networkLaneService.saveFromBulkUpload(domain, domain.getOrganizationId());
        } catch (QuincusValidationException e) {
            data.setFailedReason(e.getMessage());
            throw new JobRecordExecutionException(e.getMessage(), data.getNetworkLaneSegments().size());
        } catch (QuincusException qe) {
            log.warn(UNEXPECTED_ERROR, qe);
            data.setFailedReason(UNEXPECTED_ERROR);
            throw new JobRecordExecutionException(UNEXPECTED_ERROR, data.getNetworkLaneSegments().size());
        }
    }

    @Override
    protected void postRecordProcess(JobMetrics<NetworkLaneCsv> jobMetrics, List<NetworkLaneCsv> dataWithError) {
        if (CollectionUtils.isNotEmpty(dataWithError) && jobMetrics.getMostRecordSubDataCount() != 0) {
            padNetworkLaneSegments(jobMetrics, dataWithError);
        }
        super.postRecordProcess(jobMetrics, dataWithError);
    }

    private void padNetworkLaneSegments(JobMetrics<NetworkLaneCsv> jobMetrics, List<NetworkLaneCsv> dataWithError) {
        dataWithError.forEach(networkLaneWithError -> {
            int segmentDiff = jobMetrics.getMostRecordSubDataCount() - networkLaneWithError.getNetworkLaneSegments().size();
            if (segmentDiff > 0) {
                networkLaneWithError.addAllNetworkLaneSegmentCsv(createNetworkLaneSegmentsForPadding(segmentDiff));
            }
        });
    }

    private List<NetworkLaneSegmentCsv> createNetworkLaneSegmentsForPadding(int paddingCountNeeded) {
        return IntStream.range(0, paddingCountNeeded).mapToObj(x -> new NetworkLaneSegmentCsv())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected void handleJobRecordExecutionException(JobRecordExecutionException jobRecordExecutionException, JobMetrics<NetworkLaneCsv> jobMetrics,
                                                    NetworkLaneCsv dataToProcess, List<NetworkLaneCsv> dataWithError) {
        super.handleJobRecordExecutionException(jobRecordExecutionException, jobMetrics, dataToProcess, dataWithError);
        if (jobMetrics.getMostRecordSubDataCount() < jobRecordExecutionException.getJobDataChildrenCount()) {
            jobMetrics.setMostRecordSubDataCount(jobRecordExecutionException.getJobDataChildrenCount());
        }
    }
}