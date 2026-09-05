package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.ProjectLifecycleRequest;
import com.synechisveltiosi.tms.api.request.ProjectRequest;
import com.synechisveltiosi.tms.api.response.ProjectDto;
import com.synechisveltiosi.tms.service.ProjectService;
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
@RequestMapping(URLConstants.ProjectEndpoint.BASE)
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management APIs")
public class ProjectController {
    private final ProjectService projectService;

    /**
     * Creates a new project record.
     */
    @Operation(
            summary = "Create project",
            description = "Creates a new project with the provided details."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Project created successfully",
            content = @Content(schema = @Schema(implementation = ProjectDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    /**
     * Assigns an employee to a project.
     */
    @Operation(
            summary = "Assign employee to project",
            description = "Links an employee to the specified project and returns the updated project information."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee assigned to project successfully",
            content = @Content(schema = @Schema(implementation = ProjectDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PutMapping("/{projectId}/employees/{employeeId}")
    public ResponseEntity<ProjectDto> assignProject(
            @PathVariable("projectId")
            @Parameter(description = "Unique identifier of the project", required = true)
            UUID projectId,
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee to assign to the project", required = true)
            UUID employeeId) {
        return ResponseEntity.ok(projectService.assignProjectToEmployee(projectId, employeeId));
    }

    /**
     * Changes the active state of a project.
     */
    @Operation(
            summary = "Activate or deactivate project",
            description = "Updates the active status of a project based on the lifecycle request payload."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Project lifecycle updated successfully",
            content = @Content(schema = @Schema(implementation = ProjectDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PatchMapping("/{projectId}/active")
    public ResponseEntity<ProjectDto> changeProjectActive(
            @PathVariable("projectId")
            @Parameter(description = "Unique identifier of the project to update", required = true)
            UUID projectId,
            @Valid @RequestBody ProjectLifecycleRequest request) {
        return ResponseEntity.ok(projectService.changeProjectActive(projectId, request));
    }

    /**
     * Retrieves a paginated list of projects assigned to an employee.
     */
    @Operation(
            summary = "Get employee projects",
            description = "Returns all projects assigned to a specific employee in a paginated result set."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee projects retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectDto.class)))
    )
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<ProjectDto>> getEmployeeProjects(
            @PathVariable("employeeId")
            @Parameter(description = "Unique identifier of the employee whose projects are requested", required = true)
            UUID employeeId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsForEmployee(employeeId, pageable));
    }
}
