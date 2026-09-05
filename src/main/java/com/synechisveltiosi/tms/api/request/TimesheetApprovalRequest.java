package com.synechisveltiosi.tms.api.request;

import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TimesheetApprovalRequest(
        @Size(max = 1_000, message = "Comments must not exceed 1000 characters") String comments,
        @NotNull(message = "Approval status cannot be null") TimesheetStatus status) {
}
