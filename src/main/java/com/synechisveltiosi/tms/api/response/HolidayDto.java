package com.synechisveltiosi.tms.api.response;

import java.io.Serializable;
import java.time.LocalDate;
import com.synechisveltiosi.tms.model.entity.Holiday;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Holiday}
 */
public record HolidayDto(Long id, long version, String name, String description, LocalDate date) implements Serializable {
    public HolidayDto(Holiday holiday) {
        this(holiday.getId(), holiday.getVersion(), holiday.getName(), holiday.getDescription(), holiday.getDate());
    }
}
