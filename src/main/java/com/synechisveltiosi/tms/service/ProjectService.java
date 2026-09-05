package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.request.ProjectRequest;
import com.synechisveltiosi.tms.api.request.ProjectLifecycleRequest;
import com.synechisveltiosi.tms.api.exception.ResourceUpdateException;
import com.synechisveltiosi.tms.api.response.ProjectDto;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Project;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ProjectDto createProject(ProjectRequest request) {
        String name = request.name().trim();
        if (projectRepository.existsByNameIgnoreCase(name)) {
            throw new ResourceValidationException("A project already exists with name: " + name);
        }
        Project project = Project.builder()
                .name(name)
                .description(request.description() == null ? null : request.description().trim())
                .build();
        return new ProjectDto(projectRepository.saveAndFlush(project));
    }

    @Transactional
    public ProjectDto assignProjectToEmployee(UUID projectId, UUID employeeId) {
        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        if (!project.isActive()) {
            throw new ResourceValidationException("Archived projects cannot be assigned to employees");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        project.assignTo(employee);
        return new ProjectDto(projectRepository.saveAndFlush(project));
    }

    @Transactional
    public ProjectDto changeProjectActive(UUID projectId, ProjectLifecycleRequest request) {
        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        if (project.getVersion() != request.version()) {
            throw new ResourceUpdateException("The project has changed. Refresh it before retrying your request");
        }
        project.changeActive(request.active());
        return new ProjectDto(projectRepository.saveAndFlush(project));
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsForEmployee(UUID employeeId, Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return projectRepository.findByEmployeesIdAndActiveTrue(employeeId, pageable).map(ProjectDto::new);
    }
}
