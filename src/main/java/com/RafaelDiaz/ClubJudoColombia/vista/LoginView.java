package com.RafaelDiaz.ClubJudoColombia.vista;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Vista de Login de la aplicación.
 *
 * @Route("login"): Esta es la URL para la página de login (ej. http://localhost:8080/login)
 *
 * BeforeEnterObserver: Interfaz que nos permite ejecutar código
 * antes de que la vista se muestre (para mostrar errores de login).
 */
@Route("login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    // El componente de formulario de login que nos da Vaadin
    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        addClassName("login-view"); // (Opcional) Para estilizar con CSS
        setSizeFull(); // Ocupa toda la pantalla

        // Configura el layout para centrar el formulario
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- ¡MUY IMPORTANTE! ---
        // El 'action' le dice al formulario a qué URL debe enviar
        // los datos (username/password).
        // Debe ser "login" para que Spring Security lo intercepte.
        loginForm.setAction("login");

        // Añadimos un título y el formulario al layout
        add(new H1("Club de Judo Colombia"), loginForm);
    }

    /**
     * Este método se ejecuta antes de que la vista se muestre.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Leemos la URL para ver si Spring Security nos ha enviado
        // un parámetro de "error" (lo que significa que el login falló).
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {

            // Si hay un error, le decimos al formulario que lo muestre.
            loginForm.setError(true);
        }
    }
}