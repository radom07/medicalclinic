package com.radom07.medicalclinic.validator;

import com.radom07.medicalclinic.command.CreateVisitCommand;
import com.radom07.medicalclinic.exception.InvalidVisitTimeException;

import java.time.LocalDateTime;

public final class TimeVisitValidator {

    private TimeVisitValidator() {}

    public static void validateNewVisit(CreateVisitCommand command, LocalDateTime now) {
        validateTimeInFuture(command.startTime(), now);
        validateQuarterHourFormat(command.startTime());
        validateQuarterHourFormat(command.endTime());
        validateStartTimeIsBeforeEndTime(command.startTime(), command.endTime());
    }

    public static void validateTimeInFuture(LocalDateTime time, LocalDateTime now) {
        if (time.isBefore(now)) {
            throw new InvalidVisitTimeException("Cannot create or book a visit in the past");
        }
    }

    private static void validateQuarterHourFormat(LocalDateTime time) {
        if (time.getMinute() % 15 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw new InvalidVisitTimeException("Time must be in full quarter-hour");
        }
    }

    private static void validateStartTimeIsBeforeEndTime(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new InvalidVisitTimeException("The end time must be after start time");
        }
    }
}
