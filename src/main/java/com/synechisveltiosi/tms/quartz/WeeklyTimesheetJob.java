package com.synechisveltiosi.tms.quartz;

import com.synechisveltiosi.tms.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class WeeklyTimesheetJob implements Job {

    private final TimesheetService timesheetService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LocalDate endDate = LocalDate.now().minusDays(1);
        timesheetService.generateTimesheets(endDate.minusDays(6), endDate);
    }
}
