package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Question;
import com.primefactorsolutions.service.QuestionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
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
@Route(value = "/questions", layout = MainLayout.class)
@PermitAll
public class QuestionView extends BeanValidationForm<Question> implements HasUrlParameter<String> {
    private final QuestionService questionService;

    private TextField title = null;
    private TextArea description = null;
    private TextArea content = null;
    private IntegerField timeMinutes = null;

    public QuestionView(final QuestionService questionService) {
        super(Question.class);
        this.questionService = questionService;
        title = new TextField();
        title.setWidthFull();
        title.setLabel("Title");

        description = new TextArea();
        description.setWidthFull();
        description.setLabel("Description");

        timeMinutes = new IntegerField();
        timeMinutes.setLabel("TimeMinutes");

        content = new TextArea();
        content.setWidthFull();
        content.setLabel("Content");

        setSavedHandler((SavedHandler<Question>) question -> {
            final Question saved = questionService.createOrUpdate(question);
            setEntityWithEnabledSave(saved);
        });
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String s) {
        if (StringUtils.isNotBlank(s) && !"new".equals(s)) {
            var question = questionService.getQuestion(UUID.fromString(s));
            setEntityWithEnabledSave(question);
        } else if ("new".equals(s)) {
            setEntityWithEnabledSave(new Question());
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    protected List<Component> getFormComponents() {
        return List.of(title, description, timeMinutes, content);
    }
}
