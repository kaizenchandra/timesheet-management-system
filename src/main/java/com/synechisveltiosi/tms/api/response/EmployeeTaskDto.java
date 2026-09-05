package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.Project;
import com.synechisveltiosi.tms.model.entity.Task;

import java.util.UUID;

/** Task projection used by employees while creating timesheet entries. */
public record EmployeeTaskDto(Long id, long version, String name, String description,
                              boolean active, UUID projectId, String projectName) {
    public EmployeeTaskDto(Task task) {
        this(task.getId(), task.getVersion(), task.getName(), task.getDescription(), task.isActive(),
                projectId(task.getProject()), projectName(task.getProject()));
    }

    private static UUID projectId(Project project) {
        return project == null ? null : project.getId();
    }

    private static String projectName(Project project) {
        return project == null ? null : project.getName();
    }
}
