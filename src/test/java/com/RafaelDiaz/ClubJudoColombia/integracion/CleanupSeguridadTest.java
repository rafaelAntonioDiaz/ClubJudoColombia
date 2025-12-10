package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.CleanupService;
import com.RafaelDiaz.ClubJudoColombia.servicio.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
// --- NUEVO IMPORT (Spring Boot 3.4+) ---
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Usa H2
@Transactional
class CleanupSeguridadTest {

    @Autowired private CleanupService cleanupService;
    @Autowired private JudokaRepository judokaRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    // --- CORRECCIÓN: Usar @MockitoBean en lugar de @MockBean ---
    @MockitoBean private FileStorageService fileStorageService;

    @Test
    @DisplayName("INTEGRACIÓN: Solo debe borrar aspirantes PENDIENTES antiguos")
    void testLimpiezaSelectiva() {
        // --- ESCENARIO ---

        // 1. ZOMBI: Pendiente hace 20 días (DEBE MORIR)
        Usuario uZombi = usuarioRepo.save(new Usuario("zombi", "123", "Z", "Z"));
        Judoka zombi = new Judoka();
        zombi.setUsuario(uZombi);
        zombi.setEstado(EstadoJudoka.PENDIENTE);
        zombi.setFechaPreRegistro(LocalDateTime.now().minusDays(20)); // <--- Viejo
        judokaRepo.save(zombi);

        // 2. NOVATO: Pendiente hace 2 días (DEBE VIVIR)
        Usuario uNovato = usuarioRepo.save(new Usuario("novato", "123", "N", "N"));
        Judoka novato = new Judoka();
        novato.setUsuario(uNovato);
        novato.setEstado(EstadoJudoka.PENDIENTE);
        novato.setFechaPreRegistro(LocalDateTime.now().minusDays(2)); // <--- Nuevo
        judokaRepo.save(novato);

        // 3. VETERANO: Activo hace 3 años (DEBE VIVIR - INTOCABLE)
        Usuario uVeterano = usuarioRepo.save(new Usuario("veterano", "123", "V", "V"));
        Judoka veterano = new Judoka();
        veterano.setUsuario(uVeterano);
        veterano.setEstado(EstadoJudoka.ACTIVO); // <--- ACTIVO
        veterano.setFechaPreRegistro(LocalDateTime.now().minusYears(3)); // <--- Muy viejo
        judokaRepo.save(veterano);

        // --- ACTUAR ---
        cleanupService.eliminarAspirantesCaducados();

        // --- VERIFICAR ---
        assertTrue(judokaRepo.findByUsuario(uZombi).isEmpty(), "El Zombi debió ser eliminado");
        assertTrue(judokaRepo.findByUsuario(uNovato).isPresent(), "El Novato debe seguir existiendo");
        assertTrue(judokaRepo.findByUsuario(uVeterano).isPresent(), "El Veterano Activo JAMÁS debe ser borrado");
    }
}