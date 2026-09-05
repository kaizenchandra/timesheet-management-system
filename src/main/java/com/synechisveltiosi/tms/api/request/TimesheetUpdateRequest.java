package com.synechisveltiosi.tms.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * Replaces the entries of a draft or rejected timesheet. The version prevents a stale client from
 * overwriting a concurrent edit.
 */
public record TimesheetUpdateRequest(
        @NotNull(message = "Timesheet version cannot be null")
        @PositiveOrZero(message = "Timesheet version cannot be negative") Long version,
        @NotEmpty(message = "Timesheet must contain at least one entry")
        @Valid List<TimesheetRequest.TimesheetEntryRequest> entries) {
}
