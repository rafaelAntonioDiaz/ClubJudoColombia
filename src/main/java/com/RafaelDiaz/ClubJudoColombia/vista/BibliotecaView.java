package com.RafaelDiaz.ClubJudoColombia.vista;

// --- Imports de Modelo y Servicio (Refactorizados) ---
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TareaDiariaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TareaDiariaService;
// --- Imports de UI ---
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * --- VISTA REFACTORIZADA ---
 * Vista para que el Sensei gestione la biblioteca de Tareas Diarias
 * (ejercicios de acondicionamiento).
 */
@Route("biblioteca-tareas") // Ruta actualizada
@RolesAllowed("ROLE_SENSEI")
public class BibliotecaView extends VerticalLayout {

    // --- Servicios y Repositorios (Actualizados) ---
    private final TareaDiariaService tareaDiariaService;
    private final TareaDiariaRepository tareaDiariaRepository;
    private final SecurityService securityService;

    // --- Componentes de UI (Actualizados) ---
    private Grid<TareaDiaria> grid = new Grid<>(TareaDiaria.class);
    private TareaDiariaForm form; // Formulario actualizado
    private Button btnNuevaTarea;

    // --- Estado ---
    private Sensei senseiActual;

    public BibliotecaView(TareaDiariaService tareaDiariaService,
                          TareaDiariaRepository tareaDiariaRepository,
                          SecurityService securityService
    ) {
        this.tareaDiariaService = tareaDiariaService;
        this.tareaDiariaRepository = tareaDiariaRepository;
        this.securityService = securityService;

        setSizeFull();
        add(new H1("Biblioteca de Tareas Diarias"));

        // Obtenemos el Sensei que está logueado
        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Error: No se pudo encontrar el Sensei logueado."));

        // 1. Crear el formulario (Refactorizado)
        form = new TareaDiariaForm(); //
        form.setVisible(false);

        // 2. Configurar Listeners del Formulario
        form.addSaveListener(this::guardarTarea); //
        form.addCancelListener(e -> cerrarFormulario()); //

        // 3. Configurar el Grid
        configurarGrid();

        // 4. Configurar el botón "Nueva Tarea"
        btnNuevaTarea = new Button("Añadir Nueva Tarea", event -> abrirFormulario());

        // 5. Layout
        HorizontalLayout contenido = new HorizontalLayout(grid, form);
        contenido.setSizeFull();

        add(btnNuevaTarea, contenido);
        cargarTareas();
    }

    private void configurarGrid() {
        grid.setWidth("70%");
        grid.removeAllColumns();

        // (Usamos los getters directos de TareaDiaria)
        grid.addColumn(TareaDiaria::getNombre) //
                .setHeader("Nombre Tarea")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(TareaDiaria::getMetaTexto) //
                .setHeader("Meta (ej. 4x15)")
                .setAutoWidth(true);

        grid.addColumn(TareaDiaria::getDescripcion) //
                .setHeader("Descripción")
                .setAutoWidth(true);

        grid.addComponentColumn(tarea -> { //
            if (tarea.getVideoUrl() != null && !tarea.getVideoUrl().isBlank()) {
                return new Icon(VaadinIcon.MOVIE);
            } else {
                return new Span("");
            }
        }).setHeader("Video").setFlexGrow(0).setAutoWidth(true);
    }

    private void cargarTareas() {
        grid.setItems(tareaDiariaRepository.findAll());
    }

    private void abrirFormulario() {
        form.setTarea(new TareaDiaria()); //
        form.setVisible(true);
        btnNuevaTarea.setEnabled(false);
    }

    private void cerrarFormulario() {
        form.setVisible(false);
        btnNuevaTarea.setEnabled(true);
    }

    /**
     * --- ¡LÓGICA DE GUARDADO (Actualizada)! ---
     * Llama al TareaDiariaService para crear la entidad.
     */
    private void guardarTarea(TareaDiariaForm.SaveEvent event) {
        try {
            // Usamos el servicio y le pasamos el Sensei actual
            tareaDiariaService.guardarTarea(
                    event.getTarea(), //
                    senseiActual
            ); //

            Notification.show("Tarea guardada con éxito.", 3000, Notification.Position.MIDDLE);
            cargarTareas(); // Refrescar el grid
            cerrarFormulario();

        } catch (Exception e) {
            Notification.show("Error al guardar la tarea: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}