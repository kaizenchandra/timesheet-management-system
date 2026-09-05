package com.synechisveltiosi.tms.api.request;

import com.synechisveltiosi.tms.model.enums.TimesheetEntryType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TimesheetRequest(
        @NotNull(message = "Start date cannot be null") LocalDate startDate,
        @NotNull(message = "End date cannot be null") LocalDate endDate,
        @NotEmpty(message = "Timesheet must contain at least one entry") @Valid List<TimesheetEntryRequest> entries
) {
    public record TimesheetEntryRequest(
            Long id,
            @NotNull(message = "Task ID cannot be null")
            Long taskId,
            @NotNull(message = "Entry type cannot be null")
            TimesheetEntryType entryType,
            @NotNull(message = "Entry date cannot be null")
            LocalDate date,
            @NotNull(message = "Hours cannot be null")
            @DecimalMin(value = "0", message = "Hours cannot be negative")
            @DecimalMax(value = "24", message = "Hours cannot be greater than 24")
            double hours) {
    }
}
