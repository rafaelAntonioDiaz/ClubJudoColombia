package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- Importado
import com.RafaelDiaz.ClubJudoColombia.vista.SenseiDashboardView;
import com.RafaelDiaz.ClubJudoColombia.vista.JudokaDashboardView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@PermitAll
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private final TraduccionService traduccionService;

    // Inyección del servicio en el constructor
    @Autowired
    public MainView(TraduccionService traduccionService) {
        this.traduccionService = traduccionService;

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // i18n: Mensaje de carga inicial
        add(new H1(traduccionService.get("main.cargando")));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            // --- DIAGNÓSTICO (Logs del servidor no se traducen) ---
            System.out.println("=== DIAGNÓSTICO LOGIN ===");
            System.out.println("Usuario: " + auth.getName());
            System.out.println("Autoridades (Roles): " + auth.getAuthorities());
            // -------------------------------------------------------

            boolean esSensei = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(rol -> rol.equals("ROLE_SENSEI"));

            boolean esJudoka = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(rol -> rol.equals("ROLE_JUDOKA"));

            // Lógica de Redirección
            if (esSensei) {
                System.out.println("-> Redirigiendo a SenseiDashboardView");
                event.rerouteTo(SenseiDashboardView.class);
            } else if (esJudoka) {
                System.out.println("-> Redirigiendo a JudokaDashboardView");
                event.rerouteTo(JudokaDashboardView.class);
            } else {
                // Si llegamos aquí, el usuario está logueado pero no coincide el rol
                System.out.println("-> NO SE ENCONTRÓ ROL COINCIDENTE. Se queda en MainView.");
                mostrarPantallaSinRol(auth.getName());
            }
        }
    }

    private void mostrarPantallaSinRol(String nombre) {
        removeAll();

        // i18n: Mensaje de bienvenida con formato (ej. "Bienvenido, Juan")
        String bienvenida = String.format(traduccionService.get("main.bienvenido"), nombre);
        add(new H1(bienvenida));

        // i18n: Mensajes de error explicativos
        add(new Paragraph(traduccionService.get("main.error.sin_rol_1")));
        add(new Paragraph(traduccionService.get("main.error.sin_rol_2")));

        // i18n: Enlace de logout
        add(new Anchor("logout", traduccionService.get("btn.cerrar.sesion")));
    }
}