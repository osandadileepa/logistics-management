package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.NetworkLaneSegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NetworkLaneDurationCalculatorTest {

    @InjectMocks
    private NetworkLaneDurationCalculator networkLaneDurationCalculator;

    @ParameterizedTest
    @MethodSource("provideDatesForAirSegment")
    @DisplayName("Given NetworkLane Air Segment With Date Arguments when CalculateAndSetNetworkLaneSegmentDuration Then SegmentShouldHaveCorrectCalculatedDuration")
    void testCalculateNetworkLaneAirSegmentDuration(String arrivalTime, String departureTime, String recoveryTime, String lockoutTime, UnitOfMeasure durationUnit, BigDecimal expectedResult) {
        //GIVEN:
        NetworkLaneSegment networkLaneSegment = new NetworkLaneSegment();
        networkLaneSegment.setDurationUnit(durationUnit);
        networkLaneSegment.setTransportType(TransportType.AIR);
        networkLaneSegment.setRecoveryTime(recoveryTime);
        networkLaneSegment.setLockOutTime(lockoutTime);
        networkLaneSegment.setDepartureTime(departureTime);
        networkLaneSegment.setArrivalTime(arrivalTime);
        //WHEN:
        BigDecimal duration = networkLaneDurationCalculator.calculateNetworkLaneSegmentDuration(networkLaneSegment);
        //THEN:
        assertThat(duration).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideDatesForGroundSegment")
    @DisplayName("Given NetworkLane Ground Segment With Date Arguments when CalculateAndSetNetworkLaneSegmentDuration Then SegmentShouldHaveCorrectCalculatedDuration")
    void testCalculateNetworkLaneGroundSegmentDuration(String pickUpTime, String dropOffTime, UnitOfMeasure durationUnit, BigDecimal expectedResult) {
        //GIVEN:
        NetworkLaneSegment networkLaneSegment = new NetworkLaneSegment();
        networkLaneSegment.setDurationUnit(durationUnit);
        networkLaneSegment.setTransportType(TransportType.GROUND);
        networkLaneSegment.setPickUpTime(pickUpTime);
        networkLaneSegment.setDropOffTime(dropOffTime);

        //WHEN:
        BigDecimal duration = networkLaneDurationCalculator.calculateNetworkLaneSegmentDuration(networkLaneSegment);
        //THEN:
        assertThat(duration).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> provideDatesForAirSegment() {
        return Stream.of(
                Arguments.of("2022-12-10T11:21:02+07:00", "2022-12-10T12:27:02+07:00", "2022-12-10T13:27:02+07:00", "2022-12-10T14:27:02+07:00", UnitOfMeasure.MINUTE, BigDecimal.valueOf(186))
                , Arguments.of("2022-12-10T11:21:02+07:00", "2022-12-10T12:27:02+07:00", "2022-12-10T13:27:02+07:00", "2022-12-10T14:27:02+07:00", UnitOfMeasure.HOUR, BigDecimal.valueOf(3))
                , Arguments.of("", "", null, null, UnitOfMeasure.HOUR, BigDecimal.valueOf(0))
        );
    }

    private static Stream<Arguments> provideDatesForGroundSegment() {
        return Stream.of(
                Arguments.of("2022-12-10T11:21:02+07:00", "2022-12-10T12:27:02+07:00", UnitOfMeasure.MINUTE, BigDecimal.valueOf(66))
                , Arguments.of(null, null, UnitOfMeasure.HOUR, BigDecimal.valueOf(0))
                , Arguments.of("2022-12-10T11:xxx:02+07:00", "2022-12-xxx:27:02+07:00", UnitOfMeasure.HOUR, BigDecimal.valueOf(0))
                , Arguments.of("", "", UnitOfMeasure.HOUR, BigDecimal.valueOf(0))
        );
    }
}
