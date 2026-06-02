package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.AsistenciaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionEjecutadaService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class AsistenciaTatamiTest {

    @Autowired private SesionEjecutadaService sesionEjecutadaService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private GrupoEntrenamientoRepository grupoRepo;
    @Autowired private JudokaRepository judokaRepo;
    @Autowired private SenseiRepository senseiRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private MicrocicloRepository microcicloRepo;
    @Autowired private AsistenciaRepository asistenciaRepo;
    @Autowired private EntityManager em;

    @MockitoBean private SecurityService securityService;

    private Sensei senseiTest;
    private GrupoEntrenamiento grupoTest;
    private Microciclo microcicloTest;
    private Judoka judoka1, judoka2;

    @BeforeEach
    void setUp() {
        // 1. Rol SENSEI
        Rol rolSensei = rolRepo.findByNombre("ROLE_SENSEI")
                .orElseGet(() -> rolRepo.save(new Rol("ROLE_SENSEI")));

        // 2. Sensei con email único
        String emailSensei = "sensei-asistencia-" + UUID.randomUUID() + "@test.com";
        Usuario usuarioSensei = new Usuario(emailSensei, "hash", "Carlos", "Perez");
        usuarioSensei.setActivo(true);
        usuarioSensei.setRoles(java.util.Set.of(rolSensei));
        usuarioRepo.save(usuarioSensei);

        senseiTest = new Sensei();
        senseiTest.setUsuario(usuarioSensei);
        senseiTest.setNombreClub("Club Test Asistencia");
        senseiTest.setGrado(GradoCinturon.NEGRO_1_DAN);
        senseiTest.setAnosPractica(10);
        senseiTest.setComisionPorcentaje(BigDecimal.valueOf(15));
        senseiTest.setEsClubPropio(false);
        senseiRepo.save(senseiTest);

        // 3. Grupo
        grupoTest = new GrupoEntrenamiento();
        grupoTest.setNombre("Grupo Asistencia Test - " + UUID.randomUUID());
        grupoTest.setSensei(senseiTest);
        grupoTest.setTarifaMensual(BigDecimal.valueOf(80000));
        grupoTest.setComisionSensei(BigDecimal.valueOf(10));
        grupoRepo.save(grupoTest);

        // 4. Microciclo válido (con fecha y estado)
        microcicloTest = new Microciclo();
        microcicloTest.setNombre("Microciclo Test Asistencia");
        microcicloTest.setSensei(senseiTest);
        microcicloTest.setFechaInicio(LocalDate.now().minusDays(7));
        microcicloTest.setFechaFin(LocalDate.now().plusDays(7));
        microcicloTest.setEstado(EstadoMicrociclo.ACTIVO);
        microcicloRepo.save(microcicloTest);

        // 5. Judokas activos en el grupo
        judoka1 = crearJudokaConUsuario("Juan", "Perez", "juan.asistencia@test.com");
        judoka2 = crearJudokaConUsuario("Ana", "Gomez", "ana.asistencia@test.com");

        // Asociar judokas al grupo (ManyToMany)
        grupoTest.getJudokas().add(judoka1);
        grupoTest.getJudokas().add(judoka2);
        grupoRepo.save(grupoTest);
    }

    private Judoka crearJudokaConUsuario(String nombre, String apellido, String email) {
        Rol rolAcudiente = rolRepo.findByNombre("ROLE_ACUDIENTE")
                .orElseGet(() -> rolRepo.save(new Rol("ROLE_ACUDIENTE")));

        Usuario usuario = new Usuario(email, "hash", nombre, apellido);
        usuario.setActivo(true);
        usuario.setRoles(java.util.Set.of(rolAcudiente));
        usuarioRepo.save(usuario);

        Judoka judoka = new Judoka();
        judoka.setNombre(nombre);
        judoka.setApellido(apellido);
        judoka.setAcudiente(usuario);
        judoka.setSensei(senseiTest);
        judoka.setEstado(EstadoJudoka.ACTIVO);
        judoka.setMayorEdad(false);
        judoka.setGrupoFacturacion(grupoTest);
        judoka.setGrupo(grupoTest);
        return judokaRepo.save(judoka);
    }

    @Test
    @DisplayName("Guardar sesión ejecutada con asistencias y verificar persistencia")
    void testGuardarSesionConAsistencias() {
        // Arrange
        Asistencia asistencia1 = new Asistencia(judoka1, EstadoAsistencia.PRESENTE);
        Asistencia asistencia2 = new Asistencia(judoka2, EstadoAsistencia.AUSENTE);

        SesionEjecutada sesion = new SesionEjecutada();
        sesion.setSensei(senseiTest);
        sesion.setGrupo(grupoTest);
        sesion.setMicrociclo(microcicloTest);
        sesion.setNotasRetroalimentacion("Buena clase, mejorar ukemi");
        sesion.addAsistencia(asistencia1);
        sesion.addAsistencia(asistencia2);

        // Act
        SesionEjecutada guardada = sesionEjecutadaService.guardarSesion(sesion);
        em.flush();
        em.clear();

        // Assert
        assertNotNull(guardada.getId());
        assertEquals(2, guardada.getListaAsistencia().size());

        // Verificar desde el repositorio de asistencias
        List<Asistencia> asistenciasDb = asistenciaRepo.findBySesionId(guardada.getId());
        assertEquals(2, asistenciasDb.size());

        // Verificar estado de cada judoka
        Asistencia a1 = asistenciasDb.stream()
                .filter(a -> a.getJudoka().getId().equals(judoka1.getId()))
                .findFirst().orElseThrow();
        assertEquals(EstadoAsistencia.PRESENTE, a1.getEstado());

        Asistencia a2 = asistenciasDb.stream()
                .filter(a -> a.getJudoka().getId().equals(judoka2.getId()))
                .findFirst().orElseThrow();
        assertEquals(EstadoAsistencia.AUSENTE, a2.getEstado());

        // Verificar notas de la sesión
        SesionEjecutada sesionRecargada = sesionEjecutadaService.obtenerPorId(guardada.getId());
        assertEquals("Buena clase, mejorar ukemi", sesionRecargada.getNotasRetroalimentacion());
    }
}