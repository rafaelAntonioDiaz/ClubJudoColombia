package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Componente reutilizable para filtros de búsqueda de Judokas.
 * Incluye campo de texto (nombre) y combos de Sexo y Grado.
 *
 * <p><b>Características:</b>
 * <ul>
 *   <li>Debounce en búsqueda de texto (500ms)</li>
 *   <li>Eventos automáticos al cambiar valores</li>
 *   <li>Compacto: Ocupa todo el ancho disponible</li>
 * </ul>
 *
 * <p><b>Uso:</b>
 * <pre>
 * FiltroJudokaLayout filtros = new FiltroJudokaLayout(searchParams -> {
 *     String nombre = searchParams.nombre();
 *     Sexo sexo = searchParams.sexo();
 *     // ... actualizar tu grid
 * });
 * </pre>
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-19
 */
public class FiltroJudokaLayout extends HorizontalLayout {

    private static final Logger logger = LoggerFactory.getLogger(FiltroJudokaLayout.class);

    private final TextField searchNombre = new TextField();
    private final ComboBox<Sexo> filterSexo = new ComboBox<>();
    private final ComboBox<GradoCinturon> filterGrado = new ComboBox<>();

    /**
     * Constructor que crea el componente con callbacks.
     *
     * @param onSearchChange Callback que se ejecuta al cambiar cualquier filtro
     */
    public FiltroJudokaLayout(SerializableConsumer<SearchParams> onSearchChange) {
        configureComponents();
        configureLayout((SerializableConsumer<SearchParams>) onSearchChange);
    }

    /**
     * Configura cada componente individualmente.
     */
    private void configureComponents() {
        // Campo de búsqueda por nombre
        searchNombre.setPlaceholder("Buscar por nombre...");
        searchNombre.setClearButtonVisible(true);
        searchNombre.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchNombre.setValueChangeMode(ValueChangeMode.LAZY); // Debounce de 500ms
        searchNombre.setWidth("300px");
        searchNombre.addClassName("filtro-judoka-nombre");

        // Combo de Sexo
        filterSexo.setPlaceholder("Sexo");
        filterSexo.setItems(Sexo.values());
        filterSexo.setClearButtonVisible(true);
        filterSexo.setWidth("150px");
        filterSexo.addClassName("filtro-judoka-sexo");

        // Combo de Grado
        filterGrado.setPlaceholder("Grado");
        filterGrado.setItems(GradoCinturon.values());
        filterGrado.setClearButtonVisible(true);
        filterGrado.setWidth("180px");
        filterGrado.addClassName("filtro-judoka-grado");
    }

    /**
     * Configura el layout principal.
     */
    private void configureLayout(SerializableConsumer<SearchParams> onSearchChange) {
        setWidthFull();
        setAlignItems(Alignment.END);
        setSpacing(true);
        setPadding(true);
        addClassName("filtro-judoka-layout");

        // Agregar componentes
        add(searchNombre, filterSexo, filterGrado);

        // ✅ SOLUCIÓN CORRECTA: Listener serializable para cada tipo de campo
        // TextField usa ComponentValueChangeEvent<TextField, String>
        searchNombre.addValueChangeListener(
                (com.vaadin.flow.component.HasValue.ValueChangeListener<
                        com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<TextField, String>>)
                        event -> onSearchChange.accept(getSearchParams())
        );

        // ComboBox usa ComponentValueChangeEvent<ComboBox<Sexo>, Sexo>
        filterSexo.addValueChangeListener(
                (com.vaadin.flow.component.HasValue.ValueChangeListener<
                        com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<ComboBox<Sexo>, Sexo>>)
                        event -> onSearchChange.accept(getSearchParams())
        );

        // ComboBox<GradoCinturon> usa su propio tipo
        filterGrado.addValueChangeListener(
                (com.vaadin.flow.component.HasValue.ValueChangeListener<
                        com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<ComboBox<GradoCinturon>, GradoCinturon>>)
                        event -> onSearchChange.accept(getSearchParams())
        );
    }

    /* Obtiene los valores actuales de los filtros.
     * @return Objeto inmutable con los parámetros de búsqueda
     */
    public SearchParams getSearchParams() {
        return new SearchParams(
                searchNombre.getValue(),
                filterSexo.getValue(),
                filterGrado.getValue()
        );
    }

    /**
     * Limpia todos los filtros.
     */
    public void clear() {
        searchNombre.clear();
        filterSexo.clear();
        filterGrado.clear();
        logger.debug("Filtros limpiados");
    }

    /**
     * Deshabilita/habilita todos los filtros.
     * @param enabled true para habilitar
     */
    public void setEnabled(boolean enabled) {
        searchNombre.setEnabled(enabled);
        filterSexo.setEnabled(enabled);
        filterGrado.setEnabled(enabled);
    }

    /**
     * Record inmutable que contiene los parámetros de búsqueda.
     *
     * @param nombre Texto de búsqueda (puede ser null o vacío)
     * @param sexo Sexo seleccionado (puede ser null)
     * @param grado Grado seleccionado (puede ser null)
     */
    public record SearchParams(
            String nombre,
            Sexo sexo,
            GradoCinturon grado
    ) {}
}