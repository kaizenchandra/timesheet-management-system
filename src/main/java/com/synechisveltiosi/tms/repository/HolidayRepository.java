package com.synechisveltiosi.tms.repository;

import com.synechisveltiosi.tms.model.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    boolean existsByDate(LocalDate date);

    List<Holiday> findAllByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
