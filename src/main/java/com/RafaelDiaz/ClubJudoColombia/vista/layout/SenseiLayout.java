package com.RafaelDiaz.ClubJudoColombia.vista.layout;

import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class SenseiLayout extends AppLayout {

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;
    private final ConfiguracionService configuracionService;
    private final AuthenticationContext authenticationContext;
    public SenseiLayout(SecurityService securityService,
                        AccessAnnotationChecker accessChecker,
                        ConfiguracionService configuracionService, AuthenticationContext authenticationContext) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
        this.configuracionService = configuracionService;
        this.authenticationContext = authenticationContext;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Sensei Panel");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        // Avatar del Sensei
        String username = securityService.getAuthenticatedUserDetails().isPresent()
                ? securityService.getAuthenticatedUserDetails().get().getUsername() : "Sensei";
        Avatar avatar = new Avatar(username);
        avatar.addClassNames(LumoUtility.Margin.Right.MEDIUM);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, avatar);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        // 1. DASHBOARD
        agregarTab(tabs, "Dashboard", VaadinIcon.DASHBOARD, SenseiDashboardView.class);

        agregarTab(tabs,"Comunidad", VaadinIcon.USERS, ComunidadSenseiView.class);


        // 2. GESTIÓN DE PERSONAS
        agregarTab(tabs, "Admisiones", VaadinIcon.USER_CLOCK, ValidacionIngresoView.class);
        agregarTab(tabs, "Grupos", VaadinIcon.GROUP, SenseiGruposView.class);
        agregarTab(tabs, "Control Asistencia", VaadinIcon.CHECK_SQUARE_O, RegistroAsistenciaView.class);

        // 3. GESTIÓN TÉCNICA (Aquí va la Biblioteca)
        // --- NUEVO ENLACE ---
        agregarTab(tabs, "Biblioteca Ejercicios", VaadinIcon.BOOK, BibliotecaView.class);
        // --------------------

        agregarTab(tabs, "Planes de Entreno", VaadinIcon.CLIPBOARD_CHECK, SenseiPlanView.class);
        agregarTab(tabs, "Resultados Tests", VaadinIcon.TROPHY, SenseiResultadosView.class);
        agregarTab(tabs, "Campeonatos", VaadinIcon.TROPHY, SenseiCampeonatosView.class);
        agregarTab(tabs, "Campos & Ascensos", VaadinIcon.MEDAL, SenseiCamposView.class);

        // 4. MÓDULO GPS
        agregarTab(tabs, "Agenda GPS", VaadinIcon.MAP_MARKER, SenseiAgendaView.class);

        // 5. MÓDULO FINANCIERO (Solo si es CLUB)
        if (configuracionService.esClub()) {
            agregarTab(tabs, "Tesorería", VaadinIcon.MONEY_EXCHANGE, TesoreriaView.class);
        }

        // 6. ADMINISTRACIÓN
        agregarTab(tabs, "Configuración", VaadinIcon.COGS, AdministracionView.class);

        // SALIR
        Tab logoutTab = new Tab(createLogoutLink());
        tabs.add(logoutTab);

        addToDrawer(new VerticalLayout(tabs));
    }    private void agregarTab(Tabs tabs, String label, VaadinIcon icon, Class<? extends Component> view) {
        if (accessChecker.hasAccess(view)) {
            RouterLink link = new RouterLink(view);
            link.add(new Icon(icon), new Span(label));
            tabs.add(new Tab(link));
        }
    }

    private RouterLink createLogoutLink() {
        RouterLink link = new RouterLink();
        link.add(new Icon(VaadinIcon.SIGN_OUT), new Span("Salir"));
        // Hack para logout simple
        link.getElement().addEventListener("click", e -> {
            securityService.logout();
        });
        return link;
    }
}