package com.RafaelDiaz.ClubJudoColombia.vista;

import com.vaadin.flow.component.html.Anchor; // --- 1. ASEGÚRATE DE TENER ESTE IMPORT ---
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("") // Esta es la ruta raíz (la página principal)
@PermitAll
public class MainView extends VerticalLayout {

    public MainView() {

        H1 titulo = new H1("¡Bienvenido al Club de Judo Colombia!");

        // --- 2. ESTA ES LA LÍNEA CLAVE ---
        // Creamos un ENLACE (Anchor) que apunta al endpoint de backend "/logout"
        // Spring Security interceptará esto.
        Anchor logoutLink = new Anchor("logout", "Cerrar Sesión");

        // --- 3. AÑADIMOS AMBAS COSAS ---
        add(titulo, logoutLink); // Añadimos el título Y el enlace

        // Centrado
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
    }
}