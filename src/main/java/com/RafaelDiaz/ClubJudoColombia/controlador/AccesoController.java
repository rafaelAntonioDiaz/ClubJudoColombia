package com.RafaelDiaz.ClubJudoColombia.controlador;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AccesoDojoService;
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
public class AccesoController {

    private final AdmisionesService admisionesService;
    private final AccesoDojoService accesoDojoService;
    private final UserDetailsService userDetailsService;
    private final SenseiRepository senseiRepository;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AccesoController(AdmisionesService admisionesService,
                            AccesoDojoService accesoDojoService,
                            UserDetailsService userDetailsService, SenseiRepository senseiRepository) {
        this.admisionesService = admisionesService;
        this.accesoDojoService = accesoDojoService;
        this.userDetailsService = userDetailsService;
        this.senseiRepository = senseiRepository;
    }

    @GetMapping("/acceso/{token}")
    public void procesarAcceso(@PathVariable String token,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        try {
            // 1. Intentar como invitación de nuevo usuario
            try {
                admisionesService.validarTokenInvitacion(token);
                // Si llegamos aquí, es una invitación válida -> redirigir a registro
                response.sendRedirect("/registro/" + token);
                return;
            } catch (Exception e) {
                // No es invitación, continuar
            }

            // 2. Intentar como token de acceso de judoka (pase)
            Judoka judoka = accesoDojoService.validarPase(token).orElse(null);
            if (judoka != null) {
                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username("judoka_" + judoka.getId())
                        .password("")
                        .authorities("ROLE_JUDOKA")
                        .build();
                autenticarYRedirigir(userDetails, request, response, "/dashboard-judoka");
                return;
            }

            // 3. Token no válido
            response.sendRedirect("/login?error=token-invalido");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/login?error=error-interno");
        }
    }

    private void autenticarYRedirigir(UserDetails userDetails,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      String redirectUrl) throws IOException {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        securityContextRepository.saveContext(context, request, response);

        response.sendRedirect(redirectUrl);
    }
}