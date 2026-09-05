package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Contains the aggregate version observed by the employee before submission. */
public record TimesheetSubmissionRequest(
        @NotNull(message = "Timesheet version cannot be null")
        @PositiveOrZero(message = "Timesheet version cannot be negative") Long version) {
}
