package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByPersonalEmail(String personalEmail);
    List<Employee> findByTeamIdAndLeadManagerTrue(UUID teamId);

    //Optional<Employee> findByTeamIdAndLeadManagerTrue(UUID teamId);
    List<Employee> findByTeamName(String teamName);
}