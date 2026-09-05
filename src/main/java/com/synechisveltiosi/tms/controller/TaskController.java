package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.TaskLifecycleRequest;
import com.synechisveltiosi.tms.api.request.TaskRequest;
import com.synechisveltiosi.tms.api.response.EmployeeTaskDto;
import com.synechisveltiosi.tms.service.TaskService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;

@RestController
@RequestMapping(URLConstants.TaskEndpoint.BASE)
@RequiredArgsConstructor
@Tag(name = "Task", description = "Task and employee assignment APIs")
public class TaskController {
    private final TaskService taskService;

    /**
     * Creates a new task.
     */
    @Operation(
            summary = "Create task",
            description = "Creates a new task with the provided configuration and metadata."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Task created successfully",
            content = @Content(schema = @Schema(implementation = EmployeeTaskDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @PostMapping
    public ResponseEntity<EmployeeTaskDto> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    /**
     * Assigns a task to an employee.
     */
    @Operation(
            summary = "Assign task to employee",
            description = "Links a task to a specific employee and returns the updated task record."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Task assigned successfully",
            content = @Content(schema = @Schema(implementation = EmployeeTaskDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PutMapping("/{taskId}/employees/{employeeId}")
    public ResponseEntity<EmployeeTaskDto> assignTask(
            @PathVariable("taskId")
            @Parameter(description = "Unique identifier of the task to assign", required = true)
            Long taskId,
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee receiving the task", required = true)
            UUID employeeId) {
        return ResponseEntity.ok(taskService.assignTaskToEmployee(taskId, employeeId));
    }

    /**
     * Activates or deactivates a task.
     */
    @Operation(
            summary = "Update task active status",
            description = "Changes the active or inactive state of a task based on the lifecycle payload."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Task status updated successfully",
            content = @Content(schema = @Schema(implementation = EmployeeTaskDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PatchMapping("/{taskId}/active")
    public ResponseEntity<EmployeeTaskDto> changeTaskActive(
            @PathVariable("taskId")
            @Parameter(description = "Unique identifier of the task to update", required = true)
            Long taskId,
            @Valid @RequestBody TaskLifecycleRequest request) {
        return ResponseEntity.ok(taskService.changeTaskActive(taskId, request));
    }

    /**
     * Retrieves a paginated list of tasks for an employee.
     */
    @Operation(
            summary = "Get employee tasks",
            description = "Returns all tasks assigned to a specific employee in a paginated result set."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tasks retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeTaskDto.class)))
    )
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<EmployeeTaskDto>> getEmployeeTasks(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee whose tasks are requested", required = true)
            UUID employeeId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksForEmployee(employeeId, pageable));
    }
}
