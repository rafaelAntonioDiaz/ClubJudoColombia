package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.AsistenciaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.JsonObject;

public class CheckInWidget extends VerticalLayout {

    private final AsistenciaService asistenciaService;
    private final Judoka judoka;
    private final TraduccionService traduccionService; // <--- I18n

    private final Button btnCheckIn;
    private final Span statusLabel;

    public CheckInWidget(AsistenciaService asistenciaService,
                         Judoka judoka,
                         TraduccionService traduccionService) { // <--- ACTUALIZADO
        this.asistenciaService = asistenciaService;
        this.judoka = judoka;
        this.traduccionService = traduccionService;

        addClassName("card-blanca");
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setMaxWidth("400px");

        H3 titulo = new H3(traduccionService.get("checkin.titulo")); // "Control de Asistencia GPS"

        statusLabel = new Span(traduccionService.get("checkin.status.ready"));
        statusLabel.getStyle().set("font-size", "0.9em").set("color", "gray");

        btnCheckIn = new Button(traduccionService.get("checkin.btn.marcar"), new Icon(VaadinIcon.MAP_MARKER));
        btnCheckIn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnCheckIn.setWidthFull();

        btnCheckIn.addClickListener(e -> iniciarProcesoGeolocalizacion());

        add(titulo, statusLabel, btnCheckIn);
    }

    private void iniciarProcesoGeolocalizacion() {
        btnCheckIn.setEnabled(false);
        btnCheckIn.setText(traduccionService.get("checkin.status.locating"));
        statusLabel.setText(traduccionService.get("checkin.status.requesting"));
        statusLabel.getStyle().set("color", "blue");

        UI.getCurrent().getPage().executeJs(
                "return new Promise((resolve, reject) => {" +
                        "  if (!navigator.geolocation) {" +
                        "    reject('Geolocalización no soportada');" +
                        "  } else {" +
                        "    navigator.geolocation.getCurrentPosition(" +
                        "      pos => resolve({lat: pos.coords.latitude, lon: pos.coords.longitude})," +
                        "      err => reject(err.message)" +
                        "    );" +
                        "  }" +
                        "});"
        ).then(json -> {
            JsonObject coords = (JsonObject) json;
            double lat = coords.getNumber("lat");
            double lon = coords.getNumber("lon");
            procesarCoordenadas(lat, lon);
        }, error -> {
            resetearBoton(traduccionService.get("checkin.btn.retry"), VaadinIcon.WARNING);
            statusLabel.setText(traduccionService.get("checkin.error.denied"));
            Notification.show(traduccionService.get("checkin.error.browser"), 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
    }

    private void procesarCoordenadas(double lat, double lon) {
        try {
            statusLabel.setText(traduccionService.get("checkin.status.validating"));

            asistenciaService.realizarCheckInGps(judoka, lat, lon);

            btnCheckIn.setText(traduccionService.get("checkin.btn.success"));
            btnCheckIn.setIcon(new Icon(VaadinIcon.CHECK));
            btnCheckIn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            btnCheckIn.setEnabled(false);
            statusLabel.setText(traduccionService.get("checkin.status.registered"));
            Notification.show(traduccionService.get("checkin.msg.oss"), 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (RuntimeException e) {
            resetearBoton(traduccionService.get("checkin.btn.retry"), VaadinIcon.EXCLAMATION_CIRCLE);
            statusLabel.setText(e.getMessage());
            statusLabel.getStyle().set("color", "var(--lumo-error-color)");
        }
    }

    private void resetearBoton(String text, VaadinIcon icon) {
        btnCheckIn.setEnabled(true);
        btnCheckIn.setText(text);
        btnCheckIn.setIcon(new Icon(icon));
        btnCheckIn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnCheckIn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }
}