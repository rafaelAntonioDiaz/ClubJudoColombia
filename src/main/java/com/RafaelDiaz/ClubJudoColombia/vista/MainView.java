package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.vista.SenseiDashboardView; // Asegúrate de importar tus vistas
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

@Route("")
@PermitAll
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    public MainView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        // Contenido por defecto mientras redirige
        add(new H1("Cargando..."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            // --- DIAGNÓSTICO: MIRA LA CONSOLA DE TU IDE/SERVIDOR ---
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
        add(new H1("Bienvenido, " + nombre));
        add(new Paragraph("No tienes un rol de 'Sensei' o 'Judoka' asignado para redirigir."));
        add(new Paragraph("Revisa la consola del servidor para ver tus roles exactos."));
        add(new Anchor("logout", "Cerrar Sesión"));
    }
}