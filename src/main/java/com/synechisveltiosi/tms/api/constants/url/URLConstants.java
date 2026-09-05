package com.synechisveltiosi.tms.api.constants.url;

public class URLConstants {
    public static final String API_VERSION = "/api/v1";

    public static final class EmployeeEndpoint {
        public static final String BASE = API_VERSION + "/employees";
    }

    public static class TimesheetEndpoint {
        public static final String BASE = API_VERSION + "/timesheets";
        public static final String BY_EMP_ID = "/employee/{empId}";
        public static final String BY_EMP_ID_TMS_STATUS = "/{empId}/status/{status}";
        public static final String BY_TMS_ID_EMP_ID = "/{tmsId}/approve/{empId}";
        public static final String BY_TMS_ID = "/{tmsId}";
        public static final String BY_TMS_ID_EMPLOYEE = "/{tmsId}/employee/{empId}";
        public static final String SUBMIT_BY_TMS_ID_EMPLOYEE = BY_TMS_ID_EMPLOYEE + "/submit";
        public static final String WITHDRAW_BY_TMS_ID_EMPLOYEE = BY_TMS_ID_EMPLOYEE + "/withdraw";
        public static final String CLONE_BY_TMS_ID_EMPLOYEE = BY_TMS_ID_EMPLOYEE + "/clone";
    }

    public static class Employee {
        public static final String EMPLOYEES = API_VERSION + "/employees";
    }

    public static class ProjectEndpoint {
        public static final String BASE = API_VERSION + "/projects";
    }

    public static class TaskEndpoint {
        public static final String BASE = API_VERSION + "/tasks";
    }

    public static class HolidayEndpoint {
        public static final String BASE = API_VERSION + "/holidays";
    }

    public static class LeaveEndpoint {
        public static final String BASE = API_VERSION + "/leaves";
        public static final String BY_EMPLOYEE = "/employee/{employeeId}";
        public static final String CANCEL_BY_LEAVE_ID_EMPLOYEE = "/{leaveId}/employee/{employeeId}/cancel";
        public static final String DECIDE_BY_LEAVE_ID_MANAGER = "/{leaveId}/manager/{managerId}/decision";
    }

    public static class TimesheetReportEndpoint {
        public static final String BASE = API_VERSION + "/timesheet-reports";
    }

    public static class WorkCalendarEndpoint {
        public static final String BASE = API_VERSION + "/work-calendar";
    }
}
