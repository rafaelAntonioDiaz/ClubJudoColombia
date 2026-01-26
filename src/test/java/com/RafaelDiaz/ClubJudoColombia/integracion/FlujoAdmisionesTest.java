package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
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
        // --- LA LÍNEA MÁGICA PARA DOMAR A H2 ---
        "spring.jpa.hibernate.ddl-auto=create-drop"
        })
@Transactional // <- CRÍTICO: Borra todos los datos de prueba al terminar
public class FlujoAdmisionesTest {

    @Autowired private AdmisionesService admisionesService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JudokaService judokaService;
    @Autowired private jakarta.persistence.EntityManager em;
    @Test
    @DisplayName("🤖 TEST E2E: Flujo de Admisión (Invitación -> Documentos -> Activación)")
    void testFlujoCompletoAdmisiones() {
        System.out.println("--- INICIANDO ROBOT DE PRUEBA ---");

        // =====================================================================
        // ACTO 1: El Sensei Invita (Usando UsuarioService existente)
        // =====================================================================
        System.out.println("-> PASO 1: Sensei envía invitación");
        Usuario sensei = usuarioService.findByUsername("kiuzo").orElseThrow();

        // Generamos el token de invitación
        String tokenUuid = admisionesService.crearAspiranteYGenerarToken("Andrés", "Pérez", "andres@test.com");
        assertNotNull(tokenUuid, "Error: El token de invitación no se generó");

        // =====================================================================
        // ACTO 2: El Aspirante canjea el Token y establece su clave
        // =====================================================================
        System.out.println("-> PASO 2: Aspirante canjea token y establece contraseña");
        Judoka aspirante = admisionesService.obtenerJudokaPorToken(tokenUuid);

        // El UsuarioService que usted ya tiene se encarga de hashear la clave
        usuarioService.saveUsuario(aspirante.getUsuario(), "Secreto123");
        admisionesService.consumirToken(tokenUuid); // Quemamos el token

        assertEquals(EstadoJudoka.PENDIENTE, aspirante.getEstado(), "El aspirante debe nacer en estado PENDIENTE");

        // =====================================================================
        // ACTO 3: Aspirante sube documentos a Cloudflare
        // =====================================================================
        System.out.println("-> PASO 3: Subiendo documentos (Simulación Cloudflare)");
        String urlWaiver = "https://pub-r2.dev/waiver_andres.pdf";
        String urlEps = "https://pub-r2.dev/eps_andres.pdf";
        String urlPago = "https://pub-r2.dev/pago_nequi_andres.png";

        admisionesService.cargarRequisito(aspirante, TipoDocumento.WAIVER, urlWaiver);
        admisionesService.cargarRequisito(aspirante, TipoDocumento.EPS, urlEps);
        admisionesService.cargarRequisito(aspirante, TipoDocumento.COMPROBANTE_PAGO, urlPago);
        em.flush(); // Obliga a escribir todo en la BD real
        em.clear(); // Borra la memoria RAM del test para obligarlo a recargar
        // Recargamos el Judoka desde la BD para asegurar que se guardó
        aspirante = judokaService.findByUsuario(aspirante.getUsuario()).orElseThrow();
        assertEquals(3, aspirante.getDocumentos().size(), "Faltan documentos en la BD");

        // =====================================================================
        // ACTO 4: El Sensei Aprueba
        // =====================================================================
        System.out.println("-> PASO 4: Sensei activa al Judoka");
        admisionesService.registrarPagoMatricula(aspirante);
        admisionesService.activarJudoka(aspirante); // ESTE ES EL GRAN TRIGGER

        // =====================================================================
        // ACTO 5: Verificación Final
        // =====================================================================
        System.out.println("-> PASO 5: Verificando permisos finales");

        // Refrescamos datos
        aspirante = judokaService.findByUsuario(aspirante.getUsuario()).orElseThrow();
        Usuario usuarioFinal = aspirante.getUsuario();

        // VALIDACIONES CRÍTICAS (Si alguna falla, el test se detiene y marca ROJO)
        assertEquals(EstadoJudoka.ACTIVO, aspirante.getEstado(), "¡Error! No quedó ACTIVO");
        assertTrue(aspirante.isMatriculaPagada(), "No se marcó la matrícula como pagada");
        assertTrue(usuarioFinal.isActivo(), "El usuario de Spring Security sigue inactivo");
        assertTrue(usuarioFinal.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_JUDOKA")),
                "No se le asignó el rol ROLE_JUDOKA al aspirante");

        System.out.println("✅ PRUEBA EXITOSA: El flujo funciona a la perfección.");
    }
}