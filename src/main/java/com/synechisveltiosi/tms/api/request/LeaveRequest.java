package com.synechisveltiosi.tms.api.request;

import com.synechisveltiosi.tms.model.enums.LeaveType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequest(
        @NotNull(message = "Start date cannot be null") LocalDate startDate,
        @NotNull(message = "End date cannot be null") LocalDate endDate,
        @NotNull(message = "Leave type cannot be null") LeaveType type,
        @NotBlank(message = "Leave reason cannot be blank")
        @Size(max = 1_000, message = "Leave reason must not exceed 1000 characters") String reason,
        @DecimalMin(value = "0.25", message = "Leave hours must be at least 0.25") double hours) {
}
