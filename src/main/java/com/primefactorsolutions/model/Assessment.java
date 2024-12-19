package com.primefactorsolutions.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Assessment extends BaseEntity {
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(name = "ASSESSMENT_QUESTIONS", joinColumns = @JoinColumn(name = "assessment_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> questions = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "assessment", cascade = {CascadeType.ALL})
    private List<Submission> submissions = new ArrayList<>();

    @ManyToOne
    private Candidate candidate;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinColumn(name = "ASSESSMENT_ID")
    private List<AssessmentEvent> assessmentEvents = new ArrayList<>();

    public Submission getCurrentSubmission() {
        return submissions.getLast();
    }

    public Long getRemainingTimeSeconds() {
        final Optional<Instant> started = assessmentEvents.stream()
                .filter(e -> e.getStatus() == AssessmentStatus.STARTED)
                .map(AssessmentEvent::getTimestamp)
                .findFirst();

        final Integer totalTimeMinutes = questions.stream()
                .map(Question::getTimeMinutes)
                .filter(Objects::nonNull)
                .reduce(Integer::sum).orElse(30);
        final long totalTimeSeconds = totalTimeMinutes * 60;

        return started.map(instant -> totalTimeSeconds - (Instant.now().getEpochSecond() - instant.getEpochSecond()))
                .orElse(totalTimeSeconds);
    }

    public Instant getStartingTime() {
        final Optional<Instant> started = assessmentEvents.stream()
                .filter(e -> e.getStatus() == AssessmentStatus.STARTED)
                .map(AssessmentEvent::getTimestamp)
                .findFirst();

        return started.orElse(null);
    }

    public boolean isCompleted() {
        return assessmentEvents.stream().filter(e -> e.getStatus() == AssessmentStatus.COMPLETED)
                .map(AssessmentEvent::getTimestamp)
                .findFirst()
                .isPresent();
    }

    public boolean isStarted() {
        return assessmentEvents.stream().filter(e -> e.getStatus() == AssessmentStatus.STARTED)
                .map(AssessmentEvent::getTimestamp)
                .findFirst()
                .isPresent();
    }

    public boolean isFirst(final Submission currSubmission) {
        return getQuestions().indexOf(currSubmission.getQuestion()) == 0;
    }

    public boolean isLast(final Submission currSubmission) {
        return getQuestions().indexOf(currSubmission.getQuestion()) == getQuestions().size() - 1;
    }
}
