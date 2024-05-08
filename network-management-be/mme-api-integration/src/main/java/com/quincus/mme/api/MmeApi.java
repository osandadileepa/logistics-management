package com.quincus.mme.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.mme.model.MmeTrainModelResponse;

public interface MmeApi {
    MmeTrainModelResponse trainModel(
            JsonNode payload,
            String organizationId,
            String organizationName,
            String userId
    );

    MmeTrainModelResponse getTrainedModel(
            MmeGetTrainedModelRequest request,
            String organizationId,
            String organizationName,
            String userId,
            String trainingRequestId
    );
}
