package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DISPATCH_SCHEDULED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static org.assertj.core.api.Assertions.assertThat;

class MilestoneValidationTest extends ValidationTest {

    private static Stream<Arguments> provideUnOrderedMilestoneAndExpectedOrder() {
        Milestone milestone1 = new Milestone();
        milestone1.setId("milestone1");
        milestone1.setMilestoneCode(OM_BOOKED);
        milestone1.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone1.setCreateTime(Instant.now());

        Milestone milestone2 = new Milestone();
        milestone2.setId("milestone2");
        milestone2.setMilestoneCode(DSP_PICKUP_SUCCESSFUL);
        milestone2.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).plusHours(1));
        milestone2.setCreateTime(Instant.now().minusSeconds(1000));

        Milestone milestone3 = new Milestone();
        milestone3.setId("milestone3");
        milestone3.setMilestoneCode(DSP_DELIVERY_SUCCESSFUL);
        milestone3.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()).plusHours(4));
        milestone3.setCreateTime(Instant.now().minusSeconds(4000));

        return Stream.of(
                Arguments.of(List.of(milestone1, milestone2, milestone3), milestone1),
                Arguments.of(List.of(milestone3, milestone2, milestone1), milestone1),
                Arguments.of(List.of(milestone1, milestone3, milestone2), milestone1),
                Arguments.of(List.of(milestone3, milestone1, milestone2), milestone1),
                Arguments.of(List.of(milestone2, milestone1, milestone3), milestone1)
        );
    }

    @Test
    void milestone_withMissingFields_shouldHaveViolations() {
        assertThat(validateModel(new Milestone())).isNotEmpty();
    }

    @Test
    void milestone_withValidFields_shouldHaveNoViolations() {
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName("Shipment Created");
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setOrganizationId(UUID.randomUUID().toString());

        assertThat(validateModel(milestone)).isEmpty();
    }

    @Test
    void milestone_codeDispatchScheduledNoRequiredFields_shouldHaveViolation() {
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DISPATCH_SCHEDULED);
        milestone.setMilestoneName("Dispatch Scheduled");

        Set<ConstraintViolation<Object>> violations = validateModel(milestone);
        assertThat(violations).hasSize(23);

        Set<String> violationsMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertThat(violationsMessages).contains("milestone_time must not be null",
                "user_id must not be null",
                "hub_id must not be null",
                "organisation_id must not be null",
                "from_location_id must not be null",
                "from_city_id must not be null",
                "from_state_id must not be null",
                "from_country_id must not be null",
                "to_location_id must not be null",
                "to_city_id must not be null",
                "to_state_id must not be null",
                "to_country_id must not be null",
                "milestone_coordinates must not be null",
                "eta must not be null",
                "shipment_tracking_id must not be null",
                "segment_id must not be null",
                "service_type must not be null",
                "partner_id must not be null",
                "job_type must not be null",
                "vehicle_id must not be null",
                "vehicle_type must not be null",
                "driver_id must not be null",
                "driver_name must not be null");
    }

    @Test
    void milestone_codeDispatchScheduledMultipleViolations_shouldHaveViolations() {
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(DSP_DISPATCH_SCHEDULED);
        milestone.setMilestoneName("Dispatch Scheduled");

        Set<ConstraintViolation<Object>> violations = validateModel(milestone);
        assertThat(violations).hasSize(23);
    }

    @ParameterizedTest
    @MethodSource("provideUnOrderedMilestoneAndExpectedOrder")
    void milestone_compareTo_shouldOrderByMilestoneTime(List<Milestone> milestoneList, Milestone topMilestone) {
        Set<Milestone> orderedMilestoneSet = new TreeSet<>(milestoneList);
        List<Milestone> orderedMilestoneList = new ArrayList<>(orderedMilestoneSet);
        assertThat(orderedMilestoneList.get(0)).isEqualTo(topMilestone);
    }
}
