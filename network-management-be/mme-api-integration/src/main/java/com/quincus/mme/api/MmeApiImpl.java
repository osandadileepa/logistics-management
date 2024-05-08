package com.quincus.mme.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.mme.model.MmeTrainModelResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MmeApiImpl implements MmeApi {
    private final MmeRestClient mmeRestClient;

    @Override
    public MmeTrainModelResponse trainModel(JsonNode payload, String organizationId, String organizationName, String userId) {
        return mmeRestClient.trainModel(payload, organizationId, organizationName, userId);
    }

    @Override
    public MmeTrainModelResponse getTrainedModel(MmeGetTrainedModelRequest request, String organizationId, String organizationName, String userId, String trainingRequestId) {
        return mmeRestClient.getTrainedModel(request, organizationId, organizationName, userId, trainingRequestId);
    }
}
