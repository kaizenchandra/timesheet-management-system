# Timesheet Management System

Spring Boot REST API for managing employees, projects, tasks, leave, holidays, timesheets, reports, and work calendars.

## Requirements

- Java 21
- Docker Desktop or Docker Engine with Docker Compose
- Git
- `curl` or an API client such as Postman/Insomnia

The application uses PostgreSQL 16 by default. Flyway applies database migrations automatically and Hibernate validates the resulting schema on startup.

## Start the development environment

1. Start PostgreSQL and pgAdmin:

   ```bash
   docker compose up -d
   ```

2. Confirm the database is ready:

   ```bash
   docker compose ps
   ```

   PostgreSQL is available at `localhost:5432`.

3. Start the application with the `dev` profile:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   On Windows, use `mvnw.cmd` instead of `./mvnw`.

   The development profile loads sample data at startup: employees, projects, tasks, and recent timesheets.

4. Check that the application is running:

   ```bash
   curl -i http://localhost:8080/v3/api-docs
   ```

   Open Swagger UI at <http://localhost:8080/swagger-ui/index.html>.

### Default database configuration

| Setting | Default |
| --- | --- |
| JDBC URL | `jdbc:postgresql://localhost:5432/tms` |
| Database | `tms` |
| Username | `cs` |
| Password | `admin` |
| API base URL | `http://localhost:8080/api/v1` |
| pgAdmin | <http://localhost:5050> |

Override the database connection without changing files:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/tms \
DATABASE_USERNAME=cs \
DATABASE_PASSWORD=admin \
./mvnw spring-boot:run
```

To stop the services:

```bash
docker compose down
```

To remove the local database volume and start with an empty database:

```bash
docker compose down -v
docker compose up -d
```

## Build and run tests

Compile the application:

```bash
./mvnw clean compile
```

Run the complete test suite:

```bash
./mvnw test
```

The integration tests use Testcontainers PostgreSQL, so Docker must be running. Run a specific test class with:

```bash
./mvnw -Dtest=TimesheetServiceTest test
./mvnw -Dtest=TimesheetControllerTest test
./mvnw -Dtest=DataLoaderPostgresIntegrationTest test
```

`DataLoaderPostgresIntegrationTest` starts an isolated PostgreSQL container, inserts employees, manager relationships, projects, project assignments, and tasks, and verifies the persisted data. It does not modify the PostgreSQL instance started by `docker compose`.

Package and run the application:

```bash
./mvnw clean package
java -jar target/timesheet-management-system-0.0.1-SNAPSHOT.jar
```

## API testing workflow

All endpoints are under `http://localhost:8080/api/v1`. Swagger provides the generated request and response schemas:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

The development data loader generates random UUIDs. Capture IDs from API responses or query them in pgAdmin before calling endpoints that require an employee, manager, project, task, or timesheet ID.

Set a shell variable for the API base URL:

```bash
export API=http://localhost:8080/api/v1
```

### Projects and tasks

Create a project:

```bash
curl -i -X POST "$API/projects" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Project","description":"Project created during API testing"}'
```

Create a task using the returned project ID:

```bash
curl -i -X POST "$API/tasks" \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"Demo Task",
    "description":"Task created during API testing",
    "projectId":"<PROJECT_ID>",
    "employeeIds":["<EMPLOYEE_ID>"]
  }'
```

Assign a project or task to an employee:

```bash
curl -i -X PUT "$API/projects/<PROJECT_ID>/employees/<EMPLOYEE_ID>"
curl -i -X PUT "$API/tasks/<TASK_ID>/employees/<EMPLOYEE_ID>"
```

Change project or task activity. Use the current `version` from the previous response:

```bash
curl -i -X PATCH "$API/projects/<PROJECT_ID>/active" \
  -H 'Content-Type: application/json' \
  -d '{"version":0,"active":false}'

curl -i -X PATCH "$API/tasks/<TASK_ID>/active" \
  -H 'Content-Type: application/json' \
  -d '{"version":0,"active":false}'
```

List an employee's active projects and tasks:

```bash
curl -i "$API/projects/employee/<EMPLOYEE_ID>?page=0&size=20"
curl -i "$API/tasks/employee/<EMPLOYEE_ID>?page=0&size=20"
```

### Employees and leave

Assign a manager to an employee. The `version` must match the employee response/database value:

```bash
curl -i -X PUT "$API/employees/<EMPLOYEE_ID>/manager/<MANAGER_ID>" \
  -H 'Content-Type: application/json' \
  -d '{"version":0}'
```

List direct reports:

```bash
curl -i "$API/employees/<MANAGER_ID>/subordinates?page=0&size=20"
```

Request and list leave:

```bash
curl -i -X POST "$API/leaves/employee/<EMPLOYEE_ID>" \
  -H 'Content-Type: application/json' \
  -d '{
    "startDate":"2026-09-14",
    "endDate":"2026-09-15",
    "type":"VACATION",
    "reason":"Personal time",
    "hours":16
  }'

curl -i "$API/leaves/employee/<EMPLOYEE_ID>"
```

Cancel leave as the employee:

```bash
curl -i -X POST "$API/leaves/<LEAVE_ID>/employee/<EMPLOYEE_ID>/cancel" \
  -H 'Content-Type: application/json' \
  -d '{"version":0}'
```

Approve or reject leave as the manager:

```bash
curl -i -X POST "$API/leaves/<LEAVE_ID>/manager/<MANAGER_ID>/decision" \
  -H 'Content-Type: application/json' \
  -d '{"version":0,"status":"APPROVED","comments":"Approved"}'
```

Supported leave types are `VACATION`, `SICK_LEAVE`, `PERSONAL_LEAVE`, `CASUAL_LEAVE`, `COMPENSATORY_LEAVE`, `LEGAL_LEAVE`, `MATERNITY_LEAVE`, `PATERNITY_LEAVE`, `BEREAVEMENT_LEAVE`, `UNPAID_LEAVE`, and `STUDY_LEAVE`.

### Holidays

Create, query, and delete holidays:

```bash
curl -i -X POST "$API/holidays" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Company Holiday","description":"Office closed","date":"2026-12-25"}'

curl -i "$API/holidays?startDate=2026-01-01&endDate=2026-12-31"

curl -i -X DELETE "$API/holidays/<HOLIDAY_ID>?version=0"
```

### Timesheets

Create a drafted timesheet for an employee. Entries require an existing task ID and dates within the timesheet period:

```bash
curl -i -X POST "$API/timesheets/<EMPLOYEE_ID>/status/DRAFTED" \
  -H 'Content-Type: application/json' \
  -d '{
    "startDate":"2026-09-07",
    "endDate":"2026-09-13",
    "entries":[
      {
        "taskId":<TASK_ID>,
        "entryType":"REGULAR",
        "date":"2026-09-07",
        "hours":8
      }
    ]
  }'
```

Supported timesheet statuses are `CREATED`, `DRAFTED`, `PENDING`, `APPROVED`, `REJECTED`, `SUBMITTED`, `OPEN_RESUBMITTED`, and `CANCELLED`. Supported entry types are defined in `TimesheetEntryType` and are also displayed by Swagger.

Retrieve timesheets:

```bash
curl -i "$API/timesheets/employee/<EMPLOYEE_ID>"
curl -i "$API/timesheets/<TIMESHEET_ID>"
curl -i "$API/timesheets/manager/<MANAGER_ID>/pending?page=0&size=20"
```

Update a draft using its current version:

```bash
curl -i -X PUT "$API/timesheets/<TIMESHEET_ID>/employee/<EMPLOYEE_ID>" \
  -H 'Content-Type: application/json' \
  -d '{
    "version":0,
    "entries":[
      {
        "taskId":<TASK_ID>,
        "entryType":"REGULAR",
        "date":"2026-09-07",
        "hours":8
      }
    ]
  }'
```

Submit, withdraw, clone, and approve a timesheet:

```bash
curl -i -X POST "$API/timesheets/<TIMESHEET_ID>/employee/<EMPLOYEE_ID>/submit" \
  -H 'Content-Type: application/json' \
  -d '{"version":0}'

curl -i -X POST "$API/timesheets/<TIMESHEET_ID>/employee/<EMPLOYEE_ID>/withdraw" \
  -H 'Content-Type: application/json' \
  -d '{"version":1,"comments":"Correction required"}'

curl -i -X POST "$API/timesheets/<TIMESHEET_ID>/employee/<EMPLOYEE_ID>/clone" \
  -H 'Content-Type: application/json' \
  -d '{"sourceVersion":0,"targetStartDate":"2026-09-14","targetEndDate":"2026-09-20"}'

curl -i -X POST "$API/timesheets/<TIMESHEET_ID>/approve/<MANAGER_ID>" \
  -H 'Content-Type: application/json' \
  -d '{"status":"APPROVED","comments":"Reviewed"}'
```

Generate timesheets for all employees in a date range:

```bash
curl -i -X POST "$API/timesheets/generate?startDate=2026-09-07&endDate=2026-09-13"
```

### Reports and work calendar

Get an employee's hours summary:

```bash
curl -i "$API/timesheet-reports/employee/<EMPLOYEE_ID>/hours?startDate=2026-09-01&endDate=2026-09-30&status=APPROVED"
```

Get the employee work calendar:

```bash
curl -i "$API/work-calendar/employee/<EMPLOYEE_ID>?startDate=2026-09-01&endDate=2026-09-30"
```

## API endpoint reference

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `PUT` | `/employees/{employeeId}/manager/{managerId}` | Assign manager |
| `GET` | `/employees/{managerId}/subordinates` | List direct reports |
| `POST` | `/projects` | Create project |
| `PUT` | `/projects/{projectId}/employees/{employeeId}` | Assign project |
| `PATCH` | `/projects/{projectId}/active` | Archive/restore project |
| `GET` | `/projects/employee/{employeeId}` | List employee projects |
| `POST` | `/tasks` | Create task |
| `PUT` | `/tasks/{taskId}/employees/{employeeId}` | Assign task |
| `PATCH` | `/tasks/{taskId}/active` | Archive/restore task |
| `GET` | `/tasks/employee/{employeeId}` | List employee tasks |
| `POST` | `/holidays` | Create holiday |
| `GET` | `/holidays` | List holidays in a date range |
| `DELETE` | `/holidays/{holidayId}` | Delete holiday |
| `POST` | `/leaves/employee/{employeeId}` | Request leave |
| `GET` | `/leaves/employee/{employeeId}` | List employee leave |
| `POST` | `/leaves/{leaveId}/employee/{employeeId}/cancel` | Cancel leave |
| `POST` | `/leaves/{leaveId}/manager/{managerId}/decision` | Approve/reject leave |
| `POST` | `/timesheets/{empId}/status/{status}` | Create timesheet |
| `POST` | `/timesheets/generate` | Generate timesheets |
| `GET` | `/timesheets/employee/{empId}` | List employee timesheets |
| `GET` | `/timesheets/{tmsId}` | Get one timesheet |
| `GET` | `/timesheets/manager/{managerId}/pending` | Manager pending queue |
| `POST` | `/timesheets/{tmsId}/approve/{empId}` | Approve/reject timesheet |
| `PUT` | `/timesheets/{tmsId}/employee/{empId}` | Update timesheet |
| `POST` | `/timesheets/{tmsId}/employee/{empId}/submit` | Submit timesheet |
| `POST` | `/timesheets/{tmsId}/employee/{empId}/withdraw` | Withdraw timesheet |
| `POST` | `/timesheets/{tmsId}/employee/{empId}/clone` | Clone timesheet |
| `GET` | `/timesheet-reports/employee/{employeeId}/hours` | Summarize hours |
| `GET` | `/work-calendar/employee/{employeeId}` | Get work calendar |

## Troubleshooting

- **Missing `active` column:** stop the application and restart it so Flyway can apply the latest migrations.
- **PostgreSQL connection refused:** verify `docker compose ps` and wait for the `postgres` health check to pass.
- **Duplicate sample data:** the `dev` profile runs the data loader on every application startup. Use `docker compose down -v` to reset the local database.
- **Port already in use:** change the PostgreSQL or pgAdmin host port in `docker-compose.yaml`, or run Spring Boot with `-Dspring-boot.run.arguments=--server.port=8081`.
