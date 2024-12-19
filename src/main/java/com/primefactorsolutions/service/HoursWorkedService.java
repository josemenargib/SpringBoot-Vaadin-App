package com.primefactorsolutions.service;

import com.primefactorsolutions.model.HoursWorked;
import com.primefactorsolutions.repositories.HoursWorkedRepository;
import org.apache.commons.beanutils.BeanComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class HoursWorkedService {
    private final HoursWorkedRepository hoursWorkedRepository;

    @Autowired
    public HoursWorkedService(final HoursWorkedRepository hoursWorkedRepository) {
        this.hoursWorkedRepository = hoursWorkedRepository;
    }

    public List<HoursWorked> findAll() {
        return hoursWorkedRepository.findAll();
    }

    public double getTotalHoursWorkedByEmployeeForWeek(final UUID employeeId, final int weekNumber) {
        List<HoursWorked> hoursWorkedList = hoursWorkedRepository.findByWeekNumber(weekNumber);
        return hoursWorkedList.stream()
                .filter(hw -> hw.getEmployee().getId().equals(employeeId))
                .mapToDouble(HoursWorked::getTotalHours)
                .sum();
    }

    public HoursWorked findHoursWorked(final UUID id) {
        Optional<HoursWorked> hoursWorked = hoursWorkedRepository.findById(id);
        HoursWorked hw = hoursWorked.get();
        return hw;
    }

    public HoursWorked saveHoursWorked(final HoursWorked hoursWorked) {
        return hoursWorkedRepository.save(hoursWorked);
    }

    public HoursWorked save(final HoursWorked hoursWorked) {
        return hoursWorkedRepository.save(hoursWorked);
    }

    public double getTotalHoursForEmployee(final UUID employeeId, final int weekNumber) {
        List<HoursWorked> activities = hoursWorkedRepository.findByEmployeeIdAndWeekNumber(employeeId, weekNumber);
        return HoursWorked.calculateTotalHours(activities);
    }

    public double getPendingHoursForEmployee(final UUID employeeId, final int weekNumber) {
        List<HoursWorked> activities = hoursWorkedRepository.findByEmployeeIdAndWeekNumber(employeeId, weekNumber);
        return HoursWorked.calculatePendingHours(activities);
    }

    public List<HoursWorked> findByWeekNumber(final int weekNumber) {
        return hoursWorkedRepository.findByWeekNumber(weekNumber);
    }

    public List<HoursWorked> findByDate(final LocalDate date) {
        return hoursWorkedRepository.findByDate(date);
    }

    public List<HoursWorked> findByDateAndWeekNumber(final LocalDate date, final int weekNumber) {
        return hoursWorkedRepository.findByDate(date);
    }

    public List<HoursWorked> findHoursWorkeds(
            final int start, final int pageSize, final String sortProperty, final boolean asc) {
        List<HoursWorked> hoursWorkeds = hoursWorkedRepository.findAll();

        int end = Math.min(start + pageSize, hoursWorkeds.size());
        hoursWorkeds.sort(new BeanComparator<>(sortProperty));

        if (!asc) {
            Collections.reverse(hoursWorkeds);
        }

        return hoursWorkeds.subList(start, end);
    }

    public List<HoursWorked> findHoursWorkeds(final int start, final int pageSize) {
        List<HoursWorked> hoursWorkeds = hoursWorkedRepository.findAll();

        int end = Math.min(start + pageSize, hoursWorkeds.size());
        return hoursWorkeds.subList(start, end);
    }

    public HoursWorked getHoursWorked(final UUID id) {
        final Optional<HoursWorked> hoursWorked = hoursWorkedRepository.findById(id);
        return hoursWorked.get();
    }

    public List<HoursWorked> findListHoursWorkedEmployee(final UUID employeeId, final int weekNumber) {
        return hoursWorkedRepository.findByEmployeeIdAndWeekNumber(employeeId, weekNumber);
    }

}
