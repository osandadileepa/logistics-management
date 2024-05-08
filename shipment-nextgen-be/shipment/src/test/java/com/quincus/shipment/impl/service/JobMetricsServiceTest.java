package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.JobState;
import com.quincus.shipment.api.domain.JobMetrics;
import com.quincus.shipment.api.exception.JobCancelationNotAllowedException;
import com.quincus.shipment.api.exception.JobNotFoundException;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.repository.JobMetricsRepository;
import com.quincus.shipment.impl.repository.entity.JobMetricsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobMetricsServiceTest {
    private static final String orgId = "org-123";
    private static final String jobId = "job-123";
    private static final String executedByName = "test-User";
    private static final String executedById = UUID.randomUUID().toString();
    private static final List<String> data = Arrays.asList("test-data-1", "test-data-2", "test-data-3");
    @Mock
    private JobMetricsRepository jobMetricsRepository;
    @Mock
    private JobMetricsMapper<String> mapper;
    @InjectMocks
    private JobMetricsService<String> jobMetricsService;
    private JobMetrics<String> jobMetrics;

    @BeforeEach
    public void setUp() {
        jobMetrics = new JobMetrics<>(data, orgId, executedById, executedByName);
        jobMetrics.setId(jobId);
    }

    @Test
    @DisplayName("GIVEN JobMetrics Domain WHEN createJob THEN mapper maps domain to entity and entity would be save")
    void testCreateJob() {
        //GIVEN:
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setId(UUID.randomUUID().toString());
        jobMetricsEntity.setId(jobId);
        jobMetricsEntity.setOrganizationId(orgId);
        when(mapper.mapToEntity(jobMetrics)).thenReturn(jobMetricsEntity);
        when(jobMetricsRepository.saveAndFlush(jobMetricsEntity)).thenReturn(jobMetricsEntity);
        //WHEN:
        jobMetricsService.createJob(jobMetrics);
        //THEN:
        verify(jobMetricsRepository, times(1)).saveAndFlush(jobMetricsEntity);
    }

    @Test
    @DisplayName("GIVEN JobMetrics Domain WHEN updateJobMetrics THEN mapper maps domain to entity and entity would be save")
    void testUpdateJobMetrics() {
        //GIVEN:
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setId(jobId);
        jobMetricsEntity.setOrganizationId(orgId);
        when(jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(jobMetricsEntity));
        when(mapper.toEntityForUpdate(jobMetricsEntity, jobMetrics)).thenReturn(jobMetricsEntity);
        //WHEN:
        jobMetricsService.updateJobMetrics(jobMetrics);
        //THEN:
        verify(jobMetricsRepository, times(1)).saveAndFlush(jobMetricsEntity);
        verify(mapper, times(1)).toEntityForUpdate(jobMetricsEntity, jobMetrics);
    }

    @Test
    @DisplayName("GIVEN job id and orgId WHEN getJobStateByJobId THEN return the status of entity")
    void testGetJobStateByJobId() {
        //GIVEN:
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setStatus(JobState.IN_PROGRESS);
        when(jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(jobMetricsEntity));
        //WHEN:
        JobMetricsEntity entity = jobMetricsService.getJobMetricsByJobId(jobId, orgId);
        //THEN:
        assertThat(entity.getStatus()).isEqualTo(JobState.IN_PROGRESS);
    }

    @Test
    @DisplayName("GIVEN job id and orgId that is has cancellable status WHEN cancelJob THEN jobMetricsEntity status would be cancel and be saved")
    void testCancelJob() {
        //GIVEN:
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setStatus(JobState.IN_PROGRESS);
        when(jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(jobMetricsEntity));
        //WHEN:
        jobMetricsService.cancelJob(jobId, orgId);
        //THEN:
        verify(jobMetricsRepository, times(1)).saveAndFlush(jobMetricsEntity);
        assertThat(jobMetricsEntity.getStatus()).isEqualTo(JobState.CANCELLED);
    }

    @Test
    @DisplayName("GIVEN job id and orgId that is already cancelled WHEN cancelJob THEN expect JobCancelationNotAllowedException should be thrown")
    void testCancelNonCancelableJob() {
        //GIVEN:
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        jobMetricsEntity.setStatus(JobState.CANCELLED);
        when(jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(jobMetricsEntity));
        //WHEN THEN
        assertThatThrownBy(() -> jobMetricsService.cancelJob(jobId, orgId)).isInstanceOf(JobCancelationNotAllowedException.class);
    }

    @Test
    @DisplayName("GIVEN job id and orgId with no result WHEN findJobByIdAndOrgId THEN expect JobNotFoundException should be thrown")
    void testFindJobByIdAndOrgIdJobNotFound() {
        //GIVEN:
        when(jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.empty());
        //WHEN THEN
        assertThatThrownBy(() -> jobMetricsService.findJobByIdAndOrgId(jobId, orgId)).isInstanceOf(JobNotFoundException.class);
    }

    @Test
    @DisplayName("GIVEN job id and orgId WHEN findJobByIdAndOrgId THEN entity would be map to domain and will be returned")
    void testFindJobByIdAndOrgId() {
        //GIVEN:
        JobMetricsEntity jobMetricsEntity = new JobMetricsEntity();
        when(jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId)).thenReturn(Optional.of(jobMetricsEntity));

        when(mapper.mapToDomain(jobMetricsEntity)).thenReturn(jobMetrics);
        //WHEN:
        JobMetrics<String> result = jobMetricsService.findJobByIdAndOrgId(jobId, orgId);
        //THEN:
        verify(mapper, times(1)).mapToDomain(jobMetricsEntity);
        assertThat(result).isEqualTo(jobMetrics);
    }
}
