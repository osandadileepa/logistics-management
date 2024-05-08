package com.quincus.shipment.impl.helper.journey;

import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageJourneySegmentPickUpDropOffTimeCalculatorTest {

    private final PackageJourneySegmentPickUpDropOffTimeCalculator packageJourneySegmentPickupDropOffTimeCalculator = new PackageJourneySegmentPickUpDropOffTimeCalculator();

    @Test
    void givenOrderWithPickUpStartDateAndListOfSegmentsWithDurations_whenCalculatePickupAndDropOffTime_ThenComputeAndAssignPickUpAndDropOffTimeOfSegments() {

        Root root = new Root();
        root.setPickupStartTime("2023-05-23 00:00:00 GMT-07:00");

        List<PackageJourneySegment> segmentList = new ArrayList<>();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setDuration(BigDecimal.valueOf(10));
        segment1.setDurationUnit(UnitOfMeasure.HOUR);
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setDuration(BigDecimal.valueOf(500));
        segment2.setDurationUnit(UnitOfMeasure.MINUTE);
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setDuration(BigDecimal.valueOf(2));
        segment3.setDurationUnit(UnitOfMeasure.HOUR);
        PackageJourneySegment segment4 = new PackageJourneySegment();
        segment4.setDuration(BigDecimal.valueOf(300));
        segment4.setDurationUnit(UnitOfMeasure.MINUTE);
        segmentList.add(segment1);
        segmentList.add(segment2);
        segmentList.add(segment3);
        segmentList.add(segment4);

        packageJourneySegmentPickupDropOffTimeCalculator.computeAndAssignPickUpAndDropOffTime(segmentList, root);
        assertThat(segment1.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T00:00:00-07:00");
        assertThat(segment1.getDropOffTime()).isNotNull().isEqualTo("2023-05-23T10:00:00-07:00");
        assertThat(segment2.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T10:00:00-07:00");
        assertThat(segment2.getDropOffTime()).isNotNull().isEqualTo("2023-05-23T18:20:00-07:00");
        assertThat(segment3.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T18:20:00-07:00");
        assertThat(segment3.getDropOffTime()).isNotNull().isEqualTo("2023-05-23T20:20:00-07:00");
        assertThat(segment4.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T20:20:00-07:00");
        assertThat(segment4.getDropOffTime()).isNotNull().isEqualTo("2023-05-24T01:20:00-07:00");
    }

    @Test
    void givenOrderWithPickUpStartDateAndListOfSegmentsWithOutDuration_whenCalculatePickupAndDropOffTime_ThenPickupAndStartDateWillBeNullExpectForFirstSegmentPickUpTime() {

        Root root = new Root();
        root.setPickupStartTime("2023-05-23 00:00:00 GMT-07:00");

        List<PackageJourneySegment> segmentList = new ArrayList<>();
        segmentList.add(new PackageJourneySegment());
        segmentList.add(new PackageJourneySegment());
        segmentList.add(new PackageJourneySegment());
        segmentList.add(new PackageJourneySegment());

        packageJourneySegmentPickupDropOffTimeCalculator.computeAndAssignPickUpAndDropOffTime(segmentList, root);
        assertThat(segmentList.get(0).getPickUpTime()).isNotBlank().isEqualTo("2023-05-23T00:00:00-07:00");
        assertThat(segmentList.get(0).getDropOffTime()).isBlank();
        assertThat(segmentList.get(1).getPickUpTime()).isBlank();
        assertThat(segmentList.get(1).getDropOffTime()).isBlank();
        assertThat(segmentList.get(2).getPickUpTime()).isBlank();
        assertThat(segmentList.get(2).getDropOffTime()).isBlank();
        assertThat(segmentList.get(3).getPickUpTime()).isBlank();
        assertThat(segmentList.get(3).getDropOffTime()).isBlank();
    }

    @Test
    void givenOrderWithNoPickUpStartTimeAndListOfSegmentsWithDurations_whenCalculatePickupAndDropOffTime_ThenAllWillHaveNullValue() {

        Root root = new Root();

        List<PackageJourneySegment> segmentList = new ArrayList<>();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setDuration(BigDecimal.valueOf(10));
        segment1.setDurationUnit(UnitOfMeasure.HOUR);
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setDuration(BigDecimal.valueOf(500));
        segment2.setDurationUnit(UnitOfMeasure.MINUTE);
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setDuration(BigDecimal.valueOf(2));
        segment3.setDurationUnit(UnitOfMeasure.HOUR);
        PackageJourneySegment segment4 = new PackageJourneySegment();
        segment4.setDuration(BigDecimal.valueOf(300));
        segment4.setDurationUnit(UnitOfMeasure.MINUTE);
        segmentList.add(segment1);
        segmentList.add(segment2);
        segmentList.add(segment3);
        segmentList.add(segment4);

        packageJourneySegmentPickupDropOffTimeCalculator.computeAndAssignPickUpAndDropOffTime(segmentList, root);
        assertThat(segment1.getPickUpTime()).isNull();
        assertThat(segment1.getDropOffTime()).isNull();
        assertThat(segment2.getPickUpTime()).isNull();
        assertThat(segment2.getDropOffTime()).isNull();
        assertThat(segment3.getPickUpTime()).isNull();
        assertThat(segment3.getDropOffTime()).isNull();
        assertThat(segment4.getPickUpTime()).isNull();
        assertThat(segment4.getDropOffTime()).isNull();
    }

    @Test
    void givenOrderWithPickUpStartTimeAndListOfSegmentsWithOutDurationsOnTheMiddle_whenCalculatePickupAndDropOffTime_ThenNullPickUpAndDropOffTimeStartingFromNoDurationSegment() {

        Root root = new Root();
        root.setPickupStartTime("2023-05-23 00:00:00 GMT-07:00");

        List<PackageJourneySegment> segmentList = new ArrayList<>();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setDuration(BigDecimal.valueOf(10));
        segment1.setDurationUnit(UnitOfMeasure.HOUR);
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setDuration(BigDecimal.valueOf(500));
        segment2.setDurationUnit(UnitOfMeasure.MINUTE);
        PackageJourneySegment segment3 = new PackageJourneySegment();
        PackageJourneySegment segment4 = new PackageJourneySegment();
        segment4.setDuration(BigDecimal.valueOf(300));
        segment4.setDurationUnit(UnitOfMeasure.MINUTE);
        segmentList.add(segment1);
        segmentList.add(segment2);
        segmentList.add(segment3);
        segmentList.add(segment4);

        packageJourneySegmentPickupDropOffTimeCalculator.computeAndAssignPickUpAndDropOffTime(segmentList, root);
        assertThat(segment1.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T00:00:00-07:00");
        assertThat(segment1.getDropOffTime()).isNotNull().isEqualTo("2023-05-23T10:00:00-07:00");
        assertThat(segment2.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T10:00:00-07:00");
        assertThat(segment2.getDropOffTime()).isNotNull().isEqualTo("2023-05-23T18:20:00-07:00");
        assertThat(segment3.getPickUpTime()).isNotNull().isEqualTo("2023-05-23T18:20:00-07:00");
        assertThat(segment3.getDropOffTime()).isNull();
        assertThat(segment4.getPickUpTime()).isNull();
        assertThat(segment4.getDropOffTime()).isNull();
    }

}
