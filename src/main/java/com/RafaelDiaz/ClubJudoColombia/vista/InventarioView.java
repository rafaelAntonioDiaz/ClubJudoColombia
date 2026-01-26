package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.ArticuloInventario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.servicio.InventarioService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "inventario", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Tienda del Dojo | Club Judo Colombia")
public class InventarioView extends VerticalLayout {

    private final InventarioService inventarioService;
    private final TraduccionService traduccionService; // <--- I18n
    private Grid<ArticuloInventario> grid;

    @Autowired
    public InventarioView(InventarioService inventarioService, TraduccionService traduccionService) {
        this.inventarioService = inventarioService;
        this.traduccionService = traduccionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.setWidthFull();

        H2 title = new H2(traduccionService.get("inventario.titulo"));

        Button btnNuevo = new Button(traduccionService.get("inventario.btn.nuevo"), new Icon(VaadinIcon.PLUS));
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirDialogoProducto(new ArticuloInventario()));

        header.add(title, btnNuevo);
        header.setFlexGrow(1, title);

        crearGrid();
        actualizarGrid();

        add(header, grid);
    }

    private void crearGrid() {
        grid = new Grid<>(ArticuloInventario.class, false);

        grid.addColumn(ArticuloInventario::getNombre)
                .setHeader(traduccionService.get("inventario.grid.articulo")).setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(articulo -> {
            Span badge = new Span(String.valueOf(articulo.getCantidadStock()));
            badge.getElement().getThemeList().add("badge");

            if (articulo.getCantidadStock() == 0) {
                badge.getElement().getThemeList().add("error");
                badge.setText(traduccionService.get("inventario.status.agotado"));
            } else if (articulo.esStockCritico()) {
                badge.getElement().getThemeList().add("error");
            } else {
                badge.getElement().getThemeList().add("success");
            }
            return badge;
        })).setHeader(traduccionService.get("inventario.grid.stock")).setAutoWidth(true);

        grid.addColumn(a -> "$ " + a.getPrecioVenta()).setHeader(traduccionService.get("inventario.grid.venta"));
        grid.addColumn(a -> "$ " + a.getPrecioCosto()).setHeader(traduccionService.get("inventario.grid.costo"));

        grid.addColumn(new ComponentRenderer<>(articulo -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button btnVender = new Button(traduccionService.get("btn.vender"), new Icon(VaadinIcon.CART));
            btnVender.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            btnVender.addClickListener(e -> abrirDialogoVenta(articulo));

            Button btnStock = new Button(new Icon(VaadinIcon.PLUS_CIRCLE));
            btnStock.setTooltipText(traduccionService.get("inventario.tooltip.add_stock"));
            btnStock.addThemeVariants(ButtonVariant.LUMO_SMALL);
            btnStock.addClickListener(e -> abrirDialogoStock(articulo));

            actions.add(btnVender, btnStock);
            return actions;
        })).setHeader(traduccionService.get("generic.acciones"));

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void actualizarGrid() {
        grid.setItems(inventarioService.obtenerTodo());
    }

    private void abrirDialogoVenta(ArticuloInventario articulo) {
        Dialog d = new Dialog();
        d.setHeaderTitle(traduccionService.get("inventario.dialog.venta") + ": " + articulo.getNombre());

        IntegerField cantidad = new IntegerField(traduccionService.get("generic.cantidad"));
        cantidad.setValue(1);
        cantidad.setMin(1);
        cantidad.setMax(articulo.getCantidadStock());
        cantidad.setStepButtonsVisible(true);

        Select<MetodoPago> metodoPago = new Select<>();
        metodoPago.setLabel(traduccionService.get("finanzas.label.metodo_pago"));
        metodoPago.setItems(MetodoPago.values());
        metodoPago.setItemLabelGenerator(traduccionService::get);
        metodoPago.setValue(MetodoPago.EFECTIVO);

        Button confirmar = new Button(traduccionService.get("btn.confirmar"), e -> {
            try {
                inventarioService.registrarVenta(articulo, cantidad.getValue(), metodoPago.getValue());
                Notification.show(traduccionService.get("inventario.msg.venta_ok")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                d.close();
                actualizarGrid();
            } catch (Exception ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        d.add(new VerticalLayout(cantidad, metodoPago, confirmar));
        d.open();
    }

    private void abrirDialogoStock(ArticuloInventario articulo) {
        Dialog d = new Dialog();
        d.setHeaderTitle(traduccionService.get("inventario.dialog.stock") + ": " + articulo.getNombre());

        IntegerField cantidad = new IntegerField(traduccionService.get("inventario.field.cantidad_ingreso"));
        cantidad.setValue(5);
        cantidad.setStepButtonsVisible(true);
        Button guardar = new Button(traduccionService.get("btn.actualizar"), e -> {
            inventarioService.agregarStock(articulo, cantidad.getValue());
            Notification.show(traduccionService.get("msg.success.updated"));
            d.close();
            actualizarGrid();
        });

        d.add(new VerticalLayout(cantidad, guardar));
        d.open();
    }

    private void abrirDialogoProducto(ArticuloInventario articulo) {
        Dialog d = new Dialog();
        d.setHeaderTitle(articulo.getId() == null ? traduccionService.get("inventario.dialog.nuevo") : traduccionService.get("inventario.dialog.editar"));

        TextField nombre = new TextField(traduccionService.get("generic.nombre"));
        BigDecimalField costo = new BigDecimalField(traduccionService.get("inventario.field.costo"));
        BigDecimalField venta = new BigDecimalField(traduccionService.get("inventario.field.precio"));
        IntegerField stock = new IntegerField(traduccionService.get("inventario.field.stock_inicial"));

        if (articulo.getId() != null) {
            nombre.setValue(articulo.getNombre());
            costo.setValue(articulo.getPrecioCosto());
            venta.setValue(articulo.getPrecioVenta());
            stock.setValue(articulo.getCantidadStock());
        }

        Button guardar = new Button(traduccionService.get("btn.guardar"), e -> {
            if (nombre.isEmpty() || venta.getValue() == null) return;

            articulo.setNombre(nombre.getValue());
            articulo.setPrecioCosto(costo.getValue());
            articulo.setPrecioVenta(venta.getValue());
            articulo.setCantidadStock(stock.getValue() != null ? stock.getValue() : 0);

            inventarioService.guardar(articulo);
            actualizarGrid();
            d.close();
        });

        d.add(new VerticalLayout(nombre, costo, venta, stock, guardar));
        d.open();
    }
}