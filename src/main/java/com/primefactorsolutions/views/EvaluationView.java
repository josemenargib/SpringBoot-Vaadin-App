package com.primefactorsolutions.views;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.primefactorsolutions.model.*;
import com.primefactorsolutions.service.AssessmentService;
import com.primefactorsolutions.service.CompilerService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.DescriptionList.Description;
import com.vaadin.flow.component.html.DescriptionList.Term;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Height;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
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
@Route(value = "/evaluation", layout = MainLayout.class)
@AnonymousAllowed
@Slf4j
public class EvaluationView extends Main implements HasUrlParameter<String> {

    private final CompilerService compilerService;
    private final AssessmentService assessmentService;

    private AceEditor questionEditor = null;
    private Dialog dialog = null;
    private Dialog completeDialog = null;
    private AceEditor result = null;
    private Assessment assessment = null;
    private Submission currSubmission = null;
    private Boolean isCompleted = false;

    private Button start = null;
    private MenuItem prev = null;
    private MenuItem next = null;
    private MenuItem reset = null;
    private Section sidebar = null;
    private SimpleTimer timer = null;
    private DescriptionList dl = null;
    private Section editorSection = null;
    private Section startSection = null;
    private Section completedSection = null;
    private H3 questionTitle = null;
    private Text questionDescription = null;

    public EvaluationView(final CompilerService compilerService, final AssessmentService assessmentService) {
        this.compilerService = compilerService;
        this.assessmentService = assessmentService;

        addClassNames(Display.FLEX, Flex.GROW, Height.FULL);

        initStartSection();
        initCompletedSection();
        initEditorSection();
        initResultDialog();
        initCompleteDialog();
        initSidebar();

        add(completedSection, startSection, editorSection, sidebar, dialog);

        updateUI();
    }

    private void initResultDialog() {
        dialog = new Dialog();
        dialog.setHeaderTitle("Resultados");

        result = new AceEditor();
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

        final Button saveButton = new Button("Guardar y Siguiente", e -> {
            this.currSubmission.setResponse(this.questionEditor.getValue());
            this.assessmentService.saveSubmission(assessment.getId(), this.currSubmission);
            dialog.close();
            goToNext();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        final Button closeButton = new Button("Cerrar", e -> dialog.close());

        dialog.getFooter().add(closeButton, saveButton);
    }

    private void initCompleteDialog() {
        completeDialog = new Dialog();
        completeDialog.setHeaderTitle("Terminar Evaluacion");

        final VerticalLayout dialogLayout = new VerticalLayout(
                new Text("Desea terminar la evaluacion? No podra editar las respuestas despues de terminar."));
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle()
                .set("width", "800px")
                .set("max-width", "100%");

        completeDialog.add(dialogLayout);

        final Button completeButton = new Button("Terminar", e -> {
            completeDialog.close();
            this.currSubmission.setResponse(this.questionEditor.getValue());
            this.assessmentService.saveSubmission(assessment.getId(), this.currSubmission);
            this.assessment = assessmentService.completeAssessment(assessment.getId());
            goToCompleted();
            updateUI();
        });
        completeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        final Button closeButton = new Button("Cerrar", e -> completeDialog.close());

        completeDialog.getFooter().add(closeButton, completeButton);
    }

    private void initEditorSection() {
        editorSection = new Section();
        editorSection.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW, Height.FULL);

        VerticalLayout header = new VerticalLayout();

        questionTitle = new H3("");
        questionDescription = new Text("");
        header.add(questionTitle);
        header.add(questionDescription);

        questionEditor = getCodeEditor();

        final MenuBar runMenuBar = new MenuBar();
        runMenuBar.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);
        runMenuBar.addItem("Ejecutar/Compilar", (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
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
            this.currSubmission.setResponse(this.questionEditor.getValue());
            this.assessmentService.saveSubmission(assessment.getId(), this.currSubmission);
            this.currSubmission = this.assessmentService.getPrevSubmission(assessment.getId(), this.currSubmission);
            updateUI();
        });
        next = navMenuBar.addItem("Siguiente pregunta",
                (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
            this.currSubmission.setResponse(this.questionEditor.getValue());
            this.assessmentService.saveSubmission(assessment.getId(), this.currSubmission);
            goToNext();
        });
        reset = navMenuBar.addItem("Reiniciar pregunta (deshacer todos los cambios)",
                (ComponentEventListener<ClickEvent<MenuItem>>) menuItemClickEvent -> {
            this.currSubmission.setResponse(this.currSubmission.getQuestion().getContent());
            this.questionEditor.setValue(this.currSubmission.getQuestion().getContent());
        });

        final Div menuBar = new Div();
        menuBar.add(runMenuBar, navMenuBar);

        setInlineBlock(runMenuBar);
        setInlineBlock(navMenuBar);

        editorSection.add(header, menuBar, questionEditor);
    }

    private void initCompletedSection() {
        completedSection = new Section();
        completedSection.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW, Height.FULL);
        completedSection.setVisible(false);
        Div completedHelp = new Div();
        completedHelp.add(new Text("Evaluacion ha sido completada. Gracias!"));

        completedSection.add(completedHelp);
    }

    private void initStartSection() {
        startSection = new Section();
        startSection.addClassNames(Display.FLEX, FlexDirection.COLUMN, Flex.GROW, Height.FULL);

        Div startHelp = new Div();
        startHelp.add(new Text("Bienvenido(a) al examen de evaluacion de PFS. Ingrese su email y apriete el boton "
                + "'Empezar' para empezar la evaluacion. Tiene 30 minutos para completar."));
        startHelp.add(new HtmlComponent("br"));
        startHelp.add(new Text("La evaluacion consta de 2 (DOS) preguntas son de implementacion de codigo en JAVA. "
                + "Una vez empezada la evaluacion puede usar el boton 'Ejecutar' para compilar el codigo JAVA. "
                + "Tambien puede pasar un pregunta o volver a una pregunta anterior."));

        TextField tf = new TextField();
        tf.setRequiredIndicatorVisible(true);
        tf.getStyle().set("--vaadin-input-field-label-color", "--lumo-error-text-color");
        tf.setLabel("Ingrese su email (donde recibio el link de la evaluacion):");

        start = new Button("Empezar");
        start.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        start.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            this.assessment = this.assessmentService.startAssessment(this.assessment.getId());

            if (tf.getValue().trim().equalsIgnoreCase(this.assessment.getCandidate().getEmail())) {
                this.getUI().get().getPage().reload();
            } else {
                Notification notification = new Notification();
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);

                Div text = new Div(new Text("Email invalido. Verifique que el email corresponde al email "
                        + "donde recibio el enlace a la evaluacion."));

                Button closeButton = new Button(new Icon("lumo", "cross"));
                closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                closeButton.setAriaLabel("Close");
                closeButton.addClickListener(event -> {
                    notification.close();
                });

                HorizontalLayout layout = new HorizontalLayout(text, closeButton);
                layout.setAlignItems(FlexComponent.Alignment.CENTER);

                notification.add(layout);
                notification.open();
            }
        });

        startSection.add(startHelp, tf, start);
    }

    @NotNull
    private AceEditor getCodeEditor() {
        final AceEditor questionEditor;
        questionEditor = new AceEditor();
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
        Submission found = this.assessmentService.getNextSubmission(assessment.getId(),
                this.currSubmission.getId());

        if (found == null) {
            this.completeDialog.open();
        } else {
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

        final Text text = new Text("Tiempo restante:");

        timer = new SimpleTimer(0);
        timer.setMinutes(true);
        timer.addTimerEndEvent((ComponentEventListener<SimpleTimer.TimerEndedEvent>) timerEndedEvent -> {
            Notification.show("Tiempo completado.", 5_000, Notification.Position.TOP_CENTER);
            this.currSubmission.setResponse(this.questionEditor.getValue());
            this.assessmentService.saveSubmission(assessment.getId(), this.currSubmission);
            this.assessment = assessmentService.completeAssessment(assessment.getId());
            goToCompleted();
            updateUI();
        });
        timer.setFractions(false);

        final Button completeButton = new Button("Terminar evaluacion");
        completeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        completeButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            this.completeDialog.open();
        });

        // Add it all together
        sidebar.add(dl, text, timer, completeButton);
    }

    private void updateUI() {
        if (assessment == null || !assessment.isStarted()) {
            editorSection.setVisible(false);
            startSection.setVisible(true);
            sidebar.setVisible(false);
        } else {
            if (currSubmission != null) {
                questionEditor.setValue(this.currSubmission.getResponse());
                questionTitle.setText(this.currSubmission.getQuestion().getTitle());
                questionDescription.setText(this.currSubmission.getQuestion().getDescription());
            }

            editorSection.setVisible(true);
            startSection.setVisible(false);
            sidebar.setVisible(true);

            prev.setEnabled(currSubmission != null && !assessment.isFirst(currSubmission));
            next.setEnabled(currSubmission == null || !assessment.isLast(currSubmission));

            if (this.assessment.isCompleted()) {
                goToCompleted();
                this.editorSection.setVisible(false);
                this.sidebar.setVisible(false);
                this.startSection.setVisible(false);
                this.completedSection.setVisible(true);
            }

            if (dl.getChildren().collect(Collectors.toList()).isEmpty()) {
                dl.add(
                        createItem("Candidato:", assessment.getCandidate().getEmail()),
                        createItem("Hora de inicio:", Optional.ofNullable(assessment.getStartingTime())
                                .map(t -> ZonedDateTime.ofInstant(t,
                                        ZoneId.of("GMT-4")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .orElse("N/A"))
                );
            }

            final Long remainingTime = this.assessment.getRemainingTimeSeconds();
            timer.pause();
            timer.setStartTime(remainingTime > 0 ? remainingTime : 3);
            timer.setMinutes(true);
            timer.reset();
            timer.start();
        }
    }

    private Div createItem(final String label, final String value) {
        return new Div(createTerm(label), createDescription(value));
    }

    // private Div createBadgeItem(String label, String value) {
    //    return new Div(createTerm(label), createDescription(value, "badge"));
    // }

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

        if (this.assessment.isCompleted()) {
            goToCompleted();
        }

        this.currSubmission = this.assessment.isStarted()
                ? this.assessmentService.getNextSubmission(assessment.getId(), null)
                : null;

        updateUI();
    }

    private void goToCompleted() {
        this.isCompleted = true;
    }
}
