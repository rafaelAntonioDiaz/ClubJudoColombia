package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test de Integracion: Flujos de Admision del Club Judo Colombia.
 *
 * Escenarios cubiertos:
 *   1. Master invita a un Judoka Adulto directamente (auto-acudiente).
 *   2. Sensei invita a un Acudiente; el acudiente activa su cuenta.
 *      El menor se registra despues, en un flujo separado de UI.
 *   3. Master invita a un Sensei externo (de otro club).
 *
 * =========================================================================
 * ESTRATEGIA DE AISLAMIENTO -- leer antes de modificar el setUp
 * =========================================================================
 *
 * PROBLEMA RAIZ:
 *   @SpringBootTest reutiliza el mismo contexto de Spring (y la misma H2 en
 *   memoria) para TODOS los tests de la suite. Con create-drop, el esquema
 *   se crea al inicio y se destruye al final de la suite completa, no entre
 *   tests individuales.
 *
 *   Si @BeforeEach crea "master_admin" y "sensei@test.com" (username UNIQUE)
 *   en cada test, el segundo test falla con violacion de unicidad porque:
 *     a) @Transactional en la clase hace rollback del CUERPO del @Test, pero
 *        @BeforeEach corre DENTRO de la misma transaccion del test.
 *     b) Spring Data llama a flush() internamente (p.ej. en saveAndFlush),
 *        dejando el registro en H2 antes de que el rollback ocurra.
 *     c) El setUp del segundo test intenta insertar el mismo username -> ERROR.
 *
 * SOLUCION: @TestInstance(PER_CLASS) + @BeforeAll para fixtures con UNIQUE.
 *
 *   @TestInstance(PER_CLASS):
 *     JUnit crea UNA sola instancia de la clase para toda la suite.
 *     Permite que @BeforeAll sea un metodo de instancia (no estatico) y
 *     acceda a los beans @Autowired de Spring.
 *
 *   @BeforeAll @Transactional:
 *     Roles y "master_admin" se insertan UNA SOLA VEZ con su propio commit.
 *     Quedan en H2 de forma permanente hasta que create-drop destruya el
 *     esquema al final de la suite. Ningun test los insertara de nuevo.
 *
 *   @BeforeEach (sin @Transactional propio):
 *     Corre DENTRO de la transaccion del @Test. Crea el Sensei y el Grupo
 *     con un email unico (UUID) por test. Al terminar el @Test, el rollback
 *     de @Transactional los limpia, garantizando aislamiento entre tests.
 *
 *   @Transactional en la clase:
 *     Cada @Test hace rollback de sus propios datos (judoka, acudiente
 *     invitado, token), sin afectar los fixtures de @BeforeAll.
 * =========================================================================
 */
@SpringBootTest(properties = {
        // Credenciales falsas para que R2StorageService no falle al arrancar
        "spring.datasource.url=jdbc:h2:mem:judo_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "cloudflare.r2.access-key=dummy-key",
        "cloudflare.r2.secret-key=dummy-secret",
        "cloudflare.r2.bucket-name=dummy-bucket",
        "cloudflare.r2.public-url=https://dummy.r2.dev",
        "cloudflare.r2.endpoint=https://dummy.r2.cloudflarestorage.com",
        // H2 recrea el esquema al inicio/fin de la suite completa (no entre tests)
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Una instancia para toda la suite
@Transactional // Cada @Test corre en su propia transaccion con rollback automatico
public class FlujoAdmisionesTest {

    // -------------------------------------------------------------------------
    // Dependencias reales inyectadas por Spring
    // -------------------------------------------------------------------------
    @Autowired private AdmisionesService admisionesService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JudokaRepository judokaRepo;
    @Autowired private EntityManager em;
    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private SenseiRepository senseiRepo;
    @Autowired private GrupoEntrenamientoRepository grupoRepo;

    /**
     * Mock de SecurityService (Spring Boot 3.4+).
     * Reemplaza el bean real en el contexto de prueba. Cada test configura
     * quien esta "autenticado" con when(...).thenReturn(...).
     * NOTA: @MockBean esta deprecada desde Spring Boot 3.4. Usar @MockitoBean.
     */
    @MockitoBean
    private SecurityService securityService;

    // -------------------------------------------------------------------------
    // Fixtures de test
    // -------------------------------------------------------------------------

    /**
     * Sensei de prueba. Se recrea en @BeforeEach con email unico (UUID)
     * para que el rollback de @Transactional lo limpie sin conflictos.
     */
    private Sensei senseiTest;

    /**
     * Grupo de entrenamiento del senseiTest. Se recrea junto con el Sensei
     * en cada @BeforeEach para tener un ID fresco y consistente.
     */
    private GrupoEntrenamiento grupoTest;

    // =========================================================================
    // SETUP GLOBAL: roles + usuario Master (una sola vez para toda la suite)
    // =========================================================================

    /**
     * Inserta los datos que deben existir UNA SOLA VEZ en H2 para todos los tests.
     *
     * Por que @BeforeAll y no @BeforeEach:
     *   Los roles y "master_admin" tienen restriccion UNIQUE. Si se insertan en
     *   @BeforeEach, el segundo test falla porque el registro del primer setUp
     *   no se revierte completamente (ver ESTRATEGIA DE AISLAMIENTO arriba).
     *   Con @BeforeAll + @Transactional propio, el commit es inmediato y los
     *   datos quedan en H2 sin rollback, disponibles para todos los tests.
     *
     * Por que @Transactional aqui:
     *   @BeforeAll corre fuera de la transaccion del @Test. Sin @Transactional
     *   propio, los save() no harian flush y los datos podrian no estar en H2.
     *   Con @Transactional, el commit ocurre al cerrar este metodo.
     *
     * Requiere @TestInstance(PER_CLASS) para ser no-estatico y acceder a @Autowired.
     */
    @BeforeAll
    @Transactional
    void setUpFixturesGlobales() {
        // Roles del sistema: idempotente, solo crea si no existe
        Rol rolMaster = crearRolSiNoExiste("ROLE_MASTER");
        crearRolSiNoExiste("ROLE_SENSEI");
        crearRolSiNoExiste("ROLE_ACUDIENTE");
        crearRolSiNoExiste("ROLE_JUDOKA");
        crearRolSiNoExiste("ROLE_JUDOKA_ADULTO");
        crearRolSiNoExiste("ROLE_MECENAS");

        // Usuario Master: se crea solo si no existe (prevencion ante doble ejecucion)
        // activarJudoka() lo busca por username="master_admin" para decidir si
        // el sensei pertenece al club propio o a un cliente SaaS externo.
        if (usuarioRepo.findByUsername("master_admin").isEmpty()) {
            Usuario usuarioMaster = new Usuario(
                    "master_admin", "hash-irrelevante-setup", "Rafael", "Diaz");
            usuarioMaster.setEmail("master@clubjudo.com");
            usuarioMaster.setActivo(true);
            usuarioMaster.setRoles(Set.of(rolMaster));
            usuarioRepo.saveAndFlush(usuarioMaster); // flush inmediato -> commit al final del metodo
        }
    }

    // =========================================================================
    // SETUP POR TEST: Sensei + Grupo con email unico
    // =========================================================================

    /**
     * Prepara el Sensei y el Grupo de entrenamiento antes de CADA test.
     *
     * Usa un email con UUID para que no haya colision de unicidad entre tests,
     * incluso si por alguna razon el rollback anterior fue incompleto.
     *
     * Este metodo corre dentro de la transaccion del @Test (hereda la transaccion
     * abierta por @Transactional de la clase), por lo que el rollback al final
     * del test limpia al Sensei y al Grupo creados aqui.
     */
    @BeforeEach
    void setUp() {
        // Recuperar el rol SENSEI ya persistido en @BeforeAll
        Rol rolSensei = rolRepository.findByNombre("ROLE_SENSEI")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_SENSEI no existe. Verificar que setUpFixturesGlobales() se ejecuto correctamente."));

        // Email unico por test -> sin colisiones de username UNIQUE entre tests
        String emailSenseiUnico = "sensei-" + UUID.randomUUID() + "@test.com";

        Usuario usuarioSensei = new Usuario(
                emailSenseiUnico, "hash-irrelevante", "Carlos", "Gomez");
        usuarioSensei.setEmail(emailSenseiUnico);
        usuarioSensei.setActivo(true);
        usuarioSensei.setRoles(Set.of(rolSensei));
        usuarioRepo.save(usuarioSensei);

        senseiTest = new Sensei();
        senseiTest.setUsuario(usuarioSensei);
        senseiTest.setNombreClub("Club Judo Bucaramanga");
        senseiTest.setGrado(GradoCinturon.NEGRO_1_DAN);
        senseiTest.setAnosPractica(12);
        senseiTest.setComisionPorcentaje(BigDecimal.valueOf(15));
        senseiTest.setEsClubPropio(false);
        senseiRepo.save(senseiTest);

        // Grupo requerido por generarInvitacion() para roles JUDOKA y JUDOKA_ADULTO.
        // Campos NOT NULL sin valor por defecto en la entidad:
        //   - tarifaMensual  : nullable=false, sin default en GrupoEntrenamiento
        //   - comisionSensei : nullable=false, sin default en GrupoEntrenamiento
        // Los demas NOT NULL (esTarifario, incluyeMatricula, diasGracia) ya tienen
        // valores por defecto declarados en la entidad (false, false, 5).
        // El nombre tambien es UNIQUE en BD, por eso se le agrega UUID.
        grupoTest = new GrupoEntrenamiento();
        grupoTest.setNombre("Grupo Infantil A - " + UUID.randomUUID());
        grupoTest.setSensei(senseiTest);
        grupoTest.setTarifaMensual(BigDecimal.valueOf(80_000));  // NOT NULL sin default
        grupoTest.setComisionSensei(BigDecimal.valueOf(10));     // NOT NULL sin default
        grupoRepo.save(grupoTest);

        // Flush para que los IDs generados por H2 esten disponibles en los tests
        em.flush();
    }

    // =========================================================================
    // TEST 1: Master invita a un Judoka Adulto
    // =========================================================================

    /**
     * Escenario: El Master genera una invitacion para un judoka adulto.
     * El judoka adulto es su propio acudiente (patron auto-acudiente).
     *
     * Flujo completo:
     *   Master genera token
     *   -> Adulto activa cuenta con contrasena
     *   -> Admin carga documentos (Waiver + EPS)
     *   -> Admin registra pago de matricula
     *   -> Admin activa judoka
     *
     * Validaciones:
     *   - Token generado no es nulo.
     *   - Judoka queda en estado ACTIVO.
     *   - Matricula marcada como pagada.
     *   - Usuario (auto-acudiente) queda activo.
     *   - Usuario tiene el rol ROLE_JUDOKA_ADULTO.
     */
    @Test
    @DisplayName("TEST 1 - Master invita Judoka Adulto: flujo completo hasta ACTIVO")
    void testMasterInvitaJudokaAdulto() {
        System.out.println("\n=== TEST 1: Master invita Judoka Adulto ===");

        // ARRANGE: Master autenticado -> sin Sensei asociado -> Optional.empty()
        when(securityService.getAuthenticatedSensei()).thenReturn(Optional.empty());

        // ACT - PASO 1: Master genera la invitacion
        System.out.println("-> PASO 1: Master genera invitacion para judoka adulto");
        String tokenUuid = admisionesService.generarInvitacion(
                "Laura",              // nombre
                "Martinez",           // apellido
                "laura@test.com",     // email (sera el username)
                "3001234567",         // celular
                "ROLE_JUDOKA_ADULTO", // rol a asignar
                "http://localhost:8080",
                null,                 // esClubPropio: no aplica a judokas
                grupoTest.getId(),    // grupoId: obligatorio para cualquier judoka
                null                  // comisionPorcentaje: solo aplica a ROLE_SENSEI
        );
        assertNotNull(tokenUuid, "El token de invitacion no debe ser nulo");
        System.out.println("   -> Token generado: " + tokenUuid);

        // ACT - PASO 2: La adulta activa su cuenta eligiendo su contrasena
        System.out.println("-> PASO 2: Adulta activa cuenta con contrasena");
        AdmisionesService.ActivationResult resultado =
                admisionesService.activarInvitacionConPassword(tokenUuid, "ClaveSegura123!");

        assertNotNull(resultado.getUsuario(), "El usuario activado no debe ser nulo");
        assertNotNull(resultado.getJudokaId(), "El judoka vinculado al token no debe ser nulo");

        Long judokaId = resultado.getJudokaId();

        // Limpiar la cache de primer nivel de Hibernate para forzar lectura fresca
        em.flush();
        em.clear();

        // ACT - PASO 3: Cargar documentos requeridos para la activacion
        System.out.println("-> PASO 3: Cargando documentos (Waiver y EPS)");
        Judoka aspirante = judokaRepo.findById(judokaId).orElseThrow();
        admisionesService.cargarRequisito(aspirante, TipoDocumento.WAIVER, "https://cloud.com/waiver.pdf");
        admisionesService.cargarRequisito(aspirante, TipoDocumento.EPS, "https://cloud.com/eps.pdf");

        em.flush();
        em.clear();

        // ACT - PASO 4: Registrar pago de matricula y activar judoka
        System.out.println("-> PASO 4: Registrando pago y activando judoka");
        aspirante = judokaRepo.findById(judokaId).orElseThrow();

        // Inicializar relaciones lazy ANTES de llamar a activarJudoka(),
        // que las necesita dentro de la misma transaccion para evitar LazyInit
        Hibernate.initialize(aspirante.getAcudiente().getRoles());
        Hibernate.initialize(aspirante.getDocumentos());

        admisionesService.registrarPagoMatricula(aspirante);
        admisionesService.activarJudoka(aspirante);

        em.flush();
        em.clear();

        // ASSERT - PASO 5: Verificar estado final
        System.out.println("-> PASO 5: Verificando estado final");
        Judoka judokaFinal = judokaRepo.findById(judokaId).orElseThrow();
        Hibernate.initialize(judokaFinal.getAcudiente().getRoles());

        assertEquals(EstadoJudoka.ACTIVO, judokaFinal.getEstado(),
                "El judoka debe estar en estado ACTIVO");
        assertTrue(judokaFinal.isMatriculaPagada(),
                "La matricula debe estar marcada como pagada");
        assertTrue(judokaFinal.getAcudiente().isActivo(),
                "El usuario (auto-acudiente) debe estar activo");

        boolean tieneRolAdulto = judokaFinal.getAcudiente().getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_JUDOKA_ADULTO"));
        assertTrue(tieneRolAdulto, "El usuario debe tener el rol ROLE_JUDOKA_ADULTO");

        System.out.println("TEST 1 EXITOSO: Judoka adulto activado correctamente.\n");
    }

    // =========================================================================
    // TEST 2: Sensei invita Acudiente (menor se registra despues)
    // =========================================================================

    /**
     * Escenario: Un Sensei autenticado genera una invitacion para un Acudiente
     * (padre/madre). El Acudiente activa su cuenta. El registro del menor ocurre
     * en un flujo posterior de UI (fuera del alcance de este test).
     *
     * Flujo:
     *   Sensei genera token para ROLE_ACUDIENTE
     *   -> Acudiente activa cuenta con contrasena
     *   -> Verificar estado final del acudiente
     *
     * Validaciones:
     *   - Token generado no es nulo.
     *   - Usuario queda activo.
     *   - Usuario tiene el rol ROLE_ACUDIENTE.
     *   - Grupo tarifario asignado (para herencia al futuro menor).
     *   - NO se crea ningun Judoka en este paso (judokaId es null).
     */
    @Test
    @DisplayName("TEST 2 - Sensei invita Acudiente: flujo hasta cuenta activa (menor se registra despues)")
    void testSenseiInvitaAcudiente() {
        System.out.println("\n=== TEST 2: Sensei invita Acudiente ===");

        // ARRANGE: El Sensei esta autenticado en la sesion
        when(securityService.getAuthenticatedSensei()).thenReturn(Optional.of(senseiTest));

        // ACT - PASO 1: Sensei genera invitacion para el acudiente
        // grupoId se pasa para que el sistema asigne el grupo tarifario al acudiente,
        // de modo que cuando registre al menor, el grupo se herede automaticamente.
        System.out.println("-> PASO 1: Sensei genera invitacion para acudiente");
        String tokenUuid = admisionesService.generarInvitacion(
                "Maria",            // nombre del acudiente
                "Gonzalez",         // apellido
                "maria@test.com",   // email
                "3109876543",       // celular
                "ROLE_ACUDIENTE",
                "http://localhost:8080",
                null,               // esClubPropio: no aplica a acudientes
                grupoTest.getId(),  // grupoId: para heredar al futuro menor
                null                // comisionPorcentaje: no aplica
        );
        assertNotNull(tokenUuid, "El token de invitacion no debe ser nulo");
        System.out.println("   -> Token generado: " + tokenUuid);

        // ACT - PASO 2: El acudiente activa su cuenta
        System.out.println("-> PASO 2: Acudiente activa su cuenta con contrasena");
        AdmisionesService.ActivationResult resultado =
                admisionesService.activarInvitacionConPassword(tokenUuid, "MiClave2024!");

        assertNotNull(resultado.getUsuario(), "El usuario activado no debe ser nulo");
        // Para ROLE_ACUDIENTE no se crea Judoka en este paso del flujo
        assertNull(resultado.getJudokaId(),
                "No debe existir judoka vinculado: el menor se registra en un paso posterior");

        em.flush();
        em.clear();

        // ASSERT - PASO 3: Verificar estado final del acudiente
        System.out.println("-> PASO 3: Verificando estado final del acudiente");
        Usuario acudienteFinal = usuarioRepo.findByUsername("maria@test.com").orElseThrow();
        Hibernate.initialize(acudienteFinal.getRoles());
        Hibernate.initialize(acudienteFinal.getGrupoTarifario());

        assertTrue(acudienteFinal.isActivo(),
                "El acudiente debe estar activo");

        boolean tieneRolAcudiente = acudienteFinal.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_ACUDIENTE"));
        assertTrue(tieneRolAcudiente, "El usuario debe tener el rol ROLE_ACUDIENTE");

        assertNotNull(acudienteFinal.getGrupoTarifario(),
                "El acudiente debe tener grupo tarifario asignado para herencia al menor");
        assertEquals(grupoTest.getId(), acudienteFinal.getGrupoTarifario().getId(),
                "El grupo tarifario debe corresponder al del Sensei que invito");

        System.out.println("TEST 2 EXITOSO: Acudiente activado. Menor se registrara en paso posterior.\n");
    }

    // =========================================================================
    // TEST 3: Master invita Sensei Externo
    // =========================================================================

    /**
     * Escenario: El Master invita a un Sensei externo (de otro club).
     * El Sensei activa su cuenta y queda listo para crear su propio espacio
     * en la plataforma SaaS.
     *
     * Flujo:
     *   Master genera token para ROLE_SENSEI (esClubPropio=false)
     *   -> Sensei activa cuenta con contrasena
     *   -> Verificar estado final
     *
     * Validaciones:
     *   - Token generado no es nulo.
     *   - Usuario queda activo.
     *   - Usuario tiene el rol ROLE_SENSEI.
     *   - No hay Judoka vinculado al token (un sensei no es un judoka).
     */
    @Test
    @DisplayName("TEST 3 - Master invita Sensei Externo: flujo hasta cuenta activa")
    void testMasterInvitaSenseiExterno() {
        System.out.println("\n=== TEST 3: Master invita Sensei Externo ===");

        // ARRANGE: El Master invita -> no hay Sensei autenticado
        when(securityService.getAuthenticatedSensei()).thenReturn(Optional.empty());

        BigDecimal comisionAcordada = BigDecimal.valueOf(10);

        // ACT - PASO 1: Master genera la invitacion para el sensei externo
        System.out.println("-> PASO 1: Master genera invitacion para Sensei externo");
        String tokenUuid = admisionesService.generarInvitacion(
                "Jorge",              // nombre
                "Ramirez",            // apellido
                "jorge@dojobga.com",  // email
                "3156667788",         // celular
                "ROLE_SENSEI",
                "http://localhost:8080",
                false,                // esClubPropio=false -> sensei externo, no del club del master
                null,                 // grupoId: no aplica para ROLE_SENSEI
                comisionAcordada      // porcentaje de comision pactado con el master
        );
        assertNotNull(tokenUuid, "El token de invitacion no debe ser nulo");
        System.out.println("   -> Token generado: " + tokenUuid);

        // ACT - PASO 2: El sensei externo activa su cuenta
        System.out.println("-> PASO 2: Sensei externo activa su cuenta");
        AdmisionesService.ActivationResult resultado =
                admisionesService.activarInvitacionConPassword(tokenUuid, "DojoSeguro456!");

        assertNotNull(resultado.getUsuario(), "El usuario del Sensei no debe ser nulo");
        assertNull(resultado.getJudokaId(),
                "No debe haber judoka vinculado: los senseis no son judokas");

        em.flush();
        em.clear();

        // ASSERT - PASO 3: Verificar estado final del Sensei
        System.out.println("-> PASO 3: Verificando estado final del Sensei");
        Usuario senseiUsuarioFinal = usuarioRepo.findByUsername("jorge@dojobga.com").orElseThrow();
        Hibernate.initialize(senseiUsuarioFinal.getRoles());

        assertTrue(senseiUsuarioFinal.isActivo(),
                "El usuario del Sensei debe estar activo");

        boolean tieneRolSensei = senseiUsuarioFinal.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_SENSEI"));
        assertTrue(tieneRolSensei, "El usuario debe tener el rol ROLE_SENSEI");

        System.out.println("TEST 3 EXITOSO: Sensei externo activado correctamente.\n");
    }

    // =========================================================================
    // Metodo auxiliar
    // =========================================================================

    /**
     * Busca un Rol por nombre y lo crea si no existe. Idempotente.
     *
     * Se usa en @BeforeAll para garantizar que los roles del sistema existan
     * sin importar el orden de ejecucion de los tests o si la suite se corre
     * mas de una vez (aunque create-drop lo previene en la practica).
     *
     * @param nombre Nombre del rol (ej: "ROLE_SENSEI")
     * @return El Rol existente o recien creado y persistido
     */
    private Rol crearRolSiNoExiste(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }
}