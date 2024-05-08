package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.JobState;
import com.quincus.shipment.api.domain.JobMetrics;
import com.quincus.shipment.impl.repository.entity.JobMetricsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobMetricsMapperTest {
    private static final String orgId = "org-id-123";
    private static final String userId = "user-id-123";
    private static final String userName = "John Snow";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JobMetricsMapper<String> jobMetricsMapper = new JobMetricsMapper<>(objectMapper);

    @Test
    @DisplayName("GIVEN JobMetricsEntity provided with error record type WHEN mapToDomain THEN all values from entities should be supplied with blank error list value")
    void testMapToDomainWithErrorParsing() {
        //GIVEN:
        String data = "[\"test-data-1\",\"test-data-2\"]";
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setTotalRecords(10L);
        jobMetricsEntity.setOrganizationId(orgId);
        jobMetricsEntity.setDataWithError(data);
        jobMetricsEntity.setExecutedById(userId);
        jobMetricsEntity.setExecutedByName(userName);
        jobMetricsEntity.setSuccessfulRecords(10L);
        jobMetricsEntity.setFailedRecords(2L);
        jobMetricsEntity.setProcessedRecords(12L);
        jobMetricsEntity.setStatus(JobState.IN_PROGRESS);
        jobMetricsEntity.setFailReason("test failed reason");
        jobMetricsEntity.setRecordClassType("java.lang.Integer"); // Invalid class to parse
        //WHEN:
        JobMetrics<String> jobMetrics = jobMetricsMapper.mapToDomain(jobMetricsEntity);
        //THEN:
        assertThat(jobMetrics.getTotalRecords()).isEqualTo(10);
        assertThat(jobMetrics.getOrganizationId()).isEqualTo(orgId);
        assertThat(jobMetrics.getExecutedById()).isEqualTo(userId);
        assertThat(jobMetrics.getExecutedByName()).isEqualTo(userName);
        assertThat(jobMetrics.getDataWithError()).isEmpty();
        assertThat(jobMetrics.getFailReason()).isEqualTo("test failed reason");
        assertThat(jobMetrics.getFailedRecords()).isEqualTo(2);
        assertThat(jobMetrics.getSuccessfulRecords()).isEqualTo(10);
        assertThat(jobMetrics.getProcessedRecords()).isEqualTo(12);
        assertThat(jobMetrics.getStatus()).isEqualTo(JobState.IN_PROGRESS);

    }

    @Test
    @DisplayName("GIVEN JobMetricsEntity provided WHEN mapToDomain THEN all values from entities should be supplied")
    void testMapToDomain() {
        //GIVEN:
        String data = "[\"test-data-1\",\"test-data-2\"]";
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setTotalRecords(10L);
        jobMetricsEntity.setOrganizationId(orgId);
        jobMetricsEntity.setDataWithError(data);
        jobMetricsEntity.setExecutedById(userId);
        jobMetricsEntity.setExecutedByName(userName);
        jobMetricsEntity.setSuccessfulRecords(10L);
        jobMetricsEntity.setFailedRecords(2L);
        jobMetricsEntity.setProcessedRecords(12L);
        jobMetricsEntity.setStatus(JobState.IN_PROGRESS);
        jobMetricsEntity.setFailReason("test failed reason");
        jobMetricsEntity.setRecordClassType("java.lang.String");
        //WHEN:
        JobMetrics<String> jobMetrics = jobMetricsMapper.mapToDomain(jobMetricsEntity);
        //THEN:
        assertThat(jobMetrics.getTotalRecords()).isEqualTo(10);
        assertThat(jobMetrics.getOrganizationId()).isEqualTo(orgId);
        assertThat(jobMetrics.getExecutedById()).isEqualTo(userId);
        assertThat(jobMetrics.getExecutedByName()).isEqualTo(userName);
        assertThat(jobMetrics.getDataWithError()).hasSize(2);
        assertThat(jobMetrics.getFailReason()).isEqualTo("test failed reason");
        assertThat(jobMetrics.getFailedRecords()).isEqualTo(2);
        assertThat(jobMetrics.getSuccessfulRecords()).isEqualTo(10);
        assertThat(jobMetrics.getProcessedRecords()).isEqualTo(12);
        assertThat(jobMetrics.getStatus()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    @DisplayName("GIVEN JobMetrics Data provided WHEN mapToEntity THEN Only mapped values are supplied")
    void testMapToEntity() {
        //GIVEN:
        List<String> data = Arrays.asList("Test-data-1", "Test-data-2", "Test-data-3", "Test-data-4");
        List<String> failedData = Arrays.asList("Test-data-3", "Test-data-2");
        JobMetrics<String> jobMetrics = new JobMetrics<>(data, orgId, userId, userName);
        jobMetrics.setDataWithError(failedData);
        jobMetrics.setFailReason("test fail reason");
        //WHEN:
        JobMetricsEntity jobMetricsEntity = jobMetricsMapper.mapToEntity(jobMetrics);
        //THEN:
        assertThat(jobMetricsEntity.getTotalRecords()).isEqualTo(4);
        assertThat(jobMetricsEntity.getOrganizationId()).isEqualTo(orgId);
        assertThat(jobMetricsEntity.getExecutedById()).isEqualTo(userId);
        assertThat(jobMetricsEntity.getExecutedByName()).isEqualTo(userName);
        assertThat(jobMetricsEntity.getStatus()).isEqualTo(JobState.QUEUE);
        assertThat(jobMetricsEntity.getFailReason()).isNull();
        assertThat(jobMetricsEntity.getDataWithError()).isNull();
        assertThat(jobMetricsEntity.getFailedRecords()).isZero();
        assertThat(jobMetricsEntity.getSuccessfulRecords()).isZero();
        assertThat(jobMetricsEntity.getProcessedRecords()).isZero();
    }

    @Test
    @DisplayName("GIVEN JobMetrics and JobMetricsEntityProvided WHEN toEntityForUpdate THEN Only mapped values are for update")
    void testMapToEntityForUpdate() {
        //GIVEN:
        List<String> data = Arrays.asList("Test-data-1", "Test-data-2", "Test-data-3", "Test-data-4");
        List<String> failedData = Arrays.asList("Test-data-3", "Test-data-2");
        JobMetrics<String> jobMetrics = new JobMetrics<>(data, "other orgId", "otherId", "other username");
        jobMetrics.setDataWithError(failedData);
        jobMetrics.setFailReason("test fail reason");
        jobMetrics.setStatus(JobState.COMPLETED);
        jobMetrics.setFailedRecords(2);
        jobMetrics.setProcessedRecords(10);
        jobMetrics.setRecordClassType("java.lang.String");

        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setTotalRecords(12L);
        jobMetricsEntity.setOrganizationId(orgId);
        jobMetricsEntity.setExecutedById(userId);
        jobMetricsEntity.setExecutedByName(userName);
        jobMetricsEntity.setStatus(JobState.IN_PROGRESS);

        //WHEN:
        JobMetricsEntity updatedEntity = jobMetricsMapper.toEntityForUpdate(jobMetricsEntity, jobMetrics);
        //THEN:
        assertThat(updatedEntity.getExecutedById()).isEqualTo(userId);
        assertThat(updatedEntity.getExecutedByName()).isEqualTo(userName);
        assertThat(updatedEntity.getOrganizationId()).isEqualTo(orgId);
        assertThat(updatedEntity.getTotalRecords()).isEqualTo(12);
        assertThat(updatedEntity.getFailedRecords()).isEqualTo(2);
        assertThat(updatedEntity.getProcessedRecords()).isEqualTo(10);
        assertThat(updatedEntity.getFailReason()).isEqualTo("test fail reason");
        assertThat(updatedEntity.getRecordClassType()).isEqualTo("java.lang.String");
        assertThat(updatedEntity.getStatus()).isEqualTo(JobState.COMPLETED);
        assertThat(updatedEntity.getDataWithError()).isEqualTo("[\"Test-data-3\",\"Test-data-2\"]");
    }
}
