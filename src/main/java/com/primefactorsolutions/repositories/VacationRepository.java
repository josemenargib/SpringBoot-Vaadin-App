package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.TimeOffRequestType;
import com.primefactorsolutions.model.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VacationRepository extends JpaRepository<Vacation, UUID> {
    Vacation findByCategory(TimeOffRequestType category);
}
