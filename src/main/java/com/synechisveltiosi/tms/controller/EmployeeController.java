package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.ManagerAssignmentRequest;
import com.synechisveltiosi.tms.api.response.EmployeeHierarchyDto;
import com.synechisveltiosi.tms.service.EmployeeService;
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

@RestController
@RequestMapping(URLConstants.EmployeeEndpoint.BASE)
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PutMapping("/{employeeId}/manager/{managerId}")
    public ResponseEntity<EmployeeHierarchyDto> assignManager(
            @PathVariable UUID employeeId,
            @PathVariable UUID managerId,
            @Valid @RequestBody ManagerAssignmentRequest request) {
        return ResponseEntity.ok(employeeService.assignManager(employeeId, managerId, request));
    }

    @GetMapping("/{managerId}/subordinates")
    public ResponseEntity<Page<EmployeeHierarchyDto>> getDirectReports(
            @PathVariable UUID managerId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(employeeService.getDirectReports(managerId, pageable));
    }
}
