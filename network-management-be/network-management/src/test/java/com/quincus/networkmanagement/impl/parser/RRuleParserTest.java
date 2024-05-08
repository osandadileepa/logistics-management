package com.quincus.networkmanagement.impl.parser;

import com.quincus.networkmanagement.api.exception.InvalidRRuleException;
import com.quincus.networkmanagement.impl.parser.impl.RRuleParserImpl;
import org.dmfs.rfc5545.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RRuleParserTest {

    RRuleParser parser = new RRuleParserImpl();

    @Test
    @DisplayName("GIVEN mon/wed/fri recurring schedule WHEN generate schedules THEN return expected")
    void returnExpectedWeekdayTimings() {
        List<DateTime> timings = parser.generateTimingsFromSchedules(
                List.of(
                        "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,FR;BYHOUR=10;BYMINUTE=30",
                        "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=WE;BYHOUR=9;BYMINUTE=25"
                ),
                TimeZone.getTimeZone("UTC"),
                1577836800L
        );

        // given we are generating 12 weeks worth from a MO/WE/FR schedule, there should be 3*12 timings in total
        assertThat(timings).hasSize(36);

        // ensure generated timings are correct
        assertThat(timings.get(0).getTimestamp()).isEqualTo(1593000000);
        assertThat(timings.get(1).getTimestamp()).isEqualTo(1938600000);
        assertThat(timings.get(2).getTimestamp()).isEqualTo(2197800000L);
        assertThat(timings.get(3).getTimestamp()).isEqualTo(2543400000L);
        assertThat(timings.get(4).getTimestamp()).isEqualTo(2802600000L);
        assertThat(timings.get(5).getTimestamp()).isEqualTo(3148200000L);
        assertThat(timings.get(24).getTimestamp()).isEqualTo(1761900000);
        assertThat(timings.get(25).getTimestamp()).isEqualTo(2366700000L);
        assertThat(timings.get(26).getTimestamp()).isEqualTo(2971500000L);

    }

    @Test
    @DisplayName("GIVEN sat/sun recurring schedule WHEN generate schedules THEN return expected")
    void returnExpectedWeekendTimings() {
        List<DateTime> timings = parser.generateTimingsFromSchedules(
                List.of(
                        "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=SA,SU;BYHOUR=16;BYMINUTE=30"
                ),
                TimeZone.getTimeZone("Asia/Tokyo"),
                1577836800L
        );

        // given we are generating 12 weeks worth from a SA/SU schedule, there should be 2*12 timings in total
        assertThat(timings).hasSize(24);

        // ensure generated timings are correct
        assertThat(timings.get(0).getTimestamp()).isEqualTo(2014200000);
        assertThat(timings.get(1).getTimestamp()).isEqualTo(2100600000);
        assertThat(timings.get(2).getTimestamp()).isEqualTo(2619000000L);
        assertThat(timings.get(3).getTimestamp()).isEqualTo(2705400000L);
        assertThat(timings.get(4).getTimestamp()).isEqualTo(3223800000L);
        assertThat(timings.get(5).getTimestamp()).isEqualTo(3310200000L);

    }

    @Test
    @DisplayName("GIVEN invalid RRULE WHEN generate schedules THEN throw error")
    void returnErrorWhenInvalidSchedule() {
        assertThatThrownBy(() -> parser.toRecurrenceRule("RRULE:INVALID_RRULE"))
                .isInstanceOf(InvalidRRuleException.class)
                .hasMessage("Failed to read RRULE string: `RRULE:INVALID_RRULE`");
    }
}
