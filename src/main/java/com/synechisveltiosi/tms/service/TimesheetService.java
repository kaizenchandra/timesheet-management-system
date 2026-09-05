package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetCreationException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetUpdateException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetValidationException;
import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.api.request.TimesheetSubmissionRequest;
import com.synechisveltiosi.tms.api.request.TimesheetUpdateRequest;
import com.synechisveltiosi.tms.api.request.TimesheetWithdrawalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetCloneRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.api.response.PendingTimesheetDto;
import com.synechisveltiosi.tms.model.entity.*;
import com.synechisveltiosi.tms.model.enums.TimesheetEntryType;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.repository.TimesheetRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetService {
    private final TimesheetRepository timesheetRepository;
    private final EmployeeService employeeService;
    private final TimesheetMapper timesheetMapper;
    private final TimesheetValidator timesheetValidator;

    private static TimesheetEntry buildInitialTimesheetEntry(LocalDate currentDate) {
        return TimesheetEntry.builder()
                .date(currentDate)
                .entryType(TimesheetEntryType.NONE)
                .hours(0)
                .disable(isWeekend(currentDate))
                .build();
    }

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    @Transactional(readOnly = true)
    public List<TimesheetDto> getAllTimesheetByEmployeeId(UUID employeeId) {
        log.info("Getting timesheets for employee with id: {}", employeeId);
        List<Timesheet> timesheets = timesheetRepository.findAllByEmployeeIdOrderByStartDateDesc(employeeId);
        if (timesheets.isEmpty()) {
            throw new TimesheetNotFoundException("Timesheet not found for employee with id: " + employeeId);
        }
        return timesheets.stream().map(TimesheetDto::new).toList();
    }

    @Transactional(readOnly = true)
    public TimesheetDto getTimesheetById(UUID timesheetId) {
        log.info("Getting timesheet with id: {}", timesheetId);
        Timesheet timesheet = getTimesheetOrElseThrow(timesheetId);
        return new TimesheetDto(timesheet);
    }

    @Transactional(readOnly = true)
    public Page<PendingTimesheetDto> getPendingTimesheetsForManager(UUID managerId, Pageable pageable) {
        log.info("Getting pending timesheets for manager with id: {}", managerId);
        return timesheetRepository.findByEmployeeManagerIdAndStatus(managerId, TimesheetStatus.SUBMITTED, pageable)
                .map(PendingTimesheetDto::new);
    }

    private Timesheet getTimesheetOrElseThrow(UUID timesheetId) {
        return timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new TimesheetNotFoundException("Timesheet not found with id: " + timesheetId));
    }

    @Transactional
    public TimesheetDto draftOrSubmitTimesheet(UUID employeeId, TimesheetStatus status, TimesheetRequest timesheetRequest) {
        log.info("Creating timesheet for employee with id: {} for drafted or submitted status", employeeId);
        timesheetValidator.validateTimesheetCreation(timesheetRequest, status);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (timesheetRepository.existsByEmployeeIdAndStartDateAndEndDate(employeeId,
                timesheetRequest.startDate(), timesheetRequest.endDate())) {
            throw new TimesheetCreationException("A timesheet already exists for this employee and period");
        }

        Timesheet timesheet = timesheetMapper.toEntity(employee, status, timesheetRequest);
        if (status == TimesheetStatus.SUBMITTED) {
            Employee manager = requireManager(employee);
            timesheet.addApproval(createApproval(manager, TimesheetStatus.PENDING, ""));
        }
        return new TimesheetDto(timesheetRepository.save(timesheet));
    }

    @Transactional
    public TimesheetDto approveTimesheet(UUID timesheetId, UUID empApproverId, TimesheetApprovalRequest approvalRequest) {
        log.info("Approving timesheet with timesheetId {} and employeeApproverId {}", timesheetId, empApproverId);
        Employee approver = employeeService.getEmployeeById(empApproverId);
        Timesheet timesheet = getTimesheetOrElseThrow(timesheetId);
        validateApproval(timesheet, approver, approvalRequest.status());

        timesheet.setStatus(approvalRequest.status());
        timesheet.addApproval(createApproval(approver, approvalRequest.status(), approvalRequest.comments()));
        return new TimesheetDto(timesheetRepository.save(timesheet));
    }

    /**
     * Allows an employee to correct a generated, draft, or rejected timesheet before it enters the
     * manager-approval workflow. The period is immutable once the timesheet is generated.
     */
    @Transactional
    public TimesheetDto updateTimesheet(UUID timesheetId, UUID employeeId, TimesheetUpdateRequest request) {
        Timesheet timesheet = getEmployeeTimesheetOrElseThrow(timesheetId, employeeId);
        assertExpectedVersion(timesheet, request.version());
        assertEditable(timesheet);
        timesheetValidator.validateEntriesForPeriod(request.entries(), timesheet.getStartDate(), timesheet.getEndDate());

        timesheet.replaceEntries(timesheetMapper.toEntries(request.entries()));
        if (timesheet.getStatus() == TimesheetStatus.CREATED || timesheet.getStatus() == TimesheetStatus.REJECTED) {
            timesheet.setStatus(TimesheetStatus.DRAFTED);
        }
        return new TimesheetDto(timesheetRepository.saveAndFlush(timesheet));
    }

    /** Moves an employee-owned draft into manager approval and records the pending approval step. */
    @Transactional
    public TimesheetDto submitTimesheet(UUID timesheetId, UUID employeeId, TimesheetSubmissionRequest request) {
        Timesheet timesheet = getEmployeeTimesheetOrElseThrow(timesheetId, employeeId);
        assertExpectedVersion(timesheet, request.version());
        if (timesheet.getStatus() != TimesheetStatus.DRAFTED) {
            throw new TimesheetUpdateException("Only a drafted timesheet can be submitted");
        }

        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        timesheet.addApproval(createApproval(requireManager(timesheet.getEmployee()), TimesheetStatus.PENDING, ""));
        return new TimesheetDto(timesheetRepository.saveAndFlush(timesheet));
    }

    /**
     * Withdraws a submitted timesheet before it is decided. The cancellation is appended to the
     * approval history so that operational and audit views retain the actor and reason.
     */
    @Transactional
    public TimesheetDto withdrawTimesheet(UUID timesheetId, UUID employeeId, TimesheetWithdrawalRequest request) {
        Timesheet timesheet = getEmployeeTimesheetOrElseThrow(timesheetId, employeeId);
        assertExpectedVersion(timesheet, request.version());
        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new TimesheetUpdateException("Only a submitted timesheet can be withdrawn");
        }

        timesheet.withdraw();
        timesheet.addApproval(createApproval(timesheet.getEmployee(), TimesheetStatus.CANCELLED, request.comments()));
        return new TimesheetDto(timesheetRepository.saveAndFlush(timesheet));
    }

    /**
     * Copies task-backed entries into an equal-length target period. Generated empty-day entries
     * are deliberately excluded: the clone represents reusable work allocation, not a calendar snapshot.
     */
    @Transactional
    public TimesheetDto cloneTimesheet(UUID sourceTimesheetId, UUID employeeId, TimesheetCloneRequest request) {
        Timesheet source = timesheetRepository.findWithEntriesByIdAndEmployeeId(sourceTimesheetId, employeeId)
                .orElseThrow(() -> new TimesheetNotFoundException("Source timesheet not found for the employee"));
        assertExpectedVersion(source, request.sourceVersion());
        timesheetValidator.validateDateRange(request.targetStartDate(), request.targetEndDate());
        if (ChronoUnit.DAYS.between(source.getStartDate(), source.getEndDate())
                != ChronoUnit.DAYS.between(request.targetStartDate(), request.targetEndDate())) {
            throw new TimesheetValidationException("Target timesheet period must have the same length as the source period");
        }
        if (timesheetRepository.existsByEmployeeIdAndStartDateAndEndDate(employeeId,
                request.targetStartDate(), request.targetEndDate())) {
            throw new TimesheetCreationException("A timesheet already exists for this employee and target period");
        }

        List<TimesheetEntry> copiedEntries = source.getEntries().stream()
                .filter(entry -> entry.getTask() != null && entry.getTask().isActive() && entry.getTask().getProject().isActive())
                .map(entry -> copyEntry(entry, request.targetStartDate().plusDays(
                        ChronoUnit.DAYS.between(source.getStartDate(), entry.getDate()))))
                .toList();
        if (copiedEntries.isEmpty()) {
            throw new TimesheetCreationException("The source timesheet has no task entries to clone");
        }

        Timesheet draft = Timesheet.newDraft(source.getEmployee(), request.targetStartDate(), request.targetEndDate());
        copiedEntries.forEach(draft::addEntry);
        return new TimesheetDto(timesheetRepository.saveAndFlush(draft));
    }

    private TimesheetEntry copyEntry(TimesheetEntry source, LocalDate targetDate) {
        return TimesheetEntry.builder()
                .task(source.getTask())
                .date(targetDate)
                .hours(source.getHours())
                .entryType(source.getEntryType())
                .disable(isWeekend(targetDate))
                .build();
    }

    private TimesheetApproval createApproval(Employee approver, TimesheetStatus status, String comments) {
        return TimesheetApproval.builder()
                .comments(Objects.requireNonNullElse(comments, ""))
                .status(status)
                .approver(approver)
                .build();
    }

    private Timesheet getEmployeeTimesheetOrElseThrow(UUID timesheetId, UUID employeeId) {
        return timesheetRepository.findByIdAndEmployeeId(timesheetId, employeeId)
                .orElseThrow(() -> new TimesheetNotFoundException("Timesheet not found for the employee"));
    }

    private void assertExpectedVersion(Timesheet timesheet, long expectedVersion) {
        if (timesheet.getVersion() != expectedVersion) {
            throw new TimesheetUpdateException("The timesheet has changed. Refresh it before retrying your request");
        }
    }

    private void assertEditable(Timesheet timesheet) {
        if (timesheet.getStatus() != TimesheetStatus.CREATED
                && timesheet.getStatus() != TimesheetStatus.DRAFTED
                && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new TimesheetUpdateException("Only generated, drafted, or rejected timesheets can be edited");
        }
    }

    @Transactional
    public List<TimesheetDto> generateTimesheets(LocalDate startDate, LocalDate endDate) {
        timesheetValidator.validateDateRange(startDate, endDate);
        List<Employee> allEmployee = employeeService.getAllEmployee();
        List<Timesheet> timesheets = allEmployee.stream()
                .filter(employee -> !timesheetRepository.existsByEmployeeIdAndStartDateAndEndDate(
                        employee.getId(), startDate, endDate))
                .map(employee -> {
            Timesheet timesheet = timesheetMapper.createTimesheetBase(employee, TimesheetStatus.CREATED, TimesheetRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .entries(List.of())
                    .build());
            Collection<TimesheetEntry> timesheetEntries = generateEntries(startDate, endDate);
            timesheetEntries.forEach(timesheet::addEntry);
            return timesheet;
        }).toList();
        return timesheetRepository.saveAll(timesheets)
                .stream().map(TimesheetDto::new).toList();
    }

    private void validateApproval(Timesheet timesheet, Employee approver, TimesheetStatus decision) {
        if (decision != TimesheetStatus.APPROVED && decision != TimesheetStatus.REJECTED) {
            throw new TimesheetUpdateException("Only APPROVED or REJECTED are valid approval decisions");
        }
        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED && timesheet.getStatus() != TimesheetStatus.PENDING
                && timesheet.getStatus() != TimesheetStatus.OPEN_RESUBMITTED) {
            throw new TimesheetUpdateException("Only submitted timesheets can be approved or rejected");
        }
        Employee manager = requireManager(timesheet.getEmployee());
        if (!manager.getId().equals(approver.getId())) {
            throw new TimesheetUpdateException("Only the employee's manager can approve this timesheet");
        }
    }

    private Employee requireManager(Employee employee) {
        if (employee.getManager() == null) {
            throw new TimesheetUpdateException("The employee does not have an assigned manager");
        }
        return employee.getManager();
    }

    private Collection<TimesheetEntry> generateEntries(LocalDate startDate, LocalDate endDate) {
        List<TimesheetEntry> entries = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            entries.add(buildInitialTimesheetEntry(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        return entries;
    }
}


@Component
@RequiredArgsConstructor
class TimesheetMapper {
    private final TaskService taskService;

    public Timesheet toEntity(Employee employee, TimesheetStatus status, TimesheetRequest request) {
        Timesheet timesheet = createTimesheetBase(employee, status, request);
        addEntriesToTimesheet(timesheet, request.entries());
        return timesheet;
    }

    public Timesheet createTimesheetBase(Employee employee, TimesheetStatus status, TimesheetRequest request) {
        return Timesheet.builder()
                .startDate(request.startDate())
                .endDate(request.endDate())
                .employee(employee)
                .status(status)
                .build();
    }

    public void addEntriesToTimesheet(Timesheet timesheet, List<TimesheetRequest.TimesheetEntryRequest> entries) {
        toEntries(entries).forEach(timesheet::addEntry);
    }

    public List<TimesheetEntry> toEntries(List<TimesheetRequest.TimesheetEntryRequest> entries) {
        Map<Long, Task> tasksById = taskService.getTasksByIds(entries.stream()
                .map(TimesheetRequest.TimesheetEntryRequest::taskId).toList());
        return entries.stream()
                .map(timesheetEntryRequest -> createTimesheetEntry(timesheetEntryRequest, tasksById.get(timesheetEntryRequest.taskId())))
                .toList();
    }

    public TimesheetEntry createTimesheetEntry(TimesheetRequest.TimesheetEntryRequest request, Task rTask) {
        return TimesheetEntry.builder()
                .date(request.date())
                .hours(request.hours())
                .task(rTask)
                .entryType(request.entryType())
                .build();
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class TimesheetValidator {
    private static final String INVALID_STATUS_MESSAGE = "Cannot create timesheet in status: %s";
    private static final String INVALID_DATES_MESSAGE = "Start date cannot be after end date";
    private static final String NO_ENTRIES_MESSAGE = "Timesheet must contain at least one entry";

    public void validateTimesheetCreation(TimesheetRequest request, TimesheetStatus status) {
        log.info("Validating timesheet creation request");
        validateStatus(status);
        validateDateRange(request.startDate(), request.endDate());
        validateEntries(request.entries(), request.startDate(), request.endDate());
    }

    public void validateEntriesForPeriod(List<TimesheetRequest.TimesheetEntryRequest> entries,
                                         LocalDate startDate, LocalDate endDate) {
        validateEntries(entries, startDate, endDate);
    }

    private void validateStatus(TimesheetStatus status) {
        if (!isValidInitialStatus(status)) {
            throw new TimesheetValidationException(String.format(INVALID_STATUS_MESSAGE, status));
        }
    }

    private boolean isValidInitialStatus(TimesheetStatus status) {
        return status == TimesheetStatus.SUBMITTED || status == TimesheetStatus.DRAFTED;
    }

    void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new TimesheetValidationException(INVALID_DATES_MESSAGE);
        }
    }

    private void validateEntries(List<TimesheetRequest.TimesheetEntryRequest> entries, LocalDate startDate, LocalDate endDate) {
        if (entries == null || entries.isEmpty() || entries.stream().anyMatch(Objects::isNull)) {
            throw new TimesheetValidationException(NO_ENTRIES_MESSAGE);
        }
        Map<LocalDate, Double> hoursByDate = entries.stream()
                .collect(Collectors.groupingBy(TimesheetRequest.TimesheetEntryRequest::date,
                        Collectors.summingDouble(TimesheetRequest.TimesheetEntryRequest::hours)));
        if (entries.stream().anyMatch(entry -> !Double.isFinite(entry.hours()) || entry.hours() < 0)
                || hoursByDate.values().stream().anyMatch(hours -> hours > 24.0d)) {
            throw new TimesheetValidationException("Daily working hours must be between 0 and 24");
        }
        if (entries.stream().anyMatch(entry -> entry.date().isBefore(startDate) || entry.date().isAfter(endDate))) {
            throw new TimesheetValidationException("Entry dates must fall within the timesheet period");
        }
    }
}
