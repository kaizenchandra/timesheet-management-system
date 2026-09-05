package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record TaskRequest(
        @NotBlank(message = "Task name cannot be blank")
        @Size(max = 150, message = "Task name must not exceed 150 characters") String name,
        @Size(max = 1_000, message = "Task description must not exceed 1000 characters") String description,
        @NotNull(message = "Project ID cannot be null") UUID projectId,
        @Size(max = 100, message = "A task cannot be assigned to more than 100 employees at once")
        Set<UUID> employeeIds) {
}
