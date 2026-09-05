package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Withdraws a submitted timesheet before a manager makes a decision. */
public record TimesheetWithdrawalRequest(
        @NotNull(message = "Timesheet version cannot be null")
        @PositiveOrZero(message = "Timesheet version cannot be negative") Long version,
        @Size(max = 1_000, message = "Withdrawal comments must not exceed 1000 characters") String comments) {
}
