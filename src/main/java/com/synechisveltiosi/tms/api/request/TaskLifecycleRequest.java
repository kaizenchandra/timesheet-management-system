package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TaskLifecycleRequest(
        @NotNull(message = "Task version cannot be null")
        @PositiveOrZero(message = "Task version cannot be negative") Long version,
        boolean active) {
}
