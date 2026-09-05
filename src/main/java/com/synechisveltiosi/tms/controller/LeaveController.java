package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.LeaveCancellationRequest;
import com.synechisveltiosi.tms.api.request.LeaveDecisionRequest;
import com.synechisveltiosi.tms.api.request.LeaveRequest;
import com.synechisveltiosi.tms.api.response.LeaveDto;
import com.synechisveltiosi.tms.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;

@RestController
@RequestMapping(URLConstants.LeaveEndpoint.BASE)
@RequiredArgsConstructor
@Tag(name = "Leave", description = "Leave application and approval APIs")
public class LeaveController {
    private final LeaveService leaveService;

    /**
     * Submits a leave request for an employee.
     */
    @Operation(
            summary = "Request leave",
            description = "Creates a new leave request for the specified employee."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Leave request created successfully",
            content = @Content(schema = @Schema(implementation = LeaveDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PostMapping(URLConstants.LeaveEndpoint.BY_EMPLOYEE)
    public ResponseEntity<LeaveDto> requestLeave(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee requesting leave", required = true)
            UUID employeeId,
            @Valid @RequestBody LeaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.requestLeave(employeeId, request));
    }

    /**
     * Retrieves all leave entries for an employee.
     */
    @Operation(
            summary = "Get employee leave requests",
            description = "Returns all leave requests submitted by the specified employee."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leaves retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaveDto.class)))
    )
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping(URLConstants.LeaveEndpoint.BY_EMPLOYEE)
    public ResponseEntity<List<LeaveDto>> getLeavesForEmployee(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee whose leave requests are requested", required = true)
            UUID employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesForEmployee(employeeId));
    }

    /**
     * Cancels a leave request submitted by an employee.
     */
    @Operation(
            summary = "Cancel leave request",
            description = "Allows an employee to cancel an existing leave request before manager approval."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave request cancelled successfully",
            content = @Content(schema = @Schema(implementation = LeaveDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PostMapping(URLConstants.LeaveEndpoint.CANCEL_BY_LEAVE_ID_EMPLOYEE)
    public ResponseEntity<LeaveDto> cancelLeave(
            @PathVariable("leaveId")
            @Parameter(description = "Unique identifier of the leave request to cancel", required = true)
            UUID leaveId,
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee cancelling the leave request", required = true)
            UUID employeeId,
            @Valid @RequestBody LeaveCancellationRequest request) {
        return ResponseEntity.ok(leaveService.cancelLeave(leaveId, employeeId, request));
    }

    /**
     * Approves or rejects a leave request by manager decision.
     */
    @Operation(
            summary = "Decide on leave request",
            description = "Allows a manager to approve or reject a leave request and returns the updated leave record."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave request decision recorded successfully",
            content = @Content(schema = @Schema(implementation = LeaveDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PostMapping(URLConstants.LeaveEndpoint.DECIDE_BY_LEAVE_ID_MANAGER)
    public ResponseEntity<LeaveDto> decideLeave(
            @PathVariable("leaveId")
            @Parameter(description = "Unique identifier of the leave request to decide on", required = true)
            UUID leaveId,
            @PathVariable("managerId")
            @Parameter(description = "Unique identifier of the manager deciding the leave request", required = true)
            UUID managerId,
            @Valid @RequestBody LeaveDecisionRequest request) {
        return ResponseEntity.ok(leaveService.decideLeave(leaveId, managerId, request));
    }
}
