package com.RafaelDiaz.ClubJudoColombia.controlador;

import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class MagicLinkController {

    private final AdmisionesService admisionesService;
    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public MagicLinkController(AdmisionesService admisionesService, UserDetailsService userDetailsService) {
        this.admisionesService = admisionesService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/invitacion/{token}")
    public void procesarMagicLink(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 1. Validar token en BD y activar la cuenta (o marcar token como usado)
            Usuario usuario = admisionesService.validarYActivarInvitacion(token);

            // 2. ¡MAGIA! Usamos tu UserDetailsServiceImpl para cargar el perfil oficial
            UserDetails springUser = userDetailsService.loadUserByUsername(usuario.getUsername());

            // 3. Crear el token de autenticación nativo de Spring
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    springUser, null, springUser.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // 4. Inyectar la sesión forzada en el navegador
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            securityContextRepository.saveContext(context, request, response);

            // 5. Redirección HTTP nativa hacia el Semáforo (MainRouterView)
            response.sendRedirect("/");

        } catch (Exception e) {
            System.err.println("❌ ERROR EN CONTROLADOR MAGIC LINK: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("/login?error");
        }
    }
}