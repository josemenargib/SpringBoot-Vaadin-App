package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.TimeOffRequest;
import com.primefactorsolutions.model.TimeOffRequestStatus;
import com.primefactorsolutions.model.TimeOffRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, UUID> {
    List<TimeOffRequest> findByEmployeeId(UUID idEmployee);
    Optional<TimeOffRequest> findByEmployeeIdAndState(UUID employeeId, TimeOffRequestStatus state);
    List<TimeOffRequest> findByEmployeeIdAndCategory(UUID employeeId, TimeOffRequestType category);
    List<TimeOffRequest> findByState(TimeOffRequestStatus state);
    void deleteByEmployeeIdAndCategory(UUID employeeId, TimeOffRequestType category);
}
