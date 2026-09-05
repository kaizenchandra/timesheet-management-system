package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HolidayRequest(
        @NotBlank(message = "Holiday name cannot be blank")
        @Size(max = 150, message = "Holiday name must not exceed 150 characters") String name,
        @Size(max = 1_000, message = "Holiday description must not exceed 1000 characters") String description,
        @NotNull(message = "Holiday date cannot be null") LocalDate date) {
}
