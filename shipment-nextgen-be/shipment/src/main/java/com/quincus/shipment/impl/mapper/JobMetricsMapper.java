package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.domain.JobMetrics;
import com.quincus.shipment.api.dto.JobStatusResponse;
import com.quincus.shipment.impl.repository.entity.JobMetricsEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.lang.Class.forName;

@Component
@AllArgsConstructor
@Slf4j
public class JobMetricsMapper<V> {

    private static final String JOB_ERROR_DATA_PARSE_ERROR = "Exception occurred while parsing error data : {}";

    private final ObjectMapper objectMapper;

    public JobMetrics<V> mapToDomain(JobMetricsEntity entity) {
        JobMetrics<V> domain = new JobMetrics<>(entity.getTotalRecords(), entity.getOrganizationId()
                , entity.getExecutedById(), entity.getExecutedByName());
        domain.setStatus(entity.getStatus());
        domain.setDataWithError(serializeData(entity.getDataWithError(), entity.getRecordClassType()));
        domain.setFailReason(entity.getFailReason());
        domain.setFailedRecords(entity.getFailedRecords());
        domain.setSuccessfulRecords(entity.getSuccessfulRecords());
        domain.setProcessedRecords(entity.getProcessedRecords());
        domain.setTimeStarted(entity.getTimeStarted());
        domain.setTimeElapsed(entity.getTimeElapsed());
        return domain;
    }

    public JobMetricsEntity mapToEntity(JobMetrics<V> domain) {
        JobMetricsEntity entity = new JobMetricsEntity();
        entity.setFailedRecords(domain.getFailedRecords());
        entity.setStatus(domain.getStatus());
        entity.setTotalRecords(domain.getTotalRecords());
        entity.setSuccessfulRecords(domain.getSuccessfulRecords());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setProcessedRecords(domain.getProcessedRecords());
        entity.setExecutedById(domain.getExecutedById());
        entity.setExecutedByName(domain.getExecutedByName());
        entity.setTimeStarted(domain.getTimeStarted());
        entity.setTimeElapsed(domain.getTimeElapsed());
        return entity;
    }

    public JobMetricsEntity toEntityForUpdate(JobMetricsEntity entity, JobMetrics<V> domain) {
        entity.setProcessedRecords(domain.getProcessedRecords());
        entity.setFailedRecords(domain.getFailedRecords());
        entity.setSuccessfulRecords(domain.getSuccessfulRecords());
        entity.setStatus(domain.getStatus());
        entity.setFailReason(domain.getFailReason());
        entity.setDataWithError(parseDataToString(domain.getDataWithError()));
        entity.setRecordClassType(domain.getRecordClassType());
        entity.setTimeElapsed(domain.getTimeElapsed());
        return entity;
    }

    public JobStatusResponse toJobStatusResponse(JobMetricsEntity entity) {
        JobStatusResponse jobStatusResponse = new JobStatusResponse();
        jobStatusResponse.setJobId(entity.getId());
        jobStatusResponse.setStatus(entity.getStatus());
        jobStatusResponse.setProcessedRecords(entity.getProcessedRecords());
        jobStatusResponse.setTotalRecords(entity.getTotalRecords());
        jobStatusResponse.setSuccessfulRecords(entity.getSuccessfulRecords());
        jobStatusResponse.setFailedRecords(entity.getFailedRecords());
        jobStatusResponse.setErrorMessage(entity.getFailReason());
        jobStatusResponse.setErrorRecords(serializeData(entity.getDataWithError(), entity.getRecordClassType()));
        jobStatusResponse.setElapsedTime(DateTimeUtil.formatDuration(entity.getTimeElapsed()));
        return jobStatusResponse;
    }

    private String parseDataToString(List<V> data) {
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn(JOB_ERROR_DATA_PARSE_ERROR, e.getMessage());
        }
        return null;
    }

    private List<V> serializeData(String data, String className) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(className)) {
            return Collections.emptyList();
        }
        try {
            Class<?> clz = forName(className);
            CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clz);
            return objectMapper.readValue(data, javaType);
        } catch (JsonProcessingException | ClassNotFoundException e) {
            log.warn(JOB_ERROR_DATA_PARSE_ERROR, e.getMessage());
            return Collections.emptyList();
        }
    }

}
