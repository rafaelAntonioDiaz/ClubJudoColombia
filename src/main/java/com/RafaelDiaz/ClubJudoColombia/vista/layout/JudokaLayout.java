package com.RafaelDiaz.ClubJudoColombia.vista.layout;

import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.ComunidadJudokaView;
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

@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public class JudokaLayout extends AppLayout {

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;
    private final TraduccionService traduccionService;

    private Tabs menuTabs;

    @Autowired
    public JudokaLayout(SecurityService securityService,
                        AccessAnnotationChecker accessChecker,
                        TraduccionService traduccionService) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
        this.traduccionService = traduccionService;
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

        // 1. Título App (Ahora sí encontrará la clave 'app.nombre')
        H2 tituloApp = new H2(traduccionService.get("app.nombre"));
        tituloApp.addClassName("app-title");

        // Datos del Usuario
        String nombreCompleto = securityService.getAuthenticatedJudoka()
                .map(j -> j.getUsuario().getNombre())
                .orElse("Judoka");

        // 2. SALUDO PERSONALIZADO (Corregido)
        // Usamos el método sobrecargado que añadimos al servicio
        Span saludo = new Span(traduccionService.get("dashboard.welcome", nombreCompleto));
        saludo.addClassName("layout-welcome-text");
        saludo.getStyle()
                .set("font-size", "0.9rem")
                .set("margin-right", "15px")
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)");

        // Avatar
        Avatar avatar = new Avatar();
        avatar.setName(nombreCompleto);
        avatar.setColorIndex(nombreCompleto.hashCode() % 10);
        avatar.addClassName("judoka-avatar");

        MenuBar menuUsuario = new MenuBar();
        menuUsuario.addClassName("user-menu");
        var menuItem = menuUsuario.addItem(avatar);
        var subMenu = menuItem.getSubMenu();

        subMenu.addItem(traduccionService.get("menu.mi.perfil"),
                e -> getUI().ifPresent(ui -> ui.navigate("perfil-judoka")));

        subMenu.addItem(traduccionService.get("btn.cerrar.sesion"), e -> logout());

        // Agregamos el saludo al layout: [Toggle] [Título] .... [Saludo] [Avatar]
        HorizontalLayout navbar = new HorizontalLayout(toggle, tituloApp, saludo, menuUsuario);
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Empujamos el título para que ocupe el espacio y mande el resto a la derecha
        // Pero ojo: si expandimos tituloApp, 'saludo' se irá a la derecha junto con el avatar
        navbar.expand(tituloApp);

        navbar.addClassName("judoka-navbar");

        addToNavbar(navbar);
    }

    private void crearDrawerMenu() {
        menuTabs = new Tabs();
        menuTabs.setOrientation(Tabs.Orientation.VERTICAL);
        menuTabs.addClassName("judoka-menu-tabs");

        agregarTab(traduccionService.get("menu.dashboard"), VaadinIcon.DASHBOARD, JudokaDashboardView.class);
        agregarTab(traduccionService.get("menu.mis.planes"), VaadinIcon.CLIPBOARD_CHECK, JudokaPlanView.class);
        agregarTab(traduccionService.get("menu.comunidad"), VaadinIcon.USERS, ComunidadJudokaView.class);

        Tab logoutTab = new Tab(createLogoutLink());
        menuTabs.add(logoutTab);

        VerticalLayout drawerContent = new VerticalLayout(menuTabs);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.addClassName("judoka-drawer");

        addToDrawer(drawerContent);
    }

    private void agregarTab(String titulo, VaadinIcon icono, Class<? extends Component> vista) {
        if (accessChecker.hasAccess(vista)) {
            RouterLink link = new RouterLink(vista);
            link.add(new Icon(icono), new Span(titulo));
            link.addClassName("menu-link");
            menuTabs.add(new Tab(link));
        }
    }

    private RouterLink createLogoutLink() {
        RouterLink link = new RouterLink();
        link.add(new Icon(VaadinIcon.SIGN_OUT), new Span(traduccionService.get("btn.cerrar.sesion")));
        link.addClassName("menu-link");
        link.getElement().addEventListener("click", e -> logout());
        return link;
    }

    private void logout() {
        securityService.logout();
    }
}