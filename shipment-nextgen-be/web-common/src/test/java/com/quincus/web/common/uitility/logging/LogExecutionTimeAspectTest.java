package com.quincus.web.common.uitility.logging;

import com.quincus.web.common.utility.logging.LoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogExecutionTimeAspectTest {
    @Mock
    private Logger log;

    @InjectMocks
    private LoggingAspect aspect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void calculateAndLogMethodExecutionTime() throws Throwable {
        // Create a mock ProceedingJoinPoint
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);

        // Execute the method
        aspect.calculateAndLogMethodExecutionTime(proceedingJoinPoint);

        // Verify that proceed was invoked
        verify(proceedingJoinPoint, times(1)).proceed();
    }
}
