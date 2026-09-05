package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.LeaveCancellationRequest;
import com.synechisveltiosi.tms.api.request.LeaveRequest;
import com.synechisveltiosi.tms.api.request.LeaveDecisionRequest;
import com.synechisveltiosi.tms.api.response.LeaveDto;
import com.synechisveltiosi.tms.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(URLConstants.LeaveEndpoint.BASE)
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping(URLConstants.LeaveEndpoint.BY_EMPLOYEE)
    public ResponseEntity<LeaveDto> requestLeave(@PathVariable UUID employeeId,
                                                  @Valid @RequestBody LeaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.requestLeave(employeeId, request));
    }

    @GetMapping(URLConstants.LeaveEndpoint.BY_EMPLOYEE)
    public ResponseEntity<List<LeaveDto>> getLeavesForEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesForEmployee(employeeId));
    }

    @PostMapping(URLConstants.LeaveEndpoint.CANCEL_BY_LEAVE_ID_EMPLOYEE)
    public ResponseEntity<LeaveDto> cancelLeave(@PathVariable UUID leaveId, @PathVariable UUID employeeId,
                                                 @Valid @RequestBody LeaveCancellationRequest request) {
        return ResponseEntity.ok(leaveService.cancelLeave(leaveId, employeeId, request));
    }

    @PostMapping(URLConstants.LeaveEndpoint.DECIDE_BY_LEAVE_ID_MANAGER)
    public ResponseEntity<LeaveDto> decideLeave(@PathVariable UUID leaveId, @PathVariable UUID managerId,
                                                @Valid @RequestBody LeaveDecisionRequest request) {
        return ResponseEntity.ok(leaveService.decideLeave(leaveId, managerId, request));
    }
}
