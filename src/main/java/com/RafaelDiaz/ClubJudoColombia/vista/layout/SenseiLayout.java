package com.RafaelDiaz.ClubJudoColombia.vista.layout;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.*;
import com.RafaelDiaz.ClubJudoColombia.vista.admin.GestionSenseisView;
import com.RafaelDiaz.ClubJudoColombia.vista.component.IdiomaSelector;
import com.RafaelDiaz.ClubJudoColombia.vista.sensei.GestorInvitacionesView;
import com.RafaelDiaz.ClubJudoColombia.vista.sensei.PerfilSenseiView;
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
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct; // Vital para la inicialización segura
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class SenseiLayout extends AppLayout {

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;
    private final ConfiguracionService configuracionService;
    private final AuthenticationContext authenticationContext;

    @Autowired
    private TraduccionService traduccionService;

    /**
     * CONSTRUCTOR ORIGINAL (4 Parámetros)
     * Mantenido para compatibilidad con AsignacionJudokasView y otras subclases.
     */
    @Autowired
    public SenseiLayout(SecurityService securityService,
                        AccessAnnotationChecker accessChecker,
                        ConfiguracionService configuracionService,
                        AuthenticationContext authenticationContext) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
        this.configuracionService = configuracionService;
        this.authenticationContext = authenticationContext;

        // NOTA: No llamamos a createHeader() ni createDrawer() aquí
        // porque 'traduccionService' aún no ha sido inyectado por Spring.
        // Se llamarán automáticamente en el método init() gracias a @PostConstruct.
    }

    /**
     * Inicialización Post-Construcción.
     * Se ejecuta automáticamente cuando Spring ha terminado de inyectar todo.
     */
    @PostConstruct
    private void init() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        String tituloApp = getTexto("app.nombre", "Club Judo");
        H1 logo = new H1(tituloApp);
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);

        // Obtener perfil del sensei
        var profile = securityService.getAuthenticatedSenseiProfile();
        String nombreCompleto = profile.fullName();
        String fotoUrl = profile.avatarUrl();

        // Obtener club name (adicional)
        String clubName = securityService.getAuthenticatedSensei()
                .map(Sensei::getNombreClub)
                .orElse("");

        // Saludo personalizado
        Span saludo = new Span(getTexto("dashboard.welcome", "Hola") + " " + nombreCompleto);
        saludo.addClassName("layout-welcome-text");
        saludo.getStyle().set("font-size", "0.9rem").set("margin-right", "15px");

        // Mostrar club name si existe
        if (!clubName.isEmpty()) {
            Span clubSpan = new Span("(" + clubName + ")");
            clubSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
            saludo.add(clubSpan);
        }

        Avatar avatar = new Avatar();
        avatar.setName(nombreCompleto);
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            avatar.setImage(fotoUrl);
        }
        avatar.addClassNames(LumoUtility.Margin.Right.MEDIUM);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, saludo, new IdiomaSelector(), avatar);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        // --- 1. DASHBOARD & COMUNIDAD ---
        agregarTab(tabs, getTexto("menu.dashboard", "Dashboard"), VaadinIcon.DASHBOARD, SenseiDashboardView.class);
        agregarTab(tabs, getTexto("menu.comunidad", "Comunidad"), VaadinIcon.USERS, ComunidadSenseiView.class);
        agregarTab(tabs, getTexto("menu.reglas_gamificacion","Premios"), VaadinIcon.RANDOM, ReglasGamificacionView.class);
        // --- 2. GESTIÓN DE PERSONAS ---
        agregarTab(tabs, getTexto("menu.invitar", "Invitar"), VaadinIcon.PAPERPLANE, GestorInvitacionesView.class);
        agregarTab(tabs, getTexto("menu.admisiones", "Admisiones"), VaadinIcon.CLIPBOARD_USER, ValidacionIngresoView.class);
        agregarTab(tabs, getTexto("grupos.titulo", "Grupos"), VaadinIcon.GROUP, SenseiGruposView.class);
        //agregarTab(tabs, getTexto("menu.asistencia", "Asistencia"), VaadinIcon.CHECK_SQUARE_O, RegistroAsistenciaView.class);

        // --- 3. GESTIÓN TÉCNICA & EJECUCIÓN ---
        agregarTab(tabs, getTexto("menu.biblioteca", "Biblioteca"), VaadinIcon.BOOK, BibliotecaView.class);
        agregarTab(tabs, getTexto("view.sensei.microciclo.titulo", "Microciclos"), VaadinIcon.CLIPBOARD_CHECK, SenseiMicrociclosView.class);
        agregarTab(tabs, getTexto("view.sensei.macrociclo", "Macrociclo"), VaadinIcon.CLIPBOARD_CHECK, SenseiMacrocicloView.class);

        // >>> PARCHE AÑADIDO: MODO TATAMI Y BITÁCORA <<<
        agregarTab(tabs, getTexto("menu.tatami", "Modo Tatami"), VaadinIcon.PLAY, SenseiTatamiView.class);
        agregarTab(tabs, getTexto("menu.historial", "Bitácora (Fase R)"), VaadinIcon.ARCHIVE, SenseiHistorialView.class);
        // >>> FIN DEL PARCHE <<<

        //agregarTab(tabs, getTexto("menu.resultados", "Resultados"), VaadinIcon.TROPHY, SenseiResultadosView.class);
        //agregarTab(tabs, getTexto("campeonatos.titulo", "Campeonatos"), VaadinIcon.TROPHY, SenseiCampeonatosView.class);
        //agregarTab(tabs, getTexto("campos.titulo", "Campos"), VaadinIcon.MEDAL, SenseiCamposView.class);

        // --- 4. MÓDULO GPS ---
        agregarTab(tabs, getTexto("agenda.titulo", "Agenda GPS"), VaadinIcon.MAP_MARKER, SenseiAgendaView.class);

        // --- 5. MÓDULO FINANCIERO (Solo si es CLUB) ---
        if (configuracionService.esClub()) {
            agregarTab(tabs, getTexto("finanzas.titulo", "Tesorería"), VaadinIcon.MONEY_EXCHANGE, TesoreriaView.class);
        }

        // --- 6. INVENTARIO ---
        //agregarTab(tabs, getTexto("inventario.titulo", "Tienda"), VaadinIcon.CART, InventarioView.class);

        // --- 7. ADMINISTRACIÓN ---
        agregarTab(tabs, getTexto("menu.mi.perfil", "Perfil"), VaadinIcon.COGS, PerfilSenseiView.class);

        agregarTab(tabs, getTexto("menu.senseis", "Senseis"), VaadinIcon.COGS, GestionSenseisView.class);

        agregarTab(tabs, getTexto("menu.backup", "Back up"), VaadinIcon.COGS, BackupView.class);
        agregarTab(tabs, getTexto("admin.titulo", "Configuración"), VaadinIcon.COGS, AdministracionView.class);

        // --- SALIR ---
        Tab logoutTab = new Tab(createLogoutLink());
        tabs.add(logoutTab);

        addToDrawer(new VerticalLayout(tabs));
    }

    private void agregarTab(Tabs tabs, String label, VaadinIcon icon, Class<? extends Component> view) {
        if (accessChecker.hasAccess(view)) {
            RouterLink link = new RouterLink(view);
            link.add(new Icon(icon), new Span(label));
            tabs.add(new Tab(link));
        }
    }

    private RouterLink createLogoutLink() {
        RouterLink link = new RouterLink();
        link.add(new Icon(VaadinIcon.SIGN_OUT), new Span(getTexto("btn.cerrar.sesion", "Salir")));
        link.getElement().addEventListener("click", e -> {
            securityService.logout();
        });
        return link;
    }

    /**
     * Helper para obtener texto de forma segura.
     * Si traduccionService aún no está listo (no debería pasar en @PostConstruct), usa el defecto.
     */
    private String getTexto(String clave, String defecto) {
        if (traduccionService != null) {
            return traduccionService.get(clave);
        }
        return defecto;
    }
}