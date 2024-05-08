package com.quincus.networkmanagement.impl.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.mme.api.MmeApi;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.mme.model.MmeTrainModelResponse;
import com.quincus.mme.model.MmeTrainingStatus;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.api.exception.MmeApiCallException;
import com.quincus.networkmanagement.api.exception.TrainingLogNotFoundException;
import com.quincus.networkmanagement.impl.config.MmeTrainingProperties;
import com.quincus.networkmanagement.impl.preprocessor.PreprocessingService;
import com.quincus.networkmanagement.impl.utility.DebounceUtility;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

import static com.quincus.networkmanagement.impl.data.PreprocessingTestData.dummyTrainingInput;
import static com.quincus.networkmanagement.impl.data.PreprocessingTestData.dummyTrainingLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {MmeService.class})
class MmeServiceTest {
    @Mock
    private UserDetailsContextHolder userDetailsContextHolder;
    @Mock
    private MmeApi mmeApi;
    @Mock
    private TrainingLogService trainingLogService;
    @Mock
    private PreprocessingService preprocessingService;
    @Mock
    private MmeTrainingProperties mmeTrainingProperties;
    @Mock
    private DebounceUtility debounceUtility;

    @InjectMocks
    private MmeService mmeService;

    @Test
    void testTrainModel() {
        ObjectNode payload = dummyTrainingInput();
        String organizationId = "ORGANIZATION-ID";
        String organizationName = "ORGANIZATION-NAME";
        String userId = "USER-ID";
        MmeTrainModelResponse expectedResponse = new MmeTrainModelResponse(MmeTrainingStatus.INITIATED, "UNIQUE-ID");

        when(preprocessingService.generateTrainingInput(organizationId, null)).thenReturn(payload);
        when(mmeApi.trainModel(payload, organizationId, organizationName, userId)).thenReturn(expectedResponse);
        when(trainingLogService.create(any())).thenAnswer(i -> i.getArguments()[0]);

        TrainingLog trainingLog = mmeService.trainModel(TrainingType.ON_NETWORK_CHANGE, organizationId, organizationName, userId);

        assertThat(trainingLog).isNotNull();
        assertThat(trainingLog.getStatus().name()).isEqualTo(expectedResponse.getStatus().name());
        assertThat(trainingLog.getUniqueId()).isEqualTo(expectedResponse.getUniqueId());
    }

    @Test
    void testTrainModelWithDelay() {
        String organizationId = "ORGANIZATION-ID";
        String organizationName = "ORGANIZATION-NAME";
        String debounceKeyPrefix = "NETWORK-TRAINING-";
        long trainingDelay = 10L;

        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(organizationId);
        when(userDetailsContextHolder.getCurrentOrganizationName()).thenReturn(organizationName);
        when(mmeTrainingProperties.getTrainingDelay()).thenReturn(trainingDelay);
        when(mmeTrainingProperties.getDebounceKeyPrefix()).thenReturn(debounceKeyPrefix);

        mmeService.trainModelWithDelay();

        verify(debounceUtility, times(1))
                .debounce(
                        eq(debounceKeyPrefix + organizationId),
                        any(),
                        eq(trainingDelay),
                        eq(TimeUnit.MINUTES)
                );
    }

    @Test
    void testGetTrainedModel() {
        MmeGetTrainedModelRequest request = new MmeGetTrainedModelRequest();
        String organizationId = "ORGANIZATION-ID";
        String organizationName = "ORGANIZATION-NAME";
        String userId = "USER-ID";
        String uniqueId = "UNIQUE-ID";
        request.setUniqueId(uniqueId);
        request.setOverride(false);

        TrainingLog trainingLog = dummyTrainingLog();
        trainingLog.setUniqueId(uniqueId);

        MmeTrainModelResponse expectedResponse = new MmeTrainModelResponse(MmeTrainingStatus.COMPLETED, "UNIQUE-ID");

        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(organizationId);
        when(userDetailsContextHolder.getCurrentOrganizationName()).thenReturn(organizationName);
        when(userDetailsContextHolder.getCurrentUserId()).thenReturn(userId);
        when(mmeApi.getTrainedModel(request, organizationId, organizationName, userId, trainingLog.getTrainingRequestId())).thenReturn(expectedResponse);
        when(trainingLogService.findByUniqueId(any())).thenReturn(trainingLog);
        when(trainingLogService.update(any())).thenAnswer(i -> i.getArguments()[0]);

        TrainingLog response = mmeService.getTrainedModel(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus().name()).isEqualTo(expectedResponse.getStatus().name());
        assertThat(response.getUniqueId()).isEqualTo(expectedResponse.getUniqueId());
    }

    @Test
    void trainModelApiCallExceptionThrowsMmeApiCallException() {
        ObjectNode payload = dummyTrainingInput();
        String organizationId = "ORGANIZATION-ID";
        String organizationName = "ORGANIZATION-NAME";
        String userId = "USER-ID";

        when(preprocessingService.generateTrainingInput(organizationId, null)).thenReturn(payload);

        when(mmeApi.trainModel(any(), eq(organizationId), eq(organizationName), eq(userId)))
                .thenThrow(new ApiCallException("Error message for bad request", HttpStatus.BAD_REQUEST));

        assertThatExceptionOfType(MmeApiCallException.class)
                .isThrownBy(() -> mmeService.trainModel(TrainingType.ON_NETWORK_CHANGE, organizationId, organizationName, userId))
                .withMessageContaining("Error message for bad request");
    }

    @Test
    void getTrainedModelApiCallExceptionThrowsMmeApiCallException() {
        MmeGetTrainedModelRequest request = new MmeGetTrainedModelRequest();
        String organizationId = "ORGANIZATION-ID";
        String organizationName = "ORGANIZATION-NAME";
        String userId = "USER-ID";
        String uniqueId = "UNIQUE-ID";
        request.setUniqueId(uniqueId);
        request.setOverride(false);

        TrainingLog trainingLog = dummyTrainingLog();
        trainingLog.setUniqueId(uniqueId);

        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(organizationId);
        when(userDetailsContextHolder.getCurrentOrganizationName()).thenReturn(organizationName);
        when(userDetailsContextHolder.getCurrentUserId()).thenReturn(userId);
        when(trainingLogService.findByUniqueId(any())).thenReturn(trainingLog);

        when(mmeApi.getTrainedModel(request, organizationId, organizationName, userId, trainingLog.getTrainingRequestId()))
                .thenThrow(new ApiCallException("Error message for internal server error", HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThatExceptionOfType(MmeApiCallException.class)
                .isThrownBy(() -> mmeService.getTrainedModel(request))
                .withMessageContaining("Error message for internal server error");
    }

    @Test
    void getTrainedModelTrainingLogNotFoundThrowsMmeApiCallException() {
        MmeGetTrainedModelRequest request = new MmeGetTrainedModelRequest();
        request.setUniqueId("UNIQUE-ID");
        request.setOverride(false);

        when(trainingLogService.findByUniqueId(any()))
                .thenThrow(new TrainingLogNotFoundException("Training log not found"));

        Assertions.assertThatExceptionOfType(MmeApiCallException.class)
                .isThrownBy(() -> mmeService.getTrainedModel(request))
                .withMessageContaining("Training log not found");
    }
}

