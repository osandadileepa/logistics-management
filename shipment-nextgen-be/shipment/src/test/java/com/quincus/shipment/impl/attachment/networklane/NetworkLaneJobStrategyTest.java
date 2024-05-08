package com.quincus.shipment.impl.attachment.networklane;

import com.quincus.shipment.api.constant.JobState;
import com.quincus.shipment.api.domain.JobMetrics;
import com.quincus.shipment.api.domain.NetworkLane;
import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.shipment.api.exception.JobRecordExecutionException;
import com.quincus.shipment.impl.mapper.NetworkLaneMapper;
import com.quincus.shipment.impl.mapper.NetworkLaneSegmentMapper;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.shipment.impl.service.NetworkLaneService;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneJobStrategyTest {

    @Mock
    private NetworkLaneMapper networkLaneMapper;
    @Mock
    private NetworkLaneSegmentMapper networkLaneSegmentMapper;
    @Mock
    private NetworkLaneService networkLaneService;
    @Mock
    private NetworkLaneCsvValidator networkLaneCsvValidator;
    @Mock
    private JobMetricsService<NetworkLaneCsv> jobMetricsService;
    @Mock
    JobMetrics<NetworkLaneCsv> jobMetrics;

    @InjectMocks
    private NetworkLaneJobStrategy networkLaneJobStrategy;

    @Test
    void givenQuincusValidationExceptionThrownWhenExecuteThenWrapExceptionAndThrowsJobRecordExecutionException() {
        // Arrange
        NetworkLaneCsv data = new NetworkLaneCsv();
        data.setNetworkLaneSegments(List.of(new NetworkLaneSegmentCsv()));
        QuincusValidationException exception = new QuincusValidationException("Validation failed");

        doThrow(exception).when(networkLaneCsvValidator).validate(data);

        // Act and Assert
        assertThatThrownBy(()->networkLaneJobStrategy.execute(data)).isInstanceOf(JobRecordExecutionException.class);
    }

    @Test
    void givenQuincusExceptionThrownWhenExecuteThenWrapExceptionAndThrowsJobRecordExecutionException() {
        // Arrange
        String orgId = "org_id";
        NetworkLaneCsv csvData = new NetworkLaneCsv();
        csvData.setNetworkLaneSegments(List.of(new NetworkLaneSegmentCsv()));
        NetworkLane data = new NetworkLane();
        data.setOrganizationId(orgId);
        QuincusException exception = new QuincusException("Error saving");
        when(networkLaneMapper.mapCsvToDomain(csvData)).thenReturn(data);
        doThrow(exception).when(networkLaneService).saveFromBulkUpload(data, orgId);

        // Act and Assert
        assertThatThrownBy(() -> networkLaneJobStrategy.execute(csvData)).isInstanceOf(JobRecordExecutionException.class)
                .hasMessage("There was an unexpected error on saving Network Lane");
    }

    @Test
    void givenNoExceptionInValidationWhenExecuteThenTriggerMapperAndSaveWithoutRollbackMethod() throws QuincusException {
        // GIVEN:
        NetworkLaneCsv csvData = new NetworkLaneCsv();
        csvData.setNetworkLaneSegments(List.of(new NetworkLaneSegmentCsv()));
        NetworkLane domain = new NetworkLane();
        domain.setOrganizationId("123");

        when(networkLaneMapper.mapCsvToDomain(csvData)).thenReturn(domain);

        // WHEN:
        networkLaneJobStrategy.execute(csvData);

        // THEN:
        verify(networkLaneService, times(1)).saveFromBulkUpload(domain, "123");
        verify(networkLaneCsvValidator, times(1)).validate(csvData);
        verify(networkLaneMapper, times(1)).mapCsvToDomain(csvData);
        verify(networkLaneSegmentMapper, times(1)).mapCsvToDomain(csvData.getNetworkLaneSegments().get(0));
    }

    @Test
    void givenDataErrorWithOneSegmentAndLongestSegmentIsTwoWhenPostRecordProcessThenDataRecordShouldHaveTwoSegment() throws QuincusException {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = new NetworkLaneCsv();
        networkLaneCsv.addAllNetworkLaneSegmentCsv(List.of(new NetworkLaneSegmentCsv()));
        List<NetworkLaneCsv> dataWithErrors = new ArrayList<>();
        dataWithErrors.add(networkLaneCsv);

        when(jobMetrics.getMostRecordSubDataCount()).thenReturn(2);
        when(jobMetrics.getData()).thenReturn(List.of(new NetworkLaneCsv()));

        // WHEN:
        networkLaneJobStrategy.postRecordProcess(jobMetrics, dataWithErrors);

        // THEN:
        verify(jobMetrics, times(1)).setStatus(JobState.COMPLETED);
        verify(jobMetrics, times(1)).setDataWithError(dataWithErrors);
        assertThat(dataWithErrors.get(0).getNetworkLaneSegments()).hasSize(2);
    }

    @Test
    @DisplayName("GIVEN JobRecordException has jobChildrenRecord count greater than JobMetrics most sub record count WHEN handleJobRecordExecutionException THEN setMostRecordSubDataCount to JobRecordException jobChildrenRecord count")
    void testHandleJobRecordExecutionException() throws QuincusException {
        // GIVEN:

        JobRecordExecutionException jobRecordExecutionException = mock(JobRecordExecutionException.class);
        NetworkLaneCsv dataToProcess = new NetworkLaneCsv();
        List<NetworkLaneCsv> dataWithError = new ArrayList<>();

        when(jobMetrics.getMostRecordSubDataCount()).thenReturn(0);
        when(jobRecordExecutionException.getJobDataChildrenCount()).thenReturn(2);
        // WHEN:
        networkLaneJobStrategy.handleJobRecordExecutionException(jobRecordExecutionException, jobMetrics, dataToProcess, dataWithError);

        // THEN:
        verify(jobMetrics, times(1)).setMostRecordSubDataCount(2);
        verify(jobMetrics, times(1)).incrementFailedRecords();
    }
    
}
