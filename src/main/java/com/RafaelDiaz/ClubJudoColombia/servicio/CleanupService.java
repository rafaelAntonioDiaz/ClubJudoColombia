package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
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

    @Autowired
    public CleanupService(JudokaRepository judokaRepository,
                          UsuarioRepository usuarioRepository,
                          AlmacenamientoCloudService almacenamientoCloudService,
                          TraduccionService traduccionService) {
        this.judokaRepository = judokaRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.traduccionService = traduccionService;
    }

    /**
     * Tarea Programada: Se ejecuta todos los días a las 02:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void eliminarAspirantesCaducados() {
        logger.info(">>> INICIANDO PROTOCOLO DE LIMPIEZA AUTOMÁTICA EN LA NUBE (2:00 AM) <<<");

        LocalDateTime fechaCorte = LocalDateTime.now().minusDays(15);

        List<Judoka> caducados = judokaRepository.findByEstadoAndFechaPreRegistroBefore(
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
                logger.info("Eliminando aspirante: {} (Registrado: {})",
                        zombi.getUsuario().getUsername(), zombi.getFechaPreRegistro());

                // --- CAMBIO AQUÍ: Borrado directamente en Cloudflare ---
                if (zombi.getDocumentos() != null) {
                    zombi.getDocumentos().forEach(doc -> {
                        almacenamientoCloudService.eliminarArchivo(doc.getUrlArchivo());
                    });
                }

                judokaRepository.delete(zombi);
                usuarioRepository.delete(zombi.getUsuario());

            } catch (Exception e) {
                logger.error("Error al eliminar aspirante ID " + zombi.getId(), e);
            }
        }

        logger.info(">>> LIMPIEZA FINALIZADA. Base de datos y Cloudflare optimizados. <<<");
    }
}