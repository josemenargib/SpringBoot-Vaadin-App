package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Candidate;
import com.primefactorsolutions.model.Assessment;
import com.primefactorsolutions.model.Question;
import com.primefactorsolutions.service.AssessmentService;
import com.primefactorsolutions.service.QuestionService;
import com.primefactorsolutions.service.CandidateService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.fields.SubListSelector;
import org.vaadin.firitin.form.BeanValidationForm;

import java.util.List;
import java.util.UUID;

@SpringComponent
@PermitAll
@Scope("prototype")
@PageTitle("Assessments")
@Route(value = "/assessments", layout = MainLayout.class)
@Uses(ComboBox.class)
public class AssessmentView extends BeanValidationForm<Assessment> implements HasUrlParameter<String> {
    private final AssessmentService assessmentService;

    private ComboBox<Candidate> candidate = null;
    private SubListSelector<Question> questions = null;

    public AssessmentView(final AssessmentService assessmentService, final QuestionService questionService,
                          final CandidateService candidateService) {
        super(Assessment.class);

        this.assessmentService = assessmentService;

        candidate = new ComboBox<>("Candidate", candidateService.getCandidates());
        candidate.setItemLabelGenerator((ItemLabelGenerator<Candidate>) Candidate::getEmail);
        candidate.setReadOnly(false);

        questions = new SubListSelector<>(Question.class);
        questions.setItemLabelGenerator((ItemLabelGenerator<Question>) Question::getTitle);
        questions.setReadOnly(false);
        questions.setAvailableOptions(questionService.getQuestions());

        setSavedHandler((SavedHandler<Assessment>) assessment -> {
            final var saved = this.assessmentService.saveAssessment(assessment);
            setEntityWithEnabledSave(saved);
        });
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String s) {
        if (StringUtils.isNotBlank(s) && !"new".equals(s)) {
            final var assessment = assessmentService.getAssessment(UUID.fromString(s));

            setEntityWithEnabledSave(assessment);
        } else {
            setEntityWithEnabledSave(new Assessment());
        }
    }

    @Override
    protected List<Component> getFormComponents() {
        return List.of(candidate, questions);
    }
}
