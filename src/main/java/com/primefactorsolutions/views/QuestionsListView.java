package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Question;
import com.primefactorsolutions.service.QuestionService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.grid.VGrid;

import java.util.stream.Stream;

@SpringComponent
@Scope("prototype")
@PageTitle("Questions")
@Route(value = "/questions", layout = MainLayout.class)
@PermitAll
public class QuestionsListView extends BaseView {

    public QuestionsListView(final QuestionService questionService) {

        final HorizontalLayout hl = new HorizontalLayout();
        final Button addQuestion = new Button("Add Question");
        addQuestion.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent ->
                this.getUI().flatMap(ui -> ui.navigate(QuestionView.class, "new")));
        hl.add(addQuestion);

        final VGrid<Question> grid = new VGrid<>(Question.class);
        grid.setColumns("id", "title");
        grid.addComponentColumn((ValueProvider<Question, Component>) question -> {
            final Button edit = new Button("Edit");
            edit.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent ->
                    this.getUI().flatMap(ui -> ui.navigate(QuestionView.class, question.getId().toString())));
            return edit;
        });
        grid.setDataProvider(new DataProvider<>() {
            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(final Query<Question, Object> query) {
                return questionService.getQuestions().size();
            }

            @SuppressWarnings("unused")
            @Override
            public Stream<Question> fetch(final Query<Question, Object> query) {
                int limit = query.getLimit();
                int pagerSize = query.getPageSize();
                int page = query.getPage();
                return questionService.getQuestions().stream();
            }

            @Override
            public void refreshItem(final Question question) {
                // no-op
            }

            @Override
            public void refreshAll() {
                // no-op
            }

            @Override
            public Registration addDataProviderListener(final DataProviderListener<Question> dataProviderListener) {
                return null;
            }
        });
        grid.setAllRowsVisible(true);

        getCurrentPageLayout().add(hl, grid);
    }
}
