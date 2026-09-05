package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Project name cannot be blank")
        @Size(max = 150, message = "Project name must not exceed 150 characters") String name,
        @Size(max = 1_000, message = "Project description must not exceed 1000 characters") String description) {
}
