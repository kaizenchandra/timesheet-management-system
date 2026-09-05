package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.employee.EmployeeNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceUpdateException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.request.ManagerAssignmentRequest;
import com.synechisveltiosi.tms.api.response.EmployeeHierarchyDto;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Employee getEmployeeById(UUID uuid) {
        return employeeRepository.findById(uuid)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + uuid));
    }

    public List<Employee> getAllEmployee() {
        return employeeRepository.findAll();
    }

    @Transactional
    public EmployeeHierarchyDto assignManager(UUID employeeId, UUID managerId, ManagerAssignmentRequest request) {
        Employee employee = employeeRepository.findByIdForUpdate(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        if (employee.getVersion() != request.version()) {
            throw new ResourceUpdateException("The employee has changed. Refresh it before retrying your request");
        }
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new EmployeeNotFoundException("Manager not found with id: " + managerId));
        validateManagerAssignment(employee, manager);
        employee.assignManager(manager);
        return new EmployeeHierarchyDto(employeeRepository.saveAndFlush(employee));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeHierarchyDto> getDirectReports(UUID managerId, Pageable pageable) {
        if (!employeeRepository.existsById(managerId)) {
            throw new EmployeeNotFoundException("Manager not found with id: " + managerId);
        }
        return employeeRepository.findByManagerId(managerId, pageable).map(EmployeeHierarchyDto::new);
    }

    private void validateManagerAssignment(Employee employee, Employee manager) {
        if (employee.getId().equals(manager.getId())) {
            throw new ResourceValidationException("An employee cannot be their own manager");
        }
        Set<UUID> visited = new HashSet<>();
        Employee currentManager = manager;
        while (currentManager != null) {
            if (!visited.add(currentManager.getId()) || currentManager.getId().equals(employee.getId())) {
                throw new ResourceValidationException("The manager assignment would create a reporting cycle");
            }
            currentManager = currentManager.getManager();
        }
    }
}
