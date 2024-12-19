package com.primefactorsolutions.service;

import com.primefactorsolutions.model.TimeOffRequestType;
import com.primefactorsolutions.model.Vacation;
import com.primefactorsolutions.repositories.VacationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VacationService {
    private final VacationRepository vacationRepository;

    public Vacation findVacationByCategory(final TimeOffRequestType category) {
        return vacationRepository.findByCategory(category);
    }

    public List<Vacation> findVacations() {
        return vacationRepository.findAll();
    }
}