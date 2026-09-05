package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Version observed before changing an employee's reporting manager. */
public record ManagerAssignmentRequest(
        @NotNull(message = "Employee version cannot be null")
        @PositiveOrZero(message = "Employee version cannot be negative") Long version) {
}
