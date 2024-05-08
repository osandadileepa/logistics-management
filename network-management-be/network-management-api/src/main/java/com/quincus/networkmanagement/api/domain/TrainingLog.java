package com.quincus.networkmanagement.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.quincus.networkmanagement.api.constant.TrainingStatus;
import com.quincus.networkmanagement.api.constant.TrainingType;
import com.quincus.networkmanagement.api.validator.constraint.ValidNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@ValidNode
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TrainingLog extends Tenant {
    private String userId;
    private String trainingRequestId;
    private String uniqueId;
    private TrainingStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timeStarted;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant timeCompleted;
    private long generateRequestElapsedTime;
    private TrainingType trainingType;

}
