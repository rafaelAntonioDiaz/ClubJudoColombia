package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    private final JudokaRepository judokaRepository;
    private final UsuarioRepository usuarioRepository;
    // --- CAMBIO AQUÍ: Usamos el servicio de la Nube ---
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final TraduccionService traduccionService;
    private final TokenInvitacionRepository tokenRepository;

    @Autowired
    public CleanupService(JudokaRepository judokaRepository,
                          UsuarioRepository usuarioRepository,
                          AlmacenamientoCloudService almacenamientoCloudService,
                          TraduccionService traduccionService, TokenInvitacionRepository tokenRepository) {
        this.judokaRepository = judokaRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.traduccionService = traduccionService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Tarea Programada: Se ejecuta todos los días a las 02:00 AM.
     */
    // CleanupService.java - Método eliminarAspirantesCaducados() actualizado

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void eliminarAspirantesCaducados() {
        logger.info(">>> INICIANDO PROTOCOLO DE LIMPIEZA AUTOMÁTICA EN LA NUBE (2:00 AM) <<<");

        LocalDateTime fechaCorte = LocalDateTime.now().minusDays(15);

        List<Judoka> caducados = judokaRepository.findByEstadoAndFechaGeneracionTokenBefore(
                EstadoJudoka.PENDIENTE,
                fechaCorte
        );

        if (caducados.isEmpty()) {
            logger.info("--- No se encontraron aspirantes caducados hoy. ---");
            return;
        }

        logger.warn("!!! Se encontraron {} aspirantes caducados. Procediendo a eliminar base de datos y archivos en la nube...", caducados.size());

        for (Judoka zombi : caducados) {
            try {
                // 1. Eliminar documentos en la nube
                if (zombi.getDocumentos() != null) {
                    zombi.getDocumentos().forEach(doc -> {
                        almacenamientoCloudService.eliminarArchivo(doc.getUrlArchivo());
                    });
                }

                // 2. Eliminar token de invitación asociado

                tokenRepository.findById(zombi.getId()).ifPresent(tokenRepository::delete);

                // 3. Obtener el usuario (puede ser acudiente o el propio judoka si es adulto)
                Usuario usuario = (zombi.getUsuario() != null) ? zombi.getUsuario() : zombi.getAcudiente();

                if (usuario != null) {
                    // Limpiar la relación con roles para evitar violación de FK
                    usuario.getRoles().clear();
                    usuarioRepository.save(usuario); // Esto actualiza la tabla intermedia
                    usuarioRepository.delete(usuario);
                }

                // 4. Finalmente eliminar el judoka (sus documentos ya se borraron, y el token también)
                judokaRepository.delete(zombi);

                logger.info("Eliminado aspirante ID: {}", zombi.getId());

            } catch (Exception e) {
                logger.error("Error al eliminar aspirante ID " + zombi.getId(), e);
            }
        }

        logger.info(">>> LIMPIEZA FINALIZADA. Base de datos y Cloudflare optimizados. <<<");
    }
}