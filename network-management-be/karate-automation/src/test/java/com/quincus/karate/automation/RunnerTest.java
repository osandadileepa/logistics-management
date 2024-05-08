package com.quincus.karate.automation;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

    @Test
    void testNetworkManagement() {
        Results results = Runner.path("classpath:features")
                .tags("@Regression")
                .parallel(6);
        assertThat(results.getFailCount()).withFailMessage(results.getErrorMessages()).isZero();
    }
}
