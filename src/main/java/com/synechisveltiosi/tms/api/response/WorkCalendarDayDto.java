package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.enums.LeaveType;

import java.time.LocalDate;

/** Daily availability model that preserves overlapping calendar facts instead of choosing one label. */
public record WorkCalendarDayDto(
        LocalDate date,
        boolean weekend,
        String holidayName,
        LeaveType approvedLeaveType,
        boolean workingDay) {
}
