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

/**
 * Layout base para ROLE_JUDOKA y ROLE_COMPETIDOR.
 * VERIFICADO:
 * 1. Clase no abstracta.
 * 2. Incluye TraduccionService.
 * 3. Mantiene TODOS los métodos originales (incluyendo sobrecargas).
 * 4. Implementa Logout estilo Sensei.
 */
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

        // i18n: Título App
        H2 tituloApp = new H2(traduccionService.get("app.nombre"));
        tituloApp.addClassName("app-title");

        // Avatar
        Avatar avatar = new Avatar();
        String nombreCompleto = securityService.getAuthenticatedJudoka()
                .map(j -> j.getUsuario().getNombre())
                .orElse("Judoka");
        avatar.setName(nombreCompleto);
        avatar.setColorIndex(nombreCompleto.hashCode() % 10);
        avatar.addClassName("judoka-avatar");

        MenuBar menuUsuario = new MenuBar();
        menuUsuario.addClassName("user-menu");
        var menuItem = menuUsuario.addItem(avatar);
        var subMenu = menuItem.getSubMenu();

        // i18n: Menú usuario
        subMenu.addItem(traduccionService.get("menu.mi.perfil"),
                e -> getUI().ifPresent(ui -> ui.navigate("perfil-judoka")));

        // Mantenemos la llamada al método logout() original
        subMenu.addItem(traduccionService.get("btn.cerrar.sesion"), e -> logout());

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

        // i18n: Items del menú
        agregarTab(traduccionService.get("menu.dashboard"), VaadinIcon.DASHBOARD, JudokaDashboardView.class);
        agregarTab(traduccionService.get("menu.mis.planes"), VaadinIcon.CLIPBOARD_CHECK, JudokaPlanView.class);
        agregarTab(traduccionService.get("menu.comunidad"), VaadinIcon.USERS, ComunidadJudokaView.class);

        // Logout en el Drawer (Estilo Sensei)
        Tab logoutTab = new Tab(createLogoutLink());
        menuTabs.add(logoutTab);

        VerticalLayout drawerContent = new VerticalLayout(menuTabs);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.addClassName("judoka-drawer");

        addToDrawer(drawerContent);
    }

    /**
     * Método Original 1: Agregar Tab por Clase de Vista
     */
    private void agregarTab(String titulo, VaadinIcon icono, Class<? extends Component> vista) {
        if (accessChecker.hasAccess(vista)) {
            RouterLink link = new RouterLink(vista);
            link.add(new Icon(icono), new Span(titulo));
            link.addClassName("menu-link");
            menuTabs.add(new Tab(link));
        }
    }

    /**
     * Método Original 2 (Recuperado): Agregar Tab por String (Ruta)
     * Útil si necesitas enlaces externos o rutas manuales.
     */
    private void agregarTab(String titulo, VaadinIcon icono, String ruta) {
        RouterLink link = new RouterLink();
        // Listener para navegación manual
        link.getElement().addEventListener("click", e -> getUI().ifPresent(ui -> ui.navigate(ruta)));
        link.add(new Icon(icono), new Span(titulo));
        link.addClassName("menu-link");
        menuTabs.add(new Tab(link));
    }

    /**
     * Método Nuevo (Auxiliar): Crea el link de logout estilo SenseiLayout
     */
    private RouterLink createLogoutLink() {
        RouterLink link = new RouterLink();
        link.add(new Icon(VaadinIcon.SIGN_OUT), new Span(traduccionService.get("btn.cerrar.sesion")));
        link.addClassName("menu-link");
        // Reutilizamos el método logout() original
        link.getElement().addEventListener("click", e -> logout());
        return link;
    }

    /**
     * Método Original: Logout
     * Actualizado para usar securityService en lugar de redirección manual,
     * pero manteniendo la firma del método.
     */
    private void logout() {
        securityService.logout();
    }

    /**
     * Método Original: Refresh Menu
     */
    protected void refreshMenu() {
        if (menuTabs != null) {
            menuTabs.removeAll();
            crearDrawerMenu();
        }
    }
}