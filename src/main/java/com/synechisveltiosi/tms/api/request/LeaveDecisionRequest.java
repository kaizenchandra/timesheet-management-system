package com.synechisveltiosi.tms.api.request;

import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record LeaveDecisionRequest(
        @NotNull(message = "Leave version cannot be null")
        @PositiveOrZero(message = "Leave version cannot be negative") Long version,
        @NotNull(message = "Leave decision cannot be null") LeaveStatus status,
        @Size(max = 1_000, message = "Decision comments must not exceed 1000 characters") String comments) {
}
