package com.synechisveltiosi.tms.handler;

import com.synechisveltiosi.tms.model.embed.PersonDetails;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Project;
import com.synechisveltiosi.tms.model.entity.Task;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.repository.ProjectRepository;
import com.synechisveltiosi.tms.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class DataLoaderPostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @Transactional
    void shouldInsertEmployeesWithManagerHierarchyIntoPostgres() {
        Employee manager = employeeRepository.save(employee("manager@example.com", "Manager"));
        Employee employeeOne = employeeRepository.save(employee("employee.one@example.com", "Employee One"));
        Employee employeeTwo = employeeRepository.save(employee("employee.two@example.com", "Employee Two"));

        employeeOne.assignManager(manager);
        employeeTwo.assignManager(manager);
        employeeRepository.saveAll(List.of(employeeOne, employeeTwo));

        assertThat(employeeRepository.count()).isGreaterThanOrEqualTo(6);
        assertThat(employeeRepository.findById(employeeOne.getId()))
                .get()
                .extracting(Employee::getManager)
                .isEqualTo(manager);
        assertThat(employeeRepository.findByManagerId(manager.getId(), org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent())
                .containsExactlyInAnyOrder(employeeOne, employeeTwo);
    }

    @Test
    @Transactional
    void shouldInsertProjectsTasksAndAssignmentsIntoPostgres() {
        Employee employee = employeeRepository.save(employee("worker@example.com", "Worker"));
        Project project = projectRepository.save(Project.builder()
                .name("Integration Project")
                .description("Project inserted by PostgreSQL integration test")
                .build());

        project.assignTo(employee);
        projectRepository.saveAndFlush(project);

        Task task = taskRepository.save(Task.builder()
                .name("Integration Task")
                .description("Task inserted by PostgreSQL integration test")
                .project(project)
                .build());
        task.assignTo(employee);
        taskRepository.saveAndFlush(task);

        assertThat(projectRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(taskRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(projectRepository.findById(project.getId()))
                .get()
                .satisfies(savedProject -> {
                    assertThat(savedProject.isActive()).isTrue();
                    assertThat(savedProject.getEmployees()).contains(employee);
                });
        assertThat(taskRepository.findById(task.getId()))
                .get()
                .satisfies(savedTask -> {
                    assertThat(savedTask.isActive()).isTrue();
                    assertThat(savedTask.getProject()).isEqualTo(project);
                    assertThat(savedTask.getEmployees()).contains(employee);
                });
    }

    private Employee employee(String email, String lastName) {
        return Employee.builder()
                .personDetails(PersonDetails.builder()
                        .name(PersonDetails.Name.builder()
                                .firstName("Test")
                                .lastName(lastName)
                                .build())
                        .address(PersonDetails.Address.builder()
                                .addressLine1("1 Test Street")
                                .city("Test City")
                                .state("TS")
                                .zipCode("00000")
                                .country("Testland")
                                .build())
                        .contact(PersonDetails.Contact.builder()
                                .contactNumber("555-0000")
                                .emailAddress(email)
                                .build())
                        .build())
                .build();
    }
}
