package com.quincus.networkmanagement.impl.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.networkmanagement.UtilityController;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.api.filter.SearchFilterResult;
import com.quincus.networkmanagement.impl.preprocessor.PreprocessingService;
import com.quincus.networkmanagement.impl.service.MmeService;
import com.quincus.networkmanagement.impl.service.TrainingLogService;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@AllArgsConstructor
@RestController
public class UtilityControllerImpl implements UtilityController {

    private final PreprocessingService preprocessingService;
    private final MmeService mmeService;
    private final TrainingLogService trainingLogService;
    private final UserDetailsContextHolder userDetailsContextHolder;

    @Override
    public JsonNode generateTrainingInput(String date) {
        Long dateOfTraining = null;

        if (StringUtils.isNotBlank(date)) {
            try {
                dateOfTraining = new SimpleDateFormat("MM/dd/yyyy").parse(date).getTime();
            } catch (ParseException ignored) {
                // do nothing
            }
        }

        return preprocessingService.generateTrainingInput(
                userDetailsContextHolder.getCurrentOrganizationId(),
                dateOfTraining
        );
    }

    @Override
    public TrainingLog trainModel() {
        return mmeService.trainModel(
                TrainingType.MANUAL_VIA_UTILITY,
                userDetailsContextHolder.getCurrentOrganizationId(),
                userDetailsContextHolder.getCurrentOrganizationName(),
                userDetailsContextHolder.getCurrentUserId()
        );
    }

    @Override
    public TrainingLog getTrainedModel(@Valid MmeGetTrainedModelRequest trainRequest) {
        return mmeService.getTrainedModel(trainRequest);
    }

    @Override
    public Response<SearchFilterResult<TrainingLog>> list(Pageable pageable) {
        Page<TrainingLog> result = trainingLogService.list(pageable);
        return new Response<>(new SearchFilterResult<>(result, null));
    }
}
