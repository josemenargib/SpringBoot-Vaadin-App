package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Candidate;
import com.primefactorsolutions.service.CandidateService;
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
@PageTitle("Candidates")
@Route(value = "/candidates", layout = MainLayout.class)
@PermitAll
public class CandidatesListView extends BaseView {

    public CandidatesListView(final CandidateService candidateService) {

        final HorizontalLayout hl = new HorizontalLayout();
        final Button addCandidate = new Button("Add Candidate");
        addCandidate.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            this.getUI().flatMap(ui -> ui.navigate(CandidateView.class, "new"));
        });
        hl.add(addCandidate);

        final VGrid<Candidate> grid = new VGrid<>(Candidate.class);
        grid.setColumns("id", "email");
        grid.setAllRowsVisible(true);
        grid.addComponentColumn((ValueProvider<Candidate, Component>) candidate -> {
            final Button edit = new Button("Edit");
            edit.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent ->
                    this.getUI().flatMap(ui -> ui.navigate(CandidateView.class, candidate.getId().toString())));
            return edit;
        });

        grid.setDataProvider(new DataProvider<>() {
            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(final Query<Candidate, Object> query) {
                return candidateService.getCandidates().size();
            }

            @SuppressWarnings("unused")
            @Override
            public Stream<Candidate> fetch(final Query<Candidate, Object> query) {
                int limit = query.getLimit();
                int pagerSize = query.getPageSize();
                int page = query.getPage();
                return candidateService.getCandidates().stream();
            }

            @Override
            public void refreshItem(final Candidate candidate) {
                // no-op
            }

            @Override
            public void refreshAll() {
                // no-op
            }

            @Override
            public Registration addDataProviderListener(final DataProviderListener<Candidate> dataProviderListener) {
                return null;
            }
        });

        getCurrentPageLayout().add(hl, grid);
    }
}
