package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.servicio.RegistroService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("registro")
@PageTitle("Registro | Club Judo Colombia")
@AnonymousAllowed
public class RegistroJudokaView extends VerticalLayout {

    private final RegistroService registroService;
    private final TraduccionService traduccionService;

    // Campos
    private TextField nombre = new TextField("Nombre");
    private TextField apellido = new TextField("Apellido");
    private EmailField email = new EmailField("Email (Usuario)");
    private PasswordField password = new PasswordField("Contraseña");
    private PasswordField confirmPassword = new PasswordField("Confirmar Contraseña");
    private TextField celular = new TextField("Celular");

    private DatePicker nacimiento = new DatePicker("Fecha Nacimiento");
    private NumberField peso = new NumberField("Peso (kg)");
    private NumberField estatura = new NumberField("Estatura (cm)");
    private ComboBox<Sexo> sexo = new ComboBox<>("Sexo");

    // Validacion
    private TextField codigoField = new TextField("Código de Verificación");
    private VerticalLayout paso1Layout;
    private VerticalLayout paso2Layout;

    public RegistroJudokaView(RegistroService registroService, TraduccionService traduccionService) {
        this.registroService = registroService;
        this.traduccionService = traduccionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        // Fondo suave
        getStyle().set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)");

        construirPaso1();
        construirPaso2();
    }

    private void construirPaso1() {
        paso1Layout = new VerticalLayout();
        paso1Layout.setMaxWidth("800px");
        paso1Layout.addClassName("card-blanca"); // Asegúrate de tener este estilo CSS o usa setStyle
        paso1Layout.getStyle().set("background", "white").set("padding", "30px").set("border-radius", "10px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        paso1Layout.setSpacing(true);

        H2 titulo = new H2(traduccionService.get("registro.titulo", "Registro de Aspirante"));
        Span subtitulo = new Span(traduccionService.get("registro.subtitulo", "Únete a nuestro Dojo"));
        subtitulo.getStyle().set("color", "gray");

        sexo.setItems(Sexo.values());

        FormLayout form = new FormLayout();
        form.add(nombre, apellido, email, celular, nacimiento, sexo, peso, estatura, password, confirmPassword);
        // Hacemos que algunos campos ocupen 2 columnas si hay espacio
        form.setColspan(email, 2);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        Button btnSiguiente = new Button(traduccionService.get("registro.btn.siguiente", "Siguiente"));
        btnSiguiente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSiguiente.setWidthFull();
        btnSiguiente.addClickListener(e -> enviarCodigo());

        Button btnLogin = new Button(traduccionService.get("registro.btn.volver", "Ya tengo cuenta"),
                e -> getUI().ifPresent(ui -> ui.navigate("login")));
        btnLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        paso1Layout.add(titulo, subtitulo, form, btnSiguiente, btnLogin);
        add(paso1Layout);
    }

    private void construirPaso2() {
        paso2Layout = new VerticalLayout();
        paso2Layout.setMaxWidth("400px");
        paso2Layout.getStyle().set("background", "white").set("padding", "30px").set("border-radius", "10px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        paso2Layout.setAlignItems(Alignment.CENTER);
        paso2Layout.setVisible(false);

        H2 titulo = new H2("Verificar Email");
        Span msg = new Span("Hemos enviado un código a tu correo.");

        codigoField.setPlaceholder("Ej: 123456");
        codigoField.setWidthFull();

        Button btnVerificar = new Button("Verificar y Finalizar");
        btnVerificar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnVerificar.setWidthFull();
        btnVerificar.addClickListener(e -> finalizarRegistro());

        Button btnAtras = new Button("Volver", e -> {
            paso2Layout.setVisible(false);
            paso1Layout.setVisible(true);
        });
        btnAtras.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        paso2Layout.add(new Icon(VaadinIcon.ENVELOPE_OPEN), titulo, msg, codigoField, btnVerificar, btnAtras);
        add(paso2Layout);
    }

    private void enviarCodigo() {
        if (email.isEmpty() || password.isEmpty() || nombre.isEmpty()) {
            Notification.show(traduccionService.get("error.campos_obligatorios"), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (!password.getValue().equals(confirmPassword.getValue())) {
            Notification.show("Las contraseñas no coinciden", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            registroService.iniciarRegistro(email.getValue(), nombre.getValue());
            paso1Layout.setVisible(false);
            paso2Layout.setVisible(true);
            Notification.show("Código enviado a " + email.getValue());
        } catch (Exception e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void finalizarRegistro() {
        if (registroService.validarCodigo(email.getValue(), codigoField.getValue())) {
            try {
                Usuario u = new Usuario();
                u.setUsername(email.getValue());
                u.setEmail(email.getValue());
                u.setNombre(nombre.getValue());
                u.setApellido(apellido.getValue());
                u.setPasswordHash(password.getValue());

                Judoka j = new Judoka();
                j.setCelular(celular.getValue());
                j.setFechaNacimiento(nacimiento.getValue());
                j.setPeso(peso.getValue());
                j.setEstatura(estatura.getValue());
                j.setSexo(sexo.getValue());

                registroService.finalizarRegistro(u, j);

                Notification.show(traduccionService.get("registro.exito", "Registro Exitoso. Inicia Sesión."), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                getUI().ifPresent(ui -> ui.navigate("login"));

            } catch (Exception e) {
                Notification.show("Error al guardar: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Código incorrecto", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}