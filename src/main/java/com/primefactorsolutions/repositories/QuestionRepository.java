package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}
