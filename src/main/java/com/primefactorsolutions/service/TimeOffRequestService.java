package com.primefactorsolutions.service;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.repositories.TimeOffRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TimeOffRequestService {
    private final TimeOffRequestRepository timeOffRequestRepository;

    public void saveTimeOffRequest(final TimeOffRequest newTimeOffRequest) {
        timeOffRequestRepository.save(newTimeOffRequest);
    }

    public void deleteTimeOffRequestByEmployeeAndCategory(final UUID employeeId, final TimeOffRequestType category) {
        timeOffRequestRepository.deleteByEmployeeIdAndCategory(employeeId, category);
    }

    public void saveAll(final List<TimeOffRequest> requests) {
        timeOffRequestRepository.saveAll(requests);
    }

    public void deleteTimeOffRequest(final UUID id) {
        timeOffRequestRepository.deleteById(id);
    }

    public List<TimeOffRequest> findAllTimeOffRequests() {
        return timeOffRequestRepository.findAll();
    }

    public TimeOffRequest findTimeOffRequest(final UUID id) {
        Optional<TimeOffRequest> timeOffRequest = timeOffRequestRepository.findById(id);
        return timeOffRequest.orElse(null);
    }

    public List<TimeOffRequest> findRequestsByState(final TimeOffRequestStatus state) {
        return timeOffRequestRepository.findByState(state);
    }

    public List<TimeOffRequest> findRequestsByEmployeeId(final UUID idEmployee) {
        return timeOffRequestRepository.findByEmployeeId(idEmployee);
    }

    public Optional<TimeOffRequest> findByEmployeeAndState(final UUID employeeId, final TimeOffRequestStatus state) {
        return timeOffRequestRepository.findByEmployeeIdAndState(employeeId, state);
    }

    public List<TimeOffRequest> findByEmployeeAndCategory(final UUID employeeId, final TimeOffRequestType category) {
        return timeOffRequestRepository.findByEmployeeIdAndCategory(employeeId, category);
    }

    public void updateRequestStatuses() {
        List<TimeOffRequest> requests = findAllTimeOffRequests();
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = LocalDate.of(now.getYear(), 1, 1);

        for (TimeOffRequest request : requests) {
            if (request.getCategory() == TimeOffRequestType.VACACION_GESTION_ACTUAL && now.isEqual(startOfYear)) {
                deleteTimeOffRequestByEmployeeAndCategory(
                        request.getEmployee().getId(),
                        TimeOffRequestType.VACACION_GESTION_ANTERIOR
                );
                request.setCategory(TimeOffRequestType.VACACION_GESTION_ANTERIOR);
            }

            if (request.getState() == TimeOffRequestStatus.APROBADO
                    || request.getState() == TimeOffRequestStatus.EN_USO) {
                LocalDate startDate = request.getStartDate();
                LocalDate endDate = request.getEndDate();

                if (now.isAfter(endDate)) {
                    request.setState(TimeOffRequestStatus.TOMADO);
                } else if (now.isEqual(startDate) || (now.isAfter(startDate) && now.isBefore(endDate))) {
                    request.setState(TimeOffRequestStatus.EN_USO);
                }
            }
        }

        saveAll(requests);
    }
}