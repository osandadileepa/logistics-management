package com.quincus.networkmanagement.impl.parser;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.List;
import java.util.TimeZone;

public interface RRuleParser {
    RecurrenceRule toRecurrenceRule(String schedule);

    List<DateTime> generateTimingsFromSchedules(List<String> schedules, TimeZone timeZone, Long dateOfTraining);
}
