package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.response.EmployeeTaskDto;
import com.synechisveltiosi.tms.api.request.TaskRequest;
import com.synechisveltiosi.tms.api.request.TaskLifecycleRequest;
import com.synechisveltiosi.tms.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(URLConstants.TaskEndpoint.BASE)
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<EmployeeTaskDto> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PutMapping("/{taskId}/employees/{employeeId}")
    public ResponseEntity<EmployeeTaskDto> assignTask(@PathVariable Long taskId, @PathVariable UUID employeeId) {
        return ResponseEntity.ok(taskService.assignTaskToEmployee(taskId, employeeId));
    }

    @PatchMapping("/{taskId}/active")
    public ResponseEntity<EmployeeTaskDto> changeTaskActive(@PathVariable Long taskId,
                                                             @Valid @RequestBody TaskLifecycleRequest request) {
        return ResponseEntity.ok(taskService.changeTaskActive(taskId, request));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<EmployeeTaskDto>> getEmployeeTasks(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksForEmployee(employeeId, pageable));
    }
}
