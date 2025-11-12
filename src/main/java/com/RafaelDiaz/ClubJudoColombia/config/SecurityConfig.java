package com.RafaelDiaz.ClubJudoColombia.config;

// Importamos nuestra vista de Login
import com.RafaelDiaz.ClubJudoColombia.vista.LoginView;
// El import clave de Vaadin
import com.vaadin.flow.spring.security.VaadinWebSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de Seguridad de Spring (Versión final).
 *
 * @Configuration: Marca esta clase para configuración.
 * @EnableWebSecurity: Habilita la seguridad web de Spring.
 *
 * --- CAMBIO CLAVE ---
 * extends VaadinWebSecurity: Heredamos de la clase de Vaadin.
 * Esto configura automáticamente la seguridad para:
 * 1. Permitir todas las peticiones internas de Vaadin (CSS, JS, etc.).
 * 2. Habilitar la protección CSRF de Vaadin.
 * 3. Integrarse con el ciclo de vida de las vistas de Vaadin.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    /**
     * Define el "Bean" del codificador de contraseñas (igual que antes).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Este método configura las reglas de seguridad HTTP.
     * Al heredar de VaadinWebSecurity, ya no usamos un SecurityFilterChain Bean,
     * sino que sobrescribimos este método.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 1. (Opcional) Permitir acceso a recursos públicos.
        // Si tuvieras una carpeta /images/public, la pondrías aquí.
        // http.authorizeHttpRequests(auth ->
        //     auth.requestMatchers("/images/public/**").permitAll());

        // 2. Llama a la configuración base de Vaadin.
        // ESTO ES OBLIGATORIO.
        super.configure(http);

        // 3. Configura nuestra vista de login personalizada.
        // Esta línea mágica le dice a Spring Security:
        // - Que nuestra página de login está en la ruta de LoginView.class (o sea, "/login")
        // - Que intercepte las peticiones POST a "/login"
        // - Que redirija a "/login?error" si el login falla.
        // - Que configure automáticamente un logout en "/logout".
        setLoginView(http, LoginView.class);
    }
}