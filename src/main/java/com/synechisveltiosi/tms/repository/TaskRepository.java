package com.synechisveltiosi.tms.repository;

import com.synechisveltiosi.tms.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from Task task where task.id = :taskId")
    Optional<Task> findByIdForUpdate(@Param("taskId") Long taskId);

    @EntityGraph(attributePaths = "project")
    Page<Task> findByEmployeesIdAndActiveTrueAndProjectActiveTrue(UUID employeeId, Pageable pageable);

    boolean existsByProjectIdAndNameIgnoreCase(UUID projectId, String name);
}
