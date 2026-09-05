package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.model.entity.Task;
import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.response.EmployeeTaskDto;
import com.synechisveltiosi.tms.api.request.TaskRequest;
import com.synechisveltiosi.tms.api.request.TaskLifecycleRequest;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Project;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.ProjectRepository;
import com.synechisveltiosi.tms.repository.TaskRepository;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public Task getTaskById(Long id) {
        return getTasksByIds(java.util.List.of(id)).get(id);
    }

    public Map<Long, Task> getTasksByIds(Collection<Long> taskIds) {
        Map<Long, Task> tasksById = taskRepository.findAllById(taskIds).stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));
        if (tasksById.size() != taskIds.stream().distinct().count()
                || tasksById.values().stream().anyMatch(task -> !task.isActive()
                || task.getProject() == null || !task.getProject().isActive())) {
            throw new com.synechisveltiosi.tms.api.exception.ResourceNotFoundException("One or more tasks do not exist or are archived");
        }
        return tasksById;
    }

    @Transactional
    public EmployeeTaskDto createTask(TaskRequest request) {
        String name = request.name().trim();
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));
        if (!project.isActive()) {
            throw new ResourceValidationException("Tasks cannot be created in an archived project");
        }
        if (taskRepository.existsByProjectIdAndNameIgnoreCase(project.getId(), name)) {
            throw new ResourceValidationException("A task already exists with this name in the project");
        }

        Set<UUID> requestedEmployeeIds = request.employeeIds() == null ? Set.of() : request.employeeIds();
        List<Employee> employees = employeeRepository.findAllById(requestedEmployeeIds);
        if (employees.size() != requestedEmployeeIds.size()) {
            throw new ResourceNotFoundException("One or more employees do not exist");
        }
        Task task = Task.builder()
                .name(name)
                .description(request.description() == null ? null : request.description().trim())
                .project(project)
                .build();
        employees.forEach(task::assignTo);
        return new EmployeeTaskDto(taskRepository.saveAndFlush(task));
    }

    /** Assigning an already assigned task is intentionally idempotent. */
    @Transactional
    public EmployeeTaskDto assignTaskToEmployee(Long taskId, UUID employeeId) {
        Task task = taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.isActive()) {
            throw new ResourceValidationException("Archived tasks cannot be assigned to employees");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        task.assignTo(employee);
        return new EmployeeTaskDto(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public EmployeeTaskDto changeTaskActive(Long taskId, TaskLifecycleRequest request) {
        Task task = taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (task.getVersion() != request.version()) {
            throw new com.synechisveltiosi.tms.api.exception.ResourceUpdateException(
                    "The task has changed. Refresh it before retrying your request");
        }
        task.changeActive(request.active());
        return new EmployeeTaskDto(taskRepository.saveAndFlush(task));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeTaskDto> getTasksForEmployee(UUID employeeId, Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return taskRepository.findByEmployeesIdAndActiveTrueAndProjectActiveTrue(employeeId, pageable)
                .map(EmployeeTaskDto::new);
    }
}
