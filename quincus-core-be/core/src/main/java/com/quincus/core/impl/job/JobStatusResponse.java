package com.quincus.core.impl.job;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JobStatusResponse {
    private String jobId;
    private JobState status;
    private Long processedRecords;
    private Long successfulRecords;
    private Long failedRecords;
    private Long totalRecords;
    private String elapsedTime;
    private String errorMessage;
    private String errorCode;
    private List<?> errorRecords;

    public JobStatusResponse(String jobId) {
        this.jobId = jobId;
    }
}
