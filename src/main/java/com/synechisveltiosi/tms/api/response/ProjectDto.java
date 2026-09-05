package com.synechisveltiosi.tms.api.response;

import java.io.Serializable;
import java.util.UUID;
import com.synechisveltiosi.tms.model.entity.Project;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Project}
 */
public record ProjectDto(UUID id, long version, String name, String description, boolean active) implements Serializable {
    public ProjectDto(Project project) {
        this(project.getId(), project.getVersion(), project.getName(), project.getDescription(), project.isActive());
    }
}
