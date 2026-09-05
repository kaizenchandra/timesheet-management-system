package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.ProjectRequest;
import com.synechisveltiosi.tms.api.request.ProjectLifecycleRequest;
import com.synechisveltiosi.tms.api.response.ProjectDto;
import com.synechisveltiosi.tms.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(URLConstants.ProjectEndpoint.BASE)
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PutMapping("/{projectId}/employees/{employeeId}")
    public ResponseEntity<ProjectDto> assignProject(@PathVariable UUID projectId, @PathVariable UUID employeeId) {
        return ResponseEntity.ok(projectService.assignProjectToEmployee(projectId, employeeId));
    }

    @PatchMapping("/{projectId}/active")
    public ResponseEntity<ProjectDto> changeProjectActive(@PathVariable UUID projectId,
                                                           @Valid @RequestBody ProjectLifecycleRequest request) {
        return ResponseEntity.ok(projectService.changeProjectActive(projectId, request));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<ProjectDto>> getEmployeeProjects(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsForEmployee(employeeId, pageable));
    }
}
