package com.RafaelDiaz.ClubJudoColombia.vista.layout;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Layout base para todas las vistas del ROLE_SENSEI.
 * Proporciona navegación lateral, menú de usuario y estructura consistente.
 *
 * <p><b>Características:</b>
 * <ul>
 *   <li>Menú lateral con iconos y tooltips</li>
 *   <li>Avatar del Sensei con submenú (Perfil, Logout)</li>
 *   <li>Tema de colores del club (Rojo/Negro/Blanco)</li>
 *   <li>Responsive: Se colapsa en móviles</li>
 * </ul>
 *
 * <p><b>Uso:</b> Todas las vistas del Sensei deben extender esta clase.
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-19
 */
@RolesAllowed("ROLE_SENSEI")
public abstract class SenseiLayout extends AppLayout {

    private static final Logger logger = LoggerFactory.getLogger(SenseiLayout.class);

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;

    private Tabs menuTabs;
    private Avatar userAvatar;
    private Span userNameSpan;

    @Autowired
    public SenseiLayout(SecurityService securityService, AccessAnnotationChecker accessChecker) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
    }

    /**
     * Inicializa el layout después de la inyección de dependencias.
     * Crea la navbar y el menú lateral.
     */
    @PostConstruct
    private void init() {
        try {
            createNavBar();
            createDrawerMenu();
            logger.info("SenseiLayout inicializado correctamente");
        } catch (Exception e) {
            logger.error("Error al inicializar SenseiLayout", e);
            throw new RuntimeException("No se pudo inicializar el layout del Sensei", e);
        }
    }

    /**
     * Crea la barra superior con título, toggle del menú y menú de usuario.
     */
    private void createNavBar() {
        // Componente para alternar el menú lateral (responsive)
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("sensei-layout-toggle");

        // Logo y título del club
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.addClassName("sensei-layout-title");

        // Si tienes un logo.png en src/main/resources/META-INF/resources/images/
        // Descomenta las siguientes líneas:
        // Image logo = new Image("images/logo.png", "Club Judo Colombia");
        // logo.setHeight("40px");
        // titleLayout.add(logo);

        H1 title = new H1("Club Judo Colombia");
        title.addClassName("sensei-layout-title-text");
        titleLayout.add(title);

        // Espaciador
        Div spacer = new Div();
        //spacer.setFlexGrow(1);

        // Menú de usuario (avatar + dropdown)
        HorizontalLayout userMenu = createUserMenu();
        userMenu.addClassName("sensei-layout-user-menu");

        // Agregar a la navbar
        addToNavbar(toggle, titleLayout, spacer, userMenu);
    }

    /**
     * Crea el menú de usuario con avatar y submenú.
     * @return Layout horizontal con el avatar y nombre del Sensei
     */
    private HorizontalLayout createUserMenu() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);

        // Avatar con iniciales del Sensei
        userAvatar = new Avatar();
        userAvatar.addClassName("sensei-layout-avatar");

        // Nombre del Sensei (obtenido de forma lazy)
        userNameSpan = new Span();
        userNameSpan.addClassName("sensei-layout-user-name");

        // Menú desplegable
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_TERTIARY_INLINE);

        var userItem = menuBar.addItem(new HorizontalLayout(userAvatar, userNameSpan, new Icon(VaadinIcon.CHEVRON_DOWN_SMALL)));
        userItem.addClassName("sensei-layout-user-item");

        SubMenu subMenu = userItem.getSubMenu();
        subMenu.addItem("Mi Perfil", e -> navigateToPerfil());
        subMenu.addItem("Cerrar Sesión", e -> logout());

        layout.add(menuBar);
        return layout;
    }

    /**
     * Carga los datos del Sensei autenticado en el menú de usuario.
     * Se llama después de que el layout está montado.
     */
    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Ejecutar en el UI thread para evitar problemas con lazy loading
        attachEvent.getUI().beforeClientResponse(this, context -> {
            securityService.getAuthenticatedSensei().ifPresent(sensei -> {
                String nombreCompleto = String.format("%s %s",
                        sensei.getUsuario().getNombre(),
                        sensei.getUsuario().getApellido());

                userNameSpan.setText(nombreCompleto);
                userAvatar.setName(nombreCompleto);

                // Si el Sensei tiene foto de perfil, úsala:
                // if (sensei.getRutaFoto() != null) {
                //     userAvatar.setImage(sensei.getRutaFoto());
                // }
            });
        });
    }

    /**
     * Crea el menú lateral con las vistas disponibles para el Sensei.
     * Solo muestra las vistas a las que el rol tiene acceso.
     */
    private void createDrawerMenu() {
        VerticalLayout menuLayout = new VerticalLayout();
        menuLayout.addClassName("sensei-layout-drawer");
        menuLayout.setSpacing(false);
        menuLayout.setPadding(false);

        // Secciones del menú
        menuLayout.add(createSection("Gestión", createGestionTabs()));
        menuLayout.add(createSection("Evaluación", createEvaluacionTabs()));
        menuLayout.add(createSection("Biblioteca", createBibliotecaTabs()));

        // Información del footer
        Span version = new Span("Versión 1.0.0");
        version.addClassName("sensei-layout-version");
        menuLayout.add(version);

        addToDrawer(menuLayout);
    }

    /**
     * Crea una sección del menú con título y tabs.
     */
    private VerticalLayout createSection(String title, Tabs tabs) {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("sensei-layout-section");
        section.setSpacing(false);

        Span sectionTitle = new Span(title);
        sectionTitle.addClassName("sensei-layout-section-title");

        section.add(sectionTitle, tabs);
        return section;
    }

    /**
     * Crea las tabs de gestión (Grupos, Planes).
     */
    private Tabs createGestionTabs() {
        menuTabs = new Tabs();
        menuTabs.setOrientation(Tabs.Orientation.VERTICAL);
        menuTabs.addClassName("sensei-layout-tabs");

        // Cada tab verifica permisos con AccessAnnotationChecker
        addTabIfAllowed(menuTabs, "Grupos", VaadinIcon.GROUP, "gestion-grupos");
        addTabIfAllowed(menuTabs, "Planes", VaadinIcon.CALENDAR, "gestion-planes");

        return menuTabs;
    }

    /**
     * Crea las tabs de evaluación (Resultados, Revisión).
     */
    private Tabs createEvaluacionTabs() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        addTabIfAllowed(tabs, "Resultados", VaadinIcon.CHART_LINE, "registrar-resultados");
        addTabIfAllowed(tabs, "Revisión Tareas", VaadinIcon.TASKS, "revision-tareas");

        return tabs;
    }

    /**
     * Crea las tabs de biblioteca (Tareas, Pruebas).
     */
    private Tabs createBibliotecaTabs() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        addTabIfAllowed(tabs, "Tareas Diarias", VaadinIcon.BOOK, "biblioteca-tareas");
        // addTabIfAllowed(tabs, "Pruebas", VaadinIcon.CLIPBOARD_CHECK, "biblioteca-pruebas");

        return tabs;
    }

    /**
     * Agrega una tab al menú SOLO si el rol tiene acceso a esa ruta.
     * Usa RouteConfiguration para obtener la clase destino correctamente.
     *
     * @param tabs Contenedor de tabs
     * @param titulo Texto visible en el menú
     * @param icono Icono de Vaadin
     * @param ruta Ruta de navegación (ej. "gestion-grupos")
     */
    private void addTabIfAllowed(Tabs tabs, String titulo, VaadinIcon icono, String ruta) {
        try {
            // 1. Obtener la clase del componente desde la ruta String
            RouteConfiguration config = RouteConfiguration.forSessionScope();
            Optional<Class<? extends Component>> targetClassOpt = config.getRoute(ruta);

            if (targetClassOpt.isEmpty()) {
                logger.warn("Ruta '{}' no encontrada en configuración de Vaadin", ruta);
                return;
            }

            Class<? extends Component> targetClass = targetClassOpt.get();

            // 2. Verificar acceso con Spring Security (requiere AccessAnnotationChecker)
            if (accessChecker.hasAccess(targetClass)) {
                // 3. Crear RouterLink con la clase correcta
                RouterLink link = new RouterLink(titulo, targetClass);
                link.add(new Icon(icono), new Span(titulo));
                link.addClassName("sensei-layout-tab");

                Tab tab = new Tab(link);
                tabs.add(tab);
                logger.debug("Tab '{}' añadida al menú del Sensei", titulo);
            } else {
                logger.debug("Tab '{}' omitida: El rol no tiene acceso", titulo);
            }

        } catch (Exception e) {
            logger.error("Error al añadir tab '{}': {}", titulo, e.getMessage(), e);
        }
    }

    /**
     * Navega a la página de perfil del Sensei.
     * Implementa esta vista en el futuro.
     */
    private void navigateToPerfil() {
        getUI().ifPresent(ui -> {
            ui.navigate("perfil-sensei");
            logger.info("Navegando al perfil del Sensei");
        });
    }

    /**
     * Cierra la sesión del usuario.
     * Redirige a la página de login.
     */
    private void logout() {
        getUI().ifPresent(ui -> {
            ui.getPage().setLocation("/login");
            // Invalidar la sesión de Spring Security
            ui.getSession().close();
            logger.info("Sensei cerró sesión");
        });
    }

    /**
     * Método para que las vistas hijas puedan refrescar el menú.
     * Útil después de cambios de permisos.
     */
    protected void refreshMenu() {
        if (menuTabs != null) {
            menuTabs.removeAll();
            createGestionTabs();
            logger.debug("Menú del Sensei refrescado");
        }
    }
}