package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.CleanupService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService; // Cambio de nombre de servicio
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CleanupSeguridadTest {

    @Autowired private CleanupService cleanupService;
    @Autowired private JudokaRepository judokaRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private SenseiRepository senseiRepo;

    // Ajustado al nombre de tu servicio de almacenamiento actual
    @MockitoBean private AlmacenamientoCloudService almacenamientoCloudService;

    @Autowired private RolRepository rolRepository;

    @BeforeEach
    void setUp() {

        // Crear roles básicos si no existen
        if (rolRepository.findByNombre("ROLE_ACUDIENTE").isEmpty()) {
            rolRepository.save(new Rol("ROLE_ACUDIENTE"));
        }
        if (rolRepository.findByNombre("ROLE_JUDOKA").isEmpty()) {
            rolRepository.save(new Rol("ROLE_JUDOKA"));
        }
        if (rolRepository.findByNombre("ROLE_SENSEI").isEmpty()) {
            rolRepository.save(new Rol("ROLE_SENSEI"));
        }
        if (rolRepository.findByNombre("ROLE_MASTER").isEmpty()) {
            rolRepository.save(new Rol("ROLE_MASTER"));
        }
        if (rolRepository.findByNombre("ROLE_MECENAS").isEmpty()) {
            rolRepository.save(new Rol("ROLE_MECENAS"));
        }
        if (rolRepository.findByNombre("ROLE_JUDOKA_ADULTO").isEmpty()) {
            rolRepository.save(new Rol("ROLE_JUDOKA_ADULTO"));
        }

    }

    @Test
    @DisplayName("INTEGRACIÓN: Solo debe borrar aspirantes PENDIENTES caducados")
    void testEliminarAspirantesCaducados() {
        // 1. ZOMBI: Pendiente hace 20 días (DEBE MORIR)
        Usuario uZombi = usuarioRepo.save(new Usuario("zombi@test.com", "123", "Zom", "Bi"));
        Judoka zombi = new Judoka();
        Usuario usuarioSensei = new Usuario("sensei@test.com", "pass", "Sensei", "Test");
        usuarioSensei.setActivo(true);
        usuarioRepo.save(usuarioSensei);
        Sensei senseiTest = new Sensei();
        senseiTest.setUsuario(usuarioSensei);
        senseiTest.setNombreClub("Club Test");
        senseiTest.setAnosPractica(10);
        senseiTest.setGrado(GradoCinturon.NEGRO_1_DAN);
        senseiRepo.save(senseiTest);
        zombi.setSensei(senseiTest);
        zombi.setAcudiente(uZombi);
        zombi.setNombre("Zombi");
        zombi.setEstado(EstadoJudoka.PENDIENTE);
        zombi.setSensei(senseiTest);
        zombi.setFechaGeneracionToken(LocalDateTime.now().minusDays(20)); // caducado
        judokaRepo.save(zombi);

        // 2. NOVATO: Pendiente hace 2 días (DEBE VIVIR)
        Usuario uNovato = usuarioRepo.save(new Usuario("novato@test.com", "123", "Nov", "Ato"));
        Judoka novato = new Judoka();
        novato.setAcudiente(uNovato);
        novato.setNombre("Novato");
        novato.setEstado(EstadoJudoka.PENDIENTE);
        novato.setSensei(senseiTest);
        novato.setFechaGeneracionToken(LocalDateTime.now().minusDays(2));
        judokaRepo.save(novato);

        // 3. VETERANO: Activo hace 3 años (DEBE VIVIR)
        Usuario uVeterano = usuarioRepo.save(new Usuario("veterano@test.com", "123", "Vet", "Eran"));
        Judoka veterano = new Judoka();
        veterano.setAcudiente(uVeterano);
        veterano.setNombre("Veterano");
        veterano.setEstado(EstadoJudoka.ACTIVO);
        veterano.setSensei(senseiTest);
        judokaRepo.save(veterano);

        // --- ACTUAR ---
        cleanupService.eliminarAspirantesCaducados();

        // --- VERIFICAR ---
        assertFalse(judokaRepo.findById(zombi.getId()).isPresent(), "El judoka zombi debería haber sido eliminado");
        assertTrue(judokaRepo.findById(novato.getId()).isPresent(), "El novato debería seguir existiendo");
        assertTrue(judokaRepo.findById(veterano.getId()).isPresent(), "El veterano debería seguir existiendo");
    }
}