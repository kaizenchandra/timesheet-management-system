package com.synechisveltiosi.tms.api.response;

import java.time.LocalDate;

/** Aggregate-only report model; individual timesheet entries are never loaded for this query. */
public record TimesheetHoursSummaryDto(
        LocalDate startDate,
        LocalDate endDate,
        Double totalHours,
        Double billableHours,
        Double nonBillableHours,
        Double overtimeHours,
        Long entryCount) {
}
