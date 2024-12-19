package com.primefactorsolutions.service;

import com.primefactorsolutions.model.Question;
import com.primefactorsolutions.repositories.QuestionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Question getQuestion(final UUID id) {
        return questionRepository.findById(id).get();
    }

    public List<Question> getQuestions() {
        return questionRepository.findAll();
    }

    public Question createOrUpdate(final Question question) {
        return questionRepository.save(question);
    }
}
