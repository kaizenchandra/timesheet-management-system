package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record LeaveCancellationRequest(
        @NotNull(message = "Leave version cannot be null")
        @PositiveOrZero(message = "Leave version cannot be negative") Long version,
        @Size(max = 1_000, message = "Cancellation comments must not exceed 1000 characters") String comments) {
}
