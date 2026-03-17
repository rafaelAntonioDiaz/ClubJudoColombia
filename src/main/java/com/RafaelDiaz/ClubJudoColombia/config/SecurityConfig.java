package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.vista.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configuraciones especiales para H2 console (solo si está habilitada)
        if (h2ConsoleEnabled) {
            http.csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()));
            http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        }

        // Reglas de autorización
        http.authorizeHttpRequests(auth -> {
            if (h2ConsoleEnabled) {
                auth.requestMatchers(PathRequest.toH2Console()).permitAll();
            }
            auth.requestMatchers(
                    "/registro/**",
                    "/images/**",
                    "/icons/**",
                    "/acceso-dojo/**",
                    "/invitacion/**",
                    "/manifest.webmanifest",
                    "/sw.js",
                    "/offline.html"
            ).permitAll();
        });

        // Configuración base de Vaadin
        super.configure(http);

        // Vista de login
        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        if (h2ConsoleEnabled) {
            web.ignoring().requestMatchers(PathRequest.toH2Console());
        }
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        String hierarchyString = "ROLE_MASTER > ROLE_ADMIN \n" +
                "ROLE_ADMIN > ROLE_SENSEI \n" +
                "ROLE_SENSEI > ROLE_JUDOKA \n" +
                "ROLE_SENSEI > ROLE_ACUDIENTE \n" +
                "ROLE_SENSEI > ROLE_MECENAS";
        hierarchy.setHierarchy(hierarchyString);
        return hierarchy;
    }
}