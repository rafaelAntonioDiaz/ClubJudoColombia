package com.RafaelDiaz.ClubJudoColombia.vista.admin;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/senseis", layout = SenseiLayout.class)
@PageTitle("Gestión de Senseis | Admin")
@RolesAllowed("ROLE_MASTER")
public class GestionSenseisView extends VerticalLayout {

    private final SenseiRepository senseiRepository;
    private final TraduccionService traduccionService;
    private Grid<Sensei> grid = new Grid<>(Sensei.class, false);

    public GestionSenseisView(SenseiRepository senseiRepository, TraduccionService traduccionService) {
        this.senseiRepository = senseiRepository;
        this.traduccionService = traduccionService;

        setSizeFull();
        configureGrid();
        loadData();
    }

    private void configureGrid() {
        grid.addColumn(Sensei::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(s -> s.getUsuario().getNombre() + " " + s.getUsuario().getApellido()).setHeader("Nombre");
        grid.addColumn(s -> s.getUsuario().getUsername()).setHeader("Email");
        grid.addColumn(new ComponentRenderer<>(sensei -> {
            Checkbox check = new Checkbox(sensei.isEsClubPropio());
            check.addValueChangeListener(e -> {
                sensei.setEsClubPropio(e.getValue());
                senseiRepository.save(sensei);
            });
            return check;
        })).setHeader("Club Propio").setWidth("120px");
        grid.addColumn(s -> s.getGrado()).setHeader("Grado");
        grid.setAllRowsVisible(true);
        add(grid);
    }

    private void loadData() {
        grid.setItems(senseiRepository.findAllWithUsuario());
    }
}