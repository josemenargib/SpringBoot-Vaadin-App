package com.primefactorsolutions.views;

import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.primefactorsolutions.model.Assessment;
import com.primefactorsolutions.model.Submission;
import com.primefactorsolutions.service.AssessmentService;
import com.primefactorsolutions.service.CompilerService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.DescriptionList.Description;
import com.vaadin.flow.component.html.DescriptionList.Term;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@PageTitle("Evaluacion")
@SpringComponent
@Scope("prototype")
@Route(value = "/submission", layout = MainLayout.class)
@AnonymousAllowed
@Slf4j
public class SubmissionView extends Main implements HasUrlParameter<String> {

    private final CompilerService compilerService;
    private final AssessmentService assessmentService;

    private AceEditor questionEditor = null;
    private AceEditor result = null;
    private Dialog dialog = null;
    private Assessment assessment = null;
    private Submission currSubmission = null;
    private MenuItem prev = null;
    private MenuItem next = null;
    private Section sidebar = null;
    private DescriptionList dl = null;
    private Section editorSection = null;
    private H3 questionTitle = null;

    public SubmissionView(final CompilerService compilerService, final AssessmentService assessmentService) {
        this.compilerService = compilerService;
        this.assessmentService = assessmentService;

        addClassNames(Display.FLEX, Flex.GROW, Height.FULL);

        initResultDialog();
        initEditorSection();
        initSidebar();

        add(editorSection, sidebar);

        updateUI();
    }

    private void initResultDialog() {
        dialog = new Dialog();
        dialog.setHeaderTitle("Resultados");

        result = new AceEditor();
        result.setEnabled(false);
        result.setTheme(AceTheme.xcode);
        result.setMode(AceMode.text);
        result.setFontSize(12);
        result.setHeight("100%");
        result.setWidth("100%");
        result.setReadOnly(true);
        result.setMinlines(20);
        result.setMaxlines(30);
        result.setDisplayIndentGuides(false);
        result.setShowGutter(false);

        final VerticalLayout dialogLayout = new VerticalLayout(result);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle()
                .set("width", "800px")
                .set("max-width", "100%");

        dialog.add(dialogLayout);
        final Button closeButton = new Button("Cerrar", e -> dialog.close());

        dialog.getFooter().add(closeButton);
    }

    private void initEditorSection() {
        editorSection = new Section();
        editorSection.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW, Height.FULL);

        VerticalLayout header = new VerticalLayout();

        questionTitle = new H3("");
        header.add(questionTitle);

        questionEditor = getCodeEditor();

        final MenuBar runMenuBar = new MenuBar();
        runMenuBar.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);
        runMenuBar.addItem("Ejecutar/Compilar",
                (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
            final String javaCode = questionEditor.getValue();
            final Optional<String> result = this.compilerService.doCompile(javaCode);

            if (result.isPresent()) {
                this.result.setValue(result.get());
                this.dialog.open();
            }
        });

        final MenuBar navMenuBar = new MenuBar();
        prev = navMenuBar.addItem("Anterior pregunta",
                (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
            log.info(">>> prev");
            this.currSubmission = this.assessmentService.getPrevSubmission(assessment.getId(),
                    this.currSubmission);
            updateUI();
        });
        next = navMenuBar.addItem("Siguiente pregunta",
                (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
            this.currSubmission.setResponse(this.questionEditor.getValue());
            goToNext();
        });

        final Div menuBar = new Div();
        menuBar.add(runMenuBar, navMenuBar);

        setInlineBlock(runMenuBar);
        setInlineBlock(navMenuBar);

        editorSection.add(header, menuBar, questionEditor);
    }

    @NotNull
    private AceEditor getCodeEditor() {
        final AceEditor questionEditor;
        questionEditor = new AceEditor();
        questionEditor.setEnabled(false);
        questionEditor.setTheme(AceTheme.xcode);
        questionEditor.setMode(AceMode.java);
        questionEditor.setFontSize(15);
        questionEditor.setHeight("100%");
        questionEditor.setWidth("100%");

        questionEditor.setReadOnly(false);
        questionEditor.setShowInvisibles(true);
        questionEditor.setShowGutter(false);
        questionEditor.setShowPrintMargin(false);
        questionEditor.setDisplayIndentGuides(false);
        questionEditor.setUseWorker(false);

        questionEditor.setSofttabs(true);
        questionEditor.setTabSize(4);
        questionEditor.setWrap(true);
        questionEditor.setMinlines(60);
        questionEditor.setMaxlines(80);
        questionEditor.setAutoComplete(false);
        questionEditor.setHighlightActiveLine(true);
        questionEditor.setHighlightSelectedWord(false);

        return questionEditor;
    }

    private void setInlineBlock(final MenuBar menuBar) {
        menuBar.getStyle().set("display", "inline-block");
    }

    private void goToNext() {
        log.info(">>> next");
        Submission found = this.assessmentService.getNextSubmission(assessment.getId(),
                this.currSubmission.getId(), false);

        if (found != null) {
            this.currSubmission = found;
            updateUI();
        }
    }

    private void initSidebar() {
        sidebar = new Section();
        sidebar.addClassNames(Background.CONTRAST_5, BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN,
                Flex.SHRINK_NONE, Overflow.AUTO, Padding.LARGE);
        sidebar.setWidth("256px");

        dl = new DescriptionList();
        dl.addClassNames(Display.FLEX, FlexDirection.COLUMN, Gap.LARGE, Margin.Bottom.SMALL, Margin.Top.NONE,
                FontSize.SMALL);

        sidebar.add(dl);
    }

    private void updateUI() {
        if (assessment == null || !assessment.isStarted()) {
            editorSection.setVisible(false);
            sidebar.setVisible(false);
        } else {
            if (currSubmission != null) {
                questionEditor.setValue(this.currSubmission.getResponse());
                questionTitle.setText(this.currSubmission.getQuestion().getTitle());
            }

            editorSection.setVisible(true);
            sidebar.setVisible(true);

            prev.setEnabled(currSubmission != null && !assessment.isFirst(currSubmission));
            next.setEnabled(currSubmission == null || !assessment.isLast(currSubmission));

            if (dl.getChildren().collect(Collectors.toList()).isEmpty()) {
                dl.add(
                        createItem("Candidato:", assessment.getCandidate().getEmail()),
                        createItem("Hora de inicio:", Optional.ofNullable(assessment.getStartingTime())
                                .map(t -> ZonedDateTime.ofInstant(t,
                                        ZoneId.of("GMT-4")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .orElse("N/A"))
                );
            }
        }
    }

    private Div createItem(final String label, final String value) {
        return new Div(createTerm(label), createDescription(value));
    }

    private Term createTerm(final String label) {
        Term term = new Term(label);
        term.addClassNames(FontWeight.MEDIUM, TextColor.SECONDARY);
        return term;
    }

    private Description createDescription(final String value, final String... themeNames) {
        Description desc = new Description(value);
        desc.addClassName(Margin.Left.NONE);

        for (String themeName : themeNames) {
            desc.getElement().getThemeList().add(themeName);
        }

        return desc;
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String s) {
        this.assessment = this.assessmentService.getAssessment(UUID.fromString(s));

        if (this.assessment == null) {
            throw new NotFoundException();
        }

        this.currSubmission = this.assessment.isStarted()
                ? this.assessmentService.getNextSubmission(assessment.getId(), null, false)
                : null;

        updateUI();
    }
}
