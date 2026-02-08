package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.CleanupService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService; // Cambio de nombre de servicio
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CleanupSeguridadTest {

    @Autowired private CleanupService cleanupService;
    @Autowired private JudokaRepository judokaRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    // Ajustado al nombre de tu servicio de almacenamiento actual
    @MockitoBean private AlmacenamientoCloudService almacenamientoCloudService;

    @Test
    @DisplayName("INTEGRACIÓN: Solo debe borrar aspirantes PENDIENTES caducados")
    void testEliminarAspirantesCaducados() {
        // --- ARREGLAR ---

        // 1. ZOMBI: Pendiente hace 20 días (DEBE MORIR)
        // Constructor de Usuario ajustado: username, password, nombre, apellido
        Usuario uZombi = usuarioRepo.save(new Usuario("zombi@test.com", "123", "Zom", "Bi"));
        Judoka zombi = new Judoka();
        zombi.setAcudiente(uZombi); // Cambio: de setUsuario a setAcudiente
        zombi.setNombre("Zombi");
        zombi.setEstado(EstadoJudoka.PENDIENTE);
        judokaRepo.save(zombi);

        // 2. NOVATO: Pendiente hace 2 días (DEBE VIVIR)
        Usuario uNovato = usuarioRepo.save(new Usuario("novato@test.com", "123", "Nov", "Ato"));
        Judoka novato = new Judoka();
        novato.setAcudiente(uNovato); // Cambio: de setUsuario a setAcudiente
        novato.setNombre("Novato");
        novato.setEstado(EstadoJudoka.PENDIENTE);
        judokaRepo.save(novato);

        // 3. VETERANO: Activo hace 3 años (DEBE VIVIR)
        Usuario uVeterano = usuarioRepo.save(new Usuario("veterano@test.com", "123", "Vet", "Eran"));
        Judoka veterano = new Judoka();
        veterano.setAcudiente(uVeterano); // Cambio: de setUsuario a setAcudiente
        veterano.setNombre("Veterano");
        veterano.setEstado(EstadoJudoka.ACTIVO);
        judokaRepo.save(veterano);

        // --- ACTUAR ---
        cleanupService.eliminarAspirantesCaducados();

        // --- VERIFICAR ---
        // El zombi no debe existir
        assertFalse(judokaRepo.findById(zombi.getId()).isPresent(), "El judoka zombi debería haber sido eliminado");

        // Los otros dos deben persistir
        assertTrue(judokaRepo.findById(novato.getId()).isPresent(), "El novato debería seguir existiendo");
        assertTrue(judokaRepo.findById(veterano.getId()).isPresent(), "El veterano debería seguir existiendo");
    }
}