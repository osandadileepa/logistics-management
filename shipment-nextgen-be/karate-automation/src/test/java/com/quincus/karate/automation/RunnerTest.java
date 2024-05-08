package com.quincus.karate.automation;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

    @Test
    void testSegmentRegression() {
        Results resultsPJS = Runner.path("classpath:features/shipment")
                .tags("@SegmentRegression")
                .parallel(1);

        assertThat(resultsPJS.getFailCount()).withFailMessage(resultsPJS.getErrorMessages()).isZero();
    }

    @Test
    void testShipment() {
        Results results = Runner.path("classpath:features")
                .tags("@Regression")
                .parallel(6);
        assertThat(results.getFailCount()).withFailMessage(results.getErrorMessages()).isZero();
    }

    @Test
    void testAuthentication() {
        Results results = Runner.path("classpath:features/authentication")
                .tags("@Permissions")
                .tags("@CostPermissions")
                .parallel(1);
        assertThat(results.getFailCount()).withFailMessage(results.getErrorMessages()).isZero();
    }

    @Test
    void testUtility() {
        Results results = Runner.path("classpath:features/utility")
                .tags("@Utility")
                .parallel(1);
        assertThat(results.getFailCount()).withFailMessage(results.getErrorMessages()).isZero();
    }
}
