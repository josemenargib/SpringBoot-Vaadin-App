package com.primefactorsolutions.service;

import com.primefactorsolutions.model.*;
import com.primefactorsolutions.repositories.AssessmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final EntityManager entityManager;
    private final JavaMailSender emailSender;

    public Assessment createOrUpdate(final Assessment assessment) {
        final Assessment saved = assessmentRepository.save(assessment);

        return saved;
    }

    public void sendEmail(final Assessment assessment) {
        try {
            final String evaluationLink = String.format("https://careers.primefactorsolutions.com/evaluation/%s",
                    assessment.getId());
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@primefactorsolutions.com");
            message.setBcc("no-reply@primefactorsolutions.com");
            message.setTo(assessment.getCandidate().getEmail());
            message.setSubject("PFS - Evaluacion Tecnica");
            message.setText(String.format("Estimado candidato,\n\nGracias por su candidatura. En esta etapa del "
                    + "proceso de seleccion, usted debe completar "
                    + "una evaluacion tecnica de programacion en JAVA. La prueba tiene una duracion de 30 minutos y "
                    + "puede completarla cuando tenga una buena "
                    + "conexion de internet.\n\n"
                    + "Haga click aca: " + evaluationLink + "\n\n"
                    + "Exito!"));

            emailSender.send(message);
            log.info("Sent email to {}", assessment.getCandidate().getEmail());
        } catch (Exception e) {
            log.error("Error sending email to {}", assessment.getCandidate().getEmail(), e);
            throw e;
        }
    }

    public List<Assessment> getAssessments() {
        return assessmentRepository.findAll();
    }

    public Assessment getAssessment(final UUID id) {
        return assessmentRepository.findById(id).orElse(null);
    }

    public Assessment startAssessment(final UUID id) {
        final Assessment assessment = assessmentRepository.findById(id).get();
        if (assessment.isStarted()) {
            return assessment;
        }

        assessment.getAssessmentEvents().add(new AssessmentEvent(Instant.now(), AssessmentStatus.STARTED));

        return assessmentRepository.save(assessment);
    }

    public Submission getNextSubmission(final UUID assessmentId, final UUID currSubmissionId) {
        return getNextSubmission(assessmentId, currSubmissionId, true);
    }

    public Submission getNextSubmission(final UUID assessmentId, final UUID currSubmissionId,
                                        final boolean checkCompleted) {
        final Assessment assessment = assessmentRepository.findById(assessmentId).get();

        Assessment saved;
        if (currSubmissionId == null) {
            if (checkCompleted && assessment.isCompleted()) {
                return null;
            }

            final Question firstQuestion = assessment.getQuestions().stream().findFirst().get();
            final Optional<Submission> submissionToReturn = assessment.getSubmissions().stream()
                    .filter(s -> s.getQuestion().equals(firstQuestion))
                    .findFirst();

            if (submissionToReturn.isEmpty()) {
                final Submission result = new Submission(firstQuestion, firstQuestion.getContent(), Map.of(),
                        SubmissionStatus.FAIL, assessment);
                assessment.getSubmissions().add(result);
            }

            saved = assessmentRepository.save(assessment);
            final Optional<Submission> submissionToReturn2 = saved.getSubmissions().stream()
                    .filter(s -> s.getQuestion().equals(firstQuestion))
                    .findFirst();

            return submissionToReturn2.get();
        }

        final Submission currSubmission = assessment.getSubmissions().stream()
                .filter(s -> s.getId().equals(currSubmissionId))
                .findFirst().get();
        final Question currQuestion = currSubmission.getQuestion();
        int idx = assessment.getQuestions().indexOf(currQuestion);

        if (idx == assessment.getQuestions().size() - 1) {
            return null;
        }

        final Question nextQuestion = assessment.getQuestions().get(idx + 1);

        final Optional<Submission> submissionToReturn = assessment.getSubmissions().stream()
                .filter(s -> s.getQuestion().equals(nextQuestion))
                .findFirst();

        if (submissionToReturn.isEmpty()) {
            final Submission result = new Submission(nextQuestion, nextQuestion.getContent(), Map.of(),
                    SubmissionStatus.FAIL, assessment);
            assessment.getSubmissions().add(result);
        }

        saved = assessmentRepository.save(assessment);
        final Optional<Submission> submissionToReturn2 = saved.getSubmissions().stream()
                .filter(s -> s.getQuestion().equals(nextQuestion))
                .findFirst();

        return submissionToReturn2.get();
    }

    public Submission getPrevSubmission(final UUID assessmentId, final Submission currSubmission) {
        if (currSubmission == null) {
            return null;
        }

        final Question currQuestion = currSubmission.getQuestion();
        Assessment assessment = assessmentRepository.findById(assessmentId).get();
        int idx = assessment.getQuestions().indexOf(currQuestion);

        if (idx == 0) {
            return null;
        }

        final Question prevQuestion = assessment.getQuestions().get(idx - 1);

        return assessment.getSubmissions().stream()
                .filter(s -> s.getQuestion().equals(prevQuestion))
                .findFirst().orElseThrow(() -> new IllegalStateException("submission invalid"));
    }

    public Assessment completeAssessment(final UUID id) {
        Assessment assessment = assessmentRepository.findById(id).get();
        Optional<AssessmentEvent> completed = assessment.getAssessmentEvents().stream()
                .filter(e -> e.getStatus() == AssessmentStatus.COMPLETED)
                .findFirst();

        if (completed.isPresent()) {
            return assessment;
        }

        assessment.getAssessmentEvents().add(new AssessmentEvent(Instant.now(), AssessmentStatus.COMPLETED));
        Assessment saved = assessmentRepository.save(assessment);

        return saved;
    }

    public void saveSubmission(final UUID id, final Submission currSubmission) {
        Assessment assessment = assessmentRepository.findById(id).get();
        final Submission submission = assessment.getSubmissions().stream()
                .filter(s -> s.getId().equals(currSubmission.getId()))
                .findFirst().get();

        submission.setResponse(currSubmission.getResponse());
        Assessment saved = assessmentRepository.save(assessment);
    }

    @Transactional
    public Assessment saveAssessment(final Assessment assessment) {
        Candidate merged = entityManager.merge(assessment.getCandidate());
        List<Question> mergedQuestions = assessment.getQuestions().stream().map(entityManager::merge)
                .collect(Collectors.toList());

        assessment.setCandidate(merged);
        assessment.setQuestions(mergedQuestions);

        return assessmentRepository.save(assessment);
    }
}
