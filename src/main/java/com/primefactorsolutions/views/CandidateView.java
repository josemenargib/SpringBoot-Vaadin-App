package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Candidate;
import com.primefactorsolutions.service.CandidateService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.form.BeanValidationForm;

import java.util.List;
import java.util.UUID;

@SpringComponent
@Scope("prototype")
@PageTitle("Assessments")
@Route(value = "/candidates", layout = MainLayout.class)
@PermitAll
public class CandidateView extends BeanValidationForm<Candidate> implements HasUrlParameter<String> {
    private final CandidateService candidateService;

    private EmailField email = null;

    public CandidateView(final CandidateService candidateService) {
        super(Candidate.class);
        this.candidateService = candidateService;
        email = new EmailField();
        email.setWidthFull();
        email.setLabel("Email");

        setSavedHandler((SavedHandler<Candidate>) candidate -> {
            final Candidate saved = candidateService.createOrUpdate(candidate);
            setEntityWithEnabledSave(saved);
        });
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String s) {
        if (StringUtils.isNotBlank(s) && !"new".equals(s)) {
            var candidate = candidateService.getCandidate(UUID.fromString(s));
            setEntityWithEnabledSave(candidate);
        } else {
            setEntityWithEnabledSave(new Candidate());
        }
    }

    @Override
    protected List<Component> getFormComponents() {
        return List.of(email);
    }
}
