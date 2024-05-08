package com.quincus.networkmanagement.impl.parser.impl;

import com.quincus.networkmanagement.api.exception.InvalidRRuleException;
import com.quincus.networkmanagement.impl.parser.RRuleParser;
import lombok.extern.slf4j.Slf4j;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Component
@Slf4j
public class RRuleParserImpl implements RRuleParser {
    public static final String RRULE_PREFIX = "RRULE:";

    private static final int WEEKS_WORTH_OF_DATA = 12;

    private static final String ERR_INVALID_RRULE = "Failed to read RRULE string: `%s`";

    @Override
    public RecurrenceRule toRecurrenceRule(String schedule) {
        try {
            RecurrenceRule rule = new RecurrenceRule(schedule.replace(RRULE_PREFIX, ""));
            // ignore BYSECOND property
            rule.setByPart(RecurrenceRule.Part.BYSECOND, 0);
            return rule;
        } catch (InvalidRecurrenceRuleException e) {
            throw new InvalidRRuleException(String.format(ERR_INVALID_RRULE, schedule));
        }
    }

    /**
     * Generates schedules between now and x weeks from now
     */
    @Override
    public List<DateTime> generateTimingsFromSchedules(List<String> schedules, TimeZone timeZone, Long dateOfTraining) {

        List<DateTime> timings = new ArrayList<>();

        List<RecurrenceRule> rules = schedules.stream().map(this::toRecurrenceRule).toList();

        Duration duration = new Duration(1, WEEKS_WORTH_OF_DATA);

        DateTime start = dateOfTraining != null ? new DateTime(dateOfTraining) : DateTime.now();
        DateTime end = start.addDuration(duration);

        log.debug("Generating schedules from {} to {}", start.getTimestamp(), end.getTimestamp());

        rules.forEach(
                r -> {
                    log.debug("Generating schedules for recurrence: {}", r);
                    log.debug("Generating timings using timezone: {}", timeZone.getID());

                    RecurrenceRuleIterator it = r.iterator(start);
                    DateTime nextInstance = it.nextDateTime().swapTimeZone(timeZone);

                    while (nextInstance.before(end)) {
                        timings.add(nextInstance);
                        log.debug("Next instance converted to UTC: {}", nextInstance.getTimestamp());

                        nextInstance = it.nextDateTime().swapTimeZone(timeZone);
                    }
                }
        );

        return timings;
    }
}
