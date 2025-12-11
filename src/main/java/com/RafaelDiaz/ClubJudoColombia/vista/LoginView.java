package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Vista de Login de la aplicación.
 * Actualizada con TraduccionService para internacionalizar el formulario.
 */
@Route("login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final TraduccionService traduccionService;

    // Inyectamos el servicio en el constructor
    public LoginView(TraduccionService traduccionService) {
        this.traduccionService = traduccionService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Configuramos la acción del formulario (Spring Security)
        loginForm.setAction("login");
        // NUEVO: Botón de Registro
        Button btnRegistro = new Button(traduccionService.get("login.btn.registrar", "Crear una Cuenta"));
        btnRegistro.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnRegistro.addClickListener(e -> btnRegistro.getUI().ifPresent(ui -> ui.navigate("registro")));
        // --- INTERNACIONALIZACIÓN DEL FORMULARIO ---
        configurarTextosFormulario();

        // i18n: Título de la aplicación
        add(new H1(traduccionService.get("app.nombre")), loginForm, btnRegistro);
    }

    /**
     * Configura el objeto LoginI18n con textos de la base de datos.
     */
    private void configurarTextosFormulario() {
        LoginI18n i18n = LoginI18n.createDefault();

        // 1. Textos del Formulario (Inputs y Botones)
        LoginI18n.Form form = i18n.getForm();
        form.setTitle(traduccionService.get("login.form.titulo")); // A veces se deja vacío si usas un H1 externo
        form.setUsername(traduccionService.get("login.lbl.usuario"));
        form.setPassword(traduccionService.get("login.lbl.password"));
        form.setSubmit(traduccionService.get("login.btn.ingresar"));
        form.setForgotPassword(traduccionService.get("login.link.olvido"));
        i18n.setForm(form);

        // 2. Textos de Error (Cuando fallan las credenciales)
        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle(traduccionService.get("login.error.titulo"));
        errorMessage.setMessage(traduccionService.get("login.error.mensaje"));
        i18n.setErrorMessage(errorMessage);

        // Aplicamos la configuración al componente
        loginForm.setI18n(i18n);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}