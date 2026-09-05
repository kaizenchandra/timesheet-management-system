package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProjectLifecycleRequest(
        @NotNull(message = "Project version cannot be null")
        @PositiveOrZero(message = "Project version cannot be negative") Long version,
        boolean active) {
}
