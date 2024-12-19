package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.HoursWorked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface HoursWorkedRepository extends JpaRepository<HoursWorked, UUID> {
    List<HoursWorked> findByWeekNumber(int weekNumber);
    List<HoursWorked> findByDate(LocalDate date);
    List<HoursWorked> findByEmployeeIdAndWeekNumber(UUID employeeId, int weekNumber);
}

