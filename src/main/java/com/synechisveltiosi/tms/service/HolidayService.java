package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;
import com.synechisveltiosi.tms.api.exception.ResourceUpdateException;
import com.synechisveltiosi.tms.api.exception.ResourceValidationException;
import com.synechisveltiosi.tms.api.request.HolidayRequest;
import com.synechisveltiosi.tms.api.response.HolidayDto;
import com.synechisveltiosi.tms.model.entity.Holiday;
import com.synechisveltiosi.tms.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {
    private final HolidayRepository holidayRepository;

    @Transactional
    public HolidayDto createHoliday(HolidayRequest request) {
        if (holidayRepository.existsByDate(request.date())) {
            throw new ResourceValidationException("A holiday already exists for " + request.date());
        }
        Holiday holiday = Holiday.builder()
                .name(request.name().trim())
                .description(request.description() == null ? null : request.description().trim())
                .date(request.date())
                .build();
        return new HolidayDto(holidayRepository.saveAndFlush(holiday));
    }

    @Transactional(readOnly = true)
    public List<HolidayDto> getHolidays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResourceValidationException("Holiday start date cannot be after end date");
        }
        return holidayRepository.findAllByDateBetweenOrderByDateAsc(startDate, endDate).stream()
                .map(HolidayDto::new)
                .toList();
    }

    @Transactional
    public void deleteHoliday(Long holidayId, long version) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with id: " + holidayId));
        if (holiday.getVersion() != version) {
            throw new ResourceUpdateException("The holiday has changed. Refresh it before retrying your request");
        }
        holidayRepository.delete(holiday);
    }
}
