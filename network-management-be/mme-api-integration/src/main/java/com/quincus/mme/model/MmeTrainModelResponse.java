package com.quincus.mme.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MmeTrainModelResponse {
    private String uniqueId;
    private MmeTrainingStatus status;
    private String error;
    private String nwTrainRequestId;
    private String userId;

    public MmeTrainModelResponse(MmeTrainingStatus status, String uniqueId) {
        this.status = status;
        this.uniqueId = uniqueId;
    }
}
