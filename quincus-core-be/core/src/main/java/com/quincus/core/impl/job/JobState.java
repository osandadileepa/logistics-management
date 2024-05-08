package com.quincus.core.impl.job;

public enum JobState {
    QUEUE,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}
