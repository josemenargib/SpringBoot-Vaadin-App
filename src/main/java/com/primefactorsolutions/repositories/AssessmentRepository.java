package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
}
