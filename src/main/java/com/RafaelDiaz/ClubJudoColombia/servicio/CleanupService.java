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
    private final FileStorageService fileStorageService;
    private final TraduccionService traduccionService; // <--- INYECCIÓN

    @Autowired
    public CleanupService(JudokaRepository judokaRepository,
                          UsuarioRepository usuarioRepository,
                          FileStorageService fileStorageService,
                          TraduccionService traduccionService) {
        this.judokaRepository = judokaRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
        this.traduccionService = traduccionService;
    }

    /**
     * Tarea Programada: Se ejecuta todos los días a las 02:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void eliminarAspirantesCaducados() {
        logger.info(">>> INICIANDO PROTOCOLO DE LIMPIEZA AUTOMÁTICA (2:00 AM) <<<");

        LocalDateTime fechaCorte = LocalDateTime.now().minusDays(15);

        List<Judoka> caducados = judokaRepository.findByEstadoAndFechaPreRegistroBefore(
                EstadoJudoka.PENDIENTE,
                fechaCorte
        );

        if (caducados.isEmpty()) {
            logger.info("--- No se encontraron aspirantes caducados hoy. ---");
            return;
        }

        logger.warn("!!! Se encontraron {} aspirantes caducados. Procediendo a eliminar...", caducados.size());

        for (Judoka zombi : caducados) {
            try {
                logger.info("Eliminando aspirante: {} (Registrado: {})",
                        zombi.getUsuario().getUsername(), zombi.getFechaPreRegistro());

                if (zombi.getDocumentos() != null) {
                    zombi.getDocumentos().forEach(doc -> {
                        try {
                            fileStorageService.delete(doc.getUrlArchivo());
                        } catch (Exception e) {
                            logger.warn("No se pudo borrar archivo físico: " + doc.getUrlArchivo());
                        }
                    });
                }

                judokaRepository.delete(zombi);
                usuarioRepository.delete(zombi.getUsuario());

            } catch (Exception e) {
                logger.error("Error al eliminar aspirante ID " + zombi.getId(), e);
            }
        }

        logger.info(">>> LIMPIEZA FINALIZADA. Base de datos optimizada. <<<");
    }
}