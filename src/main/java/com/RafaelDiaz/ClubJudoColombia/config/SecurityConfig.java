package com.RafaelDiaz.ClubJudoColombia.config;

// Importamos nuestra vista de Login
import com.RafaelDiaz.ClubJudoColombia.vista.LoginView;
// El import clave de Vaadin
import com.vaadin.flow.spring.security.VaadinWebSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest; // <--- Importante
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
    protected void configure(HttpSecurity http) throws Exception {
        // --- 1. CONFIGURACIÓN MODERNA PARA H2 CONSOLE ---
        // Deshabilitamos CSRF y permitimos iFrames usando la ruta nativa de H2
        http.csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // --- 2. RUTAS PÚBLICAS (Sin AntPathRequestMatcher) ---
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(PathRequest.toH2Console()).permitAll() // Permiso nativo H2
                        .requestMatchers(
                                "/registro/**",  // Permite /registro y /registro/{token}
                                "/images/**",
                                "/icons/**",
                                "/manifest.webmanifest",
                                "/sw.js",
                                "/offline.html"
                        ).permitAll()
        );

        // Llamamos a la configuración base de Vaadin
        super.configure(http);

        // Configuramos la vista de Login
        setLoginView(http, LoginView.class);
    }

    // --- 3. EXCEPCIÓN PARA EL ENRUTADOR DE VAADIN ---
    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        // Le decimos a Vaadin que ignore la consola H2 usando la utilidad de Spring
        web.ignoring().requestMatchers(PathRequest.toH2Console());
    }
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        // El MASTER puede hacer todo lo del SENSEI, el SENSEI todo lo del JUDOKA
        hierarchy.setHierarchy("ROLE_MASTER > ROLE_SENSEI \n ROLE_SENSEI > ROLE_JUDOKA");
        return hierarchy;
    }

}