package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "cloudflare.r2.access-key=dummy-key",
        "cloudflare.r2.secret-key=dummy-secret",
        "cloudflare.r2.bucket-name=dummy-bucket",
        "cloudflare.r2.public-url=https://dummy.r2.dev",
        "cloudflare.r2.endpoint=https://dummy.r2.cloudflarestorage.com",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class FlujoAdmisionesTest {

    @Autowired private AdmisionesService admisionesService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JudokaRepository judokaRepo;
    @Autowired private EntityManager em;

    @Test
    @DisplayName("🤖 TEST E2E: Flujo de Admisión Completo")
    void testFlujoCompletoAdmisiones() {
        System.out.println("--- INICIANDO ROBOT DE PRUEBA ---");

        // =====================================================================
        // ACTO 1: El Sensei inicia el proceso (Genera Token)
        // =====================================================================
        System.out.println("-> PASO 1: Generando invitación para aspirante");

        // Usamos el método disponible en AdmisionesService
        String tokenUuid = admisionesService.crearAspiranteYGenerarToken("Andrés", "Pérez", "andres@test.com");
        assertNotNull(tokenUuid);

        // =====================================================================
        // ACTO 2: El Aspirante establece su clave
        // =====================================================================
        System.out.println("-> PASO 2: Aspirante establece su contraseña");
        Judoka aspirante = admisionesService.obtenerJudokaPorToken(tokenUuid);

        // IMPORTANTE: Usamos usuarioService.saveUsuario(Usuario, String) que es el método disponible
        usuarioService.saveUsuario(aspirante.getUsuario(), "ClaveSegura123");
        admisionesService.consumirToken(tokenUuid);

        assertEquals(EstadoJudoka.PENDIENTE, aspirante.getEstado());

        // =====================================================================
        // ACTO 3: Carga de Requisitos Documentales
        // =====================================================================
        System.out.println("-> PASO 3: Cargando documentos requeridos");

        // admisionesService.cargarRequisito es el método disponible
        admisionesService.cargarRequisito(aspirante, TipoDocumento.WAIVER, "http://cloud.com/waiver.pdf");
        admisionesService.cargarRequisito(aspirante, TipoDocumento.EPS, "http://cloud.com/eps.pdf");

        // Forzamos sincronización con la BD para que las colecciones se actualicen
        em.flush();
        em.clear();

        // Recargamos el objeto para verificar la carga
        aspirante = judokaRepo.findById(aspirante.getId()).orElseThrow();
        assertFalse(aspirante.getDocumentos().isEmpty(), "No se guardaron los documentos");

        // =====================================================================
        // ACTO 4: Gestión Administrativa y Activación
        // =====================================================================
        System.out.println("-> PASO 4: Registrando pago y activando");

        admisionesService.registrarPagoMatricula(aspirante);

        // Este método valida internamente el WAIVER y el PAGO
        admisionesService.activarJudoka(aspirante);

        // =====================================================================
        // ACTO 5: Verificación del Estado Final
        // =====================================================================
        System.out.println("-> PASO 5: Validaciones finales de seguridad");

        Judoka judokaFinal = judokaRepo.findById(aspirante.getId()).orElseThrow();
        Usuario usuarioFinal = judokaFinal.getUsuario();

        assertEquals(EstadoJudoka.ACTIVO, judokaFinal.getEstado());
        assertTrue(judokaFinal.isMatriculaPagada());
        assertTrue(usuarioFinal.isActivo());

        // Verificamos que tenga el rol ROLE_JUDOKA
        boolean tieneRol = usuarioFinal.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals("ROLE_JUDOKA"));
        assertTrue(tieneRol, "El usuario no recibió el rol ROLE_JUDOKA");

        System.out.println("✅ PRUEBA EXITOSA: Flujo coherente con los servicios actuales.");
    }
}