package com.primefactorsolutions.service;

import com.primefactorsolutions.model.Candidate;
import com.primefactorsolutions.repositories.CandidateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CandidateService {
    private final CandidateRepository candidateRepository;

    public Candidate createOrUpdate(final Candidate assessment) {
        final Candidate saved = candidateRepository.save(assessment);

        return saved;
    }

    public List<Candidate> getCandidates() {
        return candidateRepository.findAll();
    }

    public Candidate getCandidate(final UUID id) {
        return candidateRepository.findById(id).orElse(null);
    }
}
