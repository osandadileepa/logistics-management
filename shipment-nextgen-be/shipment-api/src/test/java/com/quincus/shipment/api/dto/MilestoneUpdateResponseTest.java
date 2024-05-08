package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.constant.ResponseCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MilestoneUpdateResponseTest {

    @ParameterizedTest
    @MethodSource("responseProvider")
    void testMilestoneUpdateResponse(MilestoneUpdateResponse response) {
        // Assert
        assertThat(response.getOrderNumber()).isNotNull();
        assertThat(response.getSegmentId()).isNotNull();
        assertThat(response.getMilestone()).isNotNull();
        assertThat(response.getResponseCode()).isNotNull();
        assertThat(response.getResponseMessage()).isNotNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    private static List<MilestoneUpdateResponse> responseProvider() {
        // Create test instances of MilestoneUpdateResponse
        MilestoneUpdateResponse response1 = new MilestoneUpdateResponse();
        response1.setOrderNumber("ORD123");
        response1.setSegmentId("SEG456");
        response1.setMilestone("Milestone 1");
        response1.setResponseCode(ResponseCode.SCC0000);
        response1.setResponseMessage("Success");
        response1.setTimestamp(Instant.now());

        MilestoneUpdateResponse response2 = new MilestoneUpdateResponse();
        response2.setOrderNumber("ORD789");
        response2.setSegmentId("SEG012");
        response2.setMilestone("Milestone 2");
        response2.setResponseCode(ResponseCode.ERR9999);
        response2.setResponseMessage("Failed");
        response2.setTimestamp(Instant.now());

        // Return the test instances
        return Arrays.asList(response1, response2);
    }

    @Test
    void getTimestamp_NullValue_ReturnsNull() {
        // Arrange
        MilestoneUpdateResponse response = new MilestoneUpdateResponse();

        // Act
        String timestamp = response.getTimestamp();

        // Assert
        assertThat(timestamp).isNull();
    }

    @ParameterizedTest
    @MethodSource("validTimestampsProvider")
    void getTimestamp_ValidTimestamp_ReturnsFormattedString(Instant timestamp, String expectedFormattedTimestamp) {
        // Arrange
        MilestoneUpdateResponse response = new MilestoneUpdateResponse();
        response.setTimestamp(timestamp);

        // Act
        String actualFormattedTimestamp = response.getTimestamp();

        // Assert
        assertThat(actualFormattedTimestamp).isEqualTo(expectedFormattedTimestamp);
    }

    private static List<Object[]> validTimestampsProvider() {
        return Arrays.asList(
                new Object[]{Instant.parse("2023-05-19T10:15:30Z"), "2023-05-19T10:15:30Z"},
                new Object[]{Instant.parse("2023-06-20T12:30:45Z"), "2023-06-20T12:30:45Z"}
                // Add more test cases as needed
        );
    }
}