package com.primefactorsolutions.service;

import com.primefactorsolutions.model.Assessment;
import com.primefactorsolutions.model.AssessmentEvent;
import com.primefactorsolutions.model.AssessmentStatus;
import com.primefactorsolutions.repositories.AssessmentRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssessmentServiceTests {

    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private JavaMailSender emailSender;
    @InjectMocks
    private AssessmentService assessmentService;

    @Test
    public void testAlreadyStartedAssessment() {
        final var aid = UUID.randomUUID();
        final var assessment = new Assessment();
        assessment.setId(aid);
        assessment.setAssessmentEvents(List.of(new AssessmentEvent(Instant.now(), AssessmentStatus.STARTED)));

        when(assessmentRepository.findById(eq(aid)))
                .thenReturn(Optional.of(assessment));

        final var started = assessmentService.startAssessment(aid);

        Assertions.assertThat(assessment).isEqualTo(started);
    }
}
