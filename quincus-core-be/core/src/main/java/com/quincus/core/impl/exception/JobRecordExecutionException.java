package com.quincus.core.impl.exception;

public class JobRecordExecutionException extends QuincusException {
    private final int jobDataChildrenCount;

    public JobRecordExecutionException(String msg) {
        this(msg, 0);
    }

    public JobRecordExecutionException(String msg, int jobDataChildrenCount) {
        super(msg);
        this.jobDataChildrenCount = jobDataChildrenCount;
    }

    public int getJobDataChildrenCount() {
        return this.jobDataChildrenCount;
    }
}
