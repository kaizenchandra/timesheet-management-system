package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.ManagerAssignmentRequest;
import com.synechisveltiosi.tms.api.response.EmployeeHierarchyDto;
import com.synechisveltiosi.tms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;

@RestController
@RequestMapping(URLConstants.EmployeeEndpoint.BASE)
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee and manager hierarchy APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    /**
     * Assigns a manager to an employee and returns the updated hierarchy details.
     */
    @Operation(
            summary = "Assign a manager to an employee",
            description = "Updates the employee's reporting manager and returns the refreshed employee hierarchy representation."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Manager assignment updated successfully",
            content = @Content(schema = @Schema(implementation = EmployeeHierarchyDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PutMapping("/{employeeId}/manager/{managerId}")
    public ResponseEntity<EmployeeHierarchyDto> assignManager(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee being reassigned", required = true)
            UUID employeeId,
            @PathVariable("managerId")
            @Parameter(description = "Unique identifier of the manager to assign", required = true)
            UUID managerId,
            @Valid @RequestBody ManagerAssignmentRequest request) {
        return ResponseEntity.ok(employeeService.assignManager(employeeId, managerId, request));
    }

    /**
     * Returns a paginated list of employees who report directly to the specified manager.
     */
    @Operation(
            summary = "Get direct reports for a manager",
            description = "Retrieves the paginated list of employees reporting directly to the provided manager."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Direct reports retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeHierarchyDto.class)))
    )
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping("/{managerId}/subordinates")
    public ResponseEntity<Page<EmployeeHierarchyDto>> getDirectReports(
            @PathVariable("managerId")
            @Parameter(description = "Unique identifier of the manager whose direct reports are requested", required = true)
            UUID managerId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(employeeService.getDirectReports(managerId, pageable));
    }
}
