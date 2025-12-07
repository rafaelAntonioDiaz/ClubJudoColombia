package com.RafaelDiaz.ClubJudoColombia.vista.layout;

import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.ComunidadView;
import com.RafaelDiaz.ClubJudoColombia.vista.JudokaDashboardView;
import com.RafaelDiaz.ClubJudoColombia.vista.JudokaPlanView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Layout base para ROLE_JUDOKA y ROLE_COMPETIDOR
 * Vaadin 24.8.4+ compatible – SIN StreamResource ni foto de perfil
 *
 * @author RafaelDiaz – Versión limpia y profesional 2025-11-20
 */
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public abstract class JudokaLayout extends AppLayout {

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;

    private Tabs menuTabs;

    @Autowired
    public JudokaLayout(SecurityService securityService, AccessAnnotationChecker accessChecker) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
    }

    @PostConstruct
    private void init() {
        crearNavbar();
        crearDrawerMenu();
        addClassName("judoka-layout");
    }

    private void crearNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menú");

        H2 tituloApp = new H2("Club Judo Colombia");
        tituloApp.addClassName("app-title");

        // Avatar simple con iniciales + menú usuario
        Avatar avatar = new Avatar();
        String nombreCompleto = securityService.getAuthenticatedJudoka()
                .map(j -> j.getUsuario().getNombre())
                .orElse("Judoka");
        avatar.setName(nombreCompleto);
        avatar.setColorIndex(nombreCompleto.hashCode() % 10); // Color bonito según nombre
        avatar.addClassName("judoka-avatar");

        MenuBar menuUsuario = new MenuBar();
        menuUsuario.addClassName("user-menu");
        var menuItem = menuUsuario.addItem(avatar);
        var subMenu = menuItem.getSubMenu();
        subMenu.addItem("Mi Perfil",
                e -> getUI().ifPresent(ui -> ui.navigate("perfil-judoka")));
        subMenu.addItem("Cerrar Sesión", e -> logout());

        HorizontalLayout navbar = new HorizontalLayout(toggle, tituloApp, menuUsuario);
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.expand(tituloApp);
        navbar.addClassName("judoka-navbar");

        addToNavbar(navbar);
    }

    private void crearDrawerMenu() {
        menuTabs = new Tabs();
        menuTabs.setOrientation(Tabs.Orientation.VERTICAL);
        menuTabs.addClassName("judoka-menu-tabs");

        // Ítems del menú (fácil de extender)
        agregarTab("Dashboard", VaadinIcon.DASHBOARD, JudokaDashboardView.class);
        // ...
        agregarTab("Mis Planes", VaadinIcon.CLIPBOARD_CHECK, JudokaPlanView.class);

        agregarTab("Comunidad", VaadinIcon.USERS, ComunidadView.class);
// --------------
        agregarTab("Historial", VaadinIcon.CHART_TIMELINE, "historial-evaluaciones");
// ...agregarTab("Mi Progreso", VaadinIcon.TROPHY, "progreso-judoka");

        VerticalLayout drawerContent = new VerticalLayout(menuTabs);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.addClassName("judoka-drawer");

        addToDrawer(drawerContent);
    }

    private void agregarTab(String titulo, VaadinIcon icono, Class<? extends Component> vista) {
        if (accessChecker.hasAccess(vista)) {
            RouterLink link = new RouterLink("", vista);
            link.add(new Icon(icono), new Span(titulo));
            link.addClassName("menu-link");
            menuTabs.add(new Tab(link));
        }
    }

    private void agregarTab(String titulo, VaadinIcon icono, String ruta) {
        RouterLink link = new RouterLink(titulo, JudokaDashboardView.class); // fallback seguro
        link.getElement().addEventListener("click", e -> getUI().ifPresent(ui -> ui.navigate(ruta)));
        link.add(new Icon(icono), new Span(titulo));
        link.addClassName("menu-link");
        menuTabs.add(new Tab(link));
    }

    private void logout() {
        getUI().ifPresent(ui -> {
            ui.getPage().setLocation("/logout"); // Spring Security lo maneja
            ui.getSession().close();
        });
    }

    protected void refreshMenu() {
        if (menuTabs != null) {
            menuTabs.removeAll();
            crearDrawerMenu();
        }
    }
}