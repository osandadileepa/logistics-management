package com.quincus.networkmanagement.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.mme.api.MmeApi;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.mme.model.MmeTrainModelResponse;
import com.quincus.mme.model.MmeTrainingStatus;
import com.quincus.networkmanagement.api.constant.NetworkManagementErrorCode;
import com.quincus.networkmanagement.api.constant.TrainingStatus;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.api.exception.MmeApiCallException;
import com.quincus.networkmanagement.api.exception.TrainingLogNotFoundException;
import com.quincus.networkmanagement.impl.config.MmeTrainingProperties;
import com.quincus.networkmanagement.impl.preprocessor.PreprocessingService;
import com.quincus.networkmanagement.impl.preprocessor.helper.TrainingInputValidator;
import com.quincus.networkmanagement.impl.utility.DebounceUtility;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class MmeService {
    private final MmeTrainingProperties mmeTrainingProperties;
    private final UserDetailsContextHolder userDetailsContextHolder;
    private final TrainingLogService trainingLogService;
    private final PreprocessingService preprocessingService;
    private final MmeApi mmeApi;
    private final DebounceUtility debounceUtility;

    public void trainModelWithDelay() {

        long delay = mmeTrainingProperties.getTrainingDelay();
        TimeUnit delayUnit = TimeUnit.MINUTES;

        log.info("Training will trigger in {} {} unless further change in the network is introduced.", delay, delayUnit);

        String organizationId = userDetailsContextHolder.getCurrentOrganizationId();
        String organizationName = userDetailsContextHolder.getCurrentOrganizationName();

        debounceUtility.debounce(
                mmeTrainingProperties.getDebounceKeyPrefix() + organizationId,
                () -> trainModel(
                        TrainingType.ON_NETWORK_CHANGE,
                        organizationId,
                        organizationName,
                        null
                ),
                delay,
                delayUnit
        );
    }

    public TrainingLog trainModel(TrainingType trainingType, String organizationId, String organizationName, String userId) {

        Instant timeStarted = Instant.now();

        log.info("Attempting to train the network. Generating the training input. timeStarted: {}", timeStarted);

        JsonNode trainingInput = preprocessingService.generateTrainingInput(organizationId, null);
        TrainingInputValidator.validateTrainingInput(trainingInput);

        long generateRequestElapsedTime = Instant.now().toEpochMilli() - timeStarted.toEpochMilli();

        log.info("Training input generation completed. elapsedTime: {}", generateRequestElapsedTime);

        try {
            MmeTrainModelResponse response = mmeApi.trainModel(
                    trainingInput,
                    organizationId,
                    organizationName,
                    userId
            );

            TrainingLog trainingLog = new TrainingLog();
            trainingLog.setTimeStarted(timeStarted);
            trainingLog.setGenerateRequestElapsedTime(generateRequestElapsedTime);
            trainingLog.setOrganizationId(organizationId);
            trainingLog.setTrainingType(trainingType);
            trainingLog.setTrainingRequestId(response.getNwTrainRequestId());
            trainingLog.setStatus(TrainingStatus.valueOf(response.getStatus().name()));
            trainingLog.setUniqueId(response.getUniqueId());
            trainingLog.setUserId(userId);

            log.info("Creating new training log. uniqueId: {}, trainingRequestId: {}", trainingLog.getUniqueId(), trainingLog.getTrainingRequestId());

            return trainingLogService.create(trainingLog);
        } catch (ApiCallException e) {
            throw new MmeApiCallException(e.getMessage(), NetworkManagementErrorCode.MME_TRAIN_API_FAILED, e.getResponseStatus());
        }
    }

    public TrainingLog getTrainedModel(MmeGetTrainedModelRequest request) {
        try {
            TrainingLog trainingLog = trainingLogService.findByUniqueId(request.getUniqueId());

            MmeTrainModelResponse response = mmeApi.getTrainedModel(
                    request,
                    userDetailsContextHolder.getCurrentOrganizationId(),
                    userDetailsContextHolder.getCurrentOrganizationName(),
                    userDetailsContextHolder.getCurrentUserId(),
                    trainingLog.getTrainingRequestId()
            );

            trainingLog.setStatus(TrainingStatus.valueOf(response.getStatus().name()));

            if (response.getStatus() == MmeTrainingStatus.COMPLETED) {
                trainingLog.setTimeCompleted(Instant.now());
            }

            return trainingLogService.update(trainingLog);
        } catch (TrainingLogNotFoundException e) {
            throw new MmeApiCallException(e.getMessage(), NetworkManagementErrorCode.MME_GET_TRAINED_MODEL_API_FAILED, HttpStatus.NOT_FOUND);
        } catch (ApiCallException e) {
            throw new MmeApiCallException(e.getMessage(), NetworkManagementErrorCode.MME_GET_TRAINED_MODEL_API_FAILED, e.getResponseStatus());
        }
    }
}
