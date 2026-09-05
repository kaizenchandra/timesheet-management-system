package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/** Creates a draft from an existing employee-owned timesheet snapshot. */
public record TimesheetCloneRequest(
        @NotNull(message = "Source timesheet version cannot be null")
        @PositiveOrZero(message = "Source timesheet version cannot be negative") Long sourceVersion,
        @NotNull(message = "Target start date cannot be null") LocalDate targetStartDate,
        @NotNull(message = "Target end date cannot be null") LocalDate targetEndDate) {
}
