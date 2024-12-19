package com.primefactorsolutions.views;

import com.primefactorsolutions.model.Employee;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.UUID;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {
    private final transient AuthenticationContext authContext;

    private H1 viewTitle;

    public MainLayout(final AuthenticationContext authContext,
                      @Autowired @Value("${git.commit.id.abbrev}") final String commitId) {
        this.authContext = authContext;
        setPrimarySection(Section.DRAWER);
        addDrawerContent(commitId);
        addHeaderContent();
    }

    private void addHeaderContent() {
        final DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        final HorizontalLayout header = authContext.getAuthenticatedUser(UserDetails.class)
                .map(user -> {
                    String employeeId = "N/A";

                    if (user instanceof Employee) {
                        final UUID uuid = ((Employee) user).getId();

                        if (uuid != null) {
                            employeeId = uuid.toString();
                        }
                    }

                    final Avatar loggedUser = new Avatar(user.getUsername());
                    loggedUser.getStyle().set("display", "block");
                    loggedUser.getElement().setAttribute("tabindex", "-1");

                    final MenuBar menuBar = new MenuBar();
                    menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
                    final MenuItem actions = createIconItem(menuBar, loggedUser, null, employeeId);
                    final SubMenu actionsSubMenu = actions.getSubMenu();
                    final MenuItem signOutMenuItem = createIconItem(actionsSubMenu,
                            createIcon(VaadinIcon.EXIT, true), "Sign-out", null);
                    signOutMenuItem.addClickListener(c -> this.authContext.logout());

                    final HorizontalLayout hl = new HorizontalLayout(menuBar);
                    hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

                    return hl;
                }).orElseGet(HorizontalLayout::new);
        header.setAlignItems(FlexComponent.Alignment.STRETCH);
        header.setWidthFull();

        addToNavbar(true, toggle, viewTitle, header);
    }

    private MenuItem createIconItem(final HasMenuItems menu, final Component component,
                                    final String label, final String ariaLabel) {
        final MenuItem item = menu.addItem(component, e -> {
        });

        if (ariaLabel != null) {
            item.setAriaLabel(ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private Icon createIcon(final VaadinIcon iconName, final boolean isChild) {
        final Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().set("width", "var(--lumo-icon-size-s)");
            icon.getStyle().set("height", "var(--lumo-icon-size-s)");
            icon.getStyle().set("marginRight", "var(--lumo-space-s)");
        }

        return icon;
    }

    private void addDrawerContent(final String commitId) {
        final Span appName = new Span("pfs-intra");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        final Header header = new Header(appName);
        final Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter(commitId));
    }

    private SideNav createNavigation() {
        final SideNav nav = new SideNav();

        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(u -> {
            SideNavItem recruiting = new SideNavItem("Recruiting");
            recruiting.setPrefixComponent(LineAwesomeIcon.BUSINESS_TIME_SOLID.create());
            recruiting.addItem(new SideNavItem("Assessments", AssessmentsListView.class,
                    LineAwesomeIcon.RIBBON_SOLID.create()));
            recruiting.addItem(new SideNavItem("Candidates", CandidatesListView.class,
                    LineAwesomeIcon.USER.create()));
            recruiting.addItem(new SideNavItem("Questions", QuestionsListView.class,
                    LineAwesomeIcon.QUESTION_SOLID.create()));

            SideNavItem admin = new SideNavItem("Admin");
            admin.setPrefixComponent(LineAwesomeIcon.BUILDING.create());
            admin.addItem(new SideNavItem("Employees", EmployeesListView.class,
                    LineAwesomeIcon.USER_EDIT_SOLID.create()));
            admin.addItem(new SideNavItem("Documents", DocumentsListView.class,
                    LineAwesomeIcon.FILE_ALT_SOLID.create()));

            SideNavItem timeOff = new SideNavItem("My Time-off", TimeoffView.class,
                    LineAwesomeIcon.PLANE_DEPARTURE_SOLID.create());
            timeOff.addItem(new SideNavItem("Vacations", RequestsListView.class,
                    LineAwesomeIcon.UMBRELLA_BEACH_SOLID.create()));
            timeOff.addItem(new SideNavItem("Add Vacation", RequestRegisterView.class,
                    LineAwesomeIcon.CALENDAR_PLUS.create()));
            timeOff.addItem(new SideNavItem("Pending Requests", PendingRequestsListView.class,
                    LineAwesomeIcon.LIST_ALT.create()));
            SideNavItem timesheet = new SideNavItem("My Timesheet", TimesheetView.class,
                    LineAwesomeIcon.HOURGLASS_START_SOLID.create());
            timesheet.addItem(new SideNavItem("Registro de Horas Trabajadas", HoursWorkedListView.class,
                    LineAwesomeIcon.ID_CARD_SOLID.create()));
            timesheet.addItem(new SideNavItem("Reporte Horas Trabajadas", ReporteView.class,
                    LineAwesomeIcon.ID_CARD_SOLID.create()));

            SideNavItem profile = new SideNavItem("My Profile", ProfileView.class,
                    LineAwesomeIcon.USER_EDIT_SOLID.create());

            nav.addItem(new SideNavItem("Home", MainView.class, LineAwesomeIcon.HOME_SOLID.create()));
            nav.addItem(admin);
            nav.addItem(recruiting);
            nav.addItem(profile);
            nav.addItem(timesheet);
            nav.addItem(timeOff);
        });

        return nav;
    }

    private Footer createFooter(final String commitId) {
        return new Footer(new Text(String.format("v.%s", commitId)));
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
