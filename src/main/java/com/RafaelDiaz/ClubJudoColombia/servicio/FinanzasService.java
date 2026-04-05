package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FinanzasService {

    // Constantes para evitar "Magic Strings"
    private static final String CONCEPTO_NOMINA = "PAGO_NOMINA_SENSEI";
    private static final String CONCEPTO_VARIOS = "INGRESO_VARIOS";
    private static final String CONCEPTO_MENSUALIDAD_SAAS = "MENSUALIDAD_PLATAFORMA";
    private static final String CONCEPTO_MENSUALIDAD_CLUB = "MENSUALIDAD_CLUB";

    private final JudokaRepository judokaRepo;
    private final SenseiRepository senseiRepo;
    private final MovimientoCajaRepository movimientoRepo;
    private final CuentaCobroRepository cuentaCobroRepo;
    private final ConceptoFinancieroRepository conceptoRepo;
    private final PagoRepository pagoRepo;
    private final GrupoEntrenamientoRepository grupoRepository;

    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final ConfiguracionService configService;
    private static final String CONCEPTO_OTROS = "OTROS";
    public FinanzasService(JudokaRepository judokaRepo,
                           SenseiRepository senseiRepo,
                           MovimientoCajaRepository movimientoRepo,
                           CuentaCobroRepository cuentaCobroRepo,
                           ConceptoFinancieroRepository conceptoRepo,
                           PagoRepository pagoRepo, GrupoEntrenamientoRepository grupoRepository,
                           SecurityService securityService,
                           TraduccionService traduccionService,
                           ConfiguracionService configService) {
        this.judokaRepo = judokaRepo;
        this.senseiRepo = senseiRepo;
        this.movimientoRepo = movimientoRepo;
        this.cuentaCobroRepo = cuentaCobroRepo;
        this.conceptoRepo = conceptoRepo;
        this.pagoRepo = pagoRepo;
        this.grupoRepository = grupoRepository;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.configService = configService;
    }
    // --- CONCEPTOS ---
    public List<ConceptoFinanciero> obtenerConceptosPorTipo(TipoTransaccion tipo) {
        return conceptoRepo.findByTipo(tipo);
    }

    public ConceptoFinanciero guardarConcepto(ConceptoFinanciero concepto) {
        return conceptoRepo.save(concepto);
    }
    // =========================================================================
    // 1. MOTOR TRANSACCIONAL (CAJA)
    // =========================================================================

    /**
     * Registra cualquier movimiento en la caja del club.
     * Sobrecarga principal que recibe la entidad completa.
     */
    @Transactional
    public MovimientoCaja registrarMovimiento(MovimientoCaja movimiento) {
        securityService.getAuthenticatedUserDetails().ifPresent(u ->
                movimiento.setRegistradoPor(u.getUsername())
        );
        if (movimiento.getFecha() == null) movimiento.setFecha(LocalDateTime.now());
        return movimientoRepo.save(movimiento);
    }

    /**
     * Helper simplificado para registrar ingresos/egresos rápidos desde las vistas.
     */
    @Transactional
    public MovimientoCaja registrarMovimiento(
            BigDecimal monto,
            TipoTransaccion tipo,
            MetodoPago metodoPago,
            String nombreConcepto,
            Judoka judoka, // Opcional
            String observacion,
            String urlSoporte
    ) {
        MovimientoCaja mov = new MovimientoCaja();
        mov.setMonto(monto);
        mov.setTipo(tipo);
        mov.setMetodoPago(metodoPago);
        mov.setJudoka(judoka);
        mov.setObservacion(observacion);
        mov.setUrlSoporte(urlSoporte);

        // Auto-gestión de Conceptos Financieros
        final String nombreABuscar = (nombreConcepto == null) ? CONCEPTO_OTROS : nombreConcepto;
        ConceptoFinanciero concepto = conceptoRepo.findByNombre(nombreABuscar)
                    .orElseGet(() -> conceptoRepo.save(
                            new ConceptoFinanciero(nombreABuscar, tipo, BigDecimal.ZERO)));
            mov.setConcepto(concepto);

        return registrarMovimiento(mov);
    }
    public List<MovimientoCaja> obtenerMovimientosDelMes() {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        // Cambiar el método llamado para asegurar la carga de relaciones
        return movimientoRepo.findByFechaBetweenWithDetails(inicio, fin);
    }
    // --- DASHBOARD DE BALANCE ---
    public BigDecimal calcularTotalIngresosMes() {
        return calcularTotal(TipoTransaccion.INGRESO);
    }

    public BigDecimal calcularTotalEgresosMes() {
        return calcularTotal(TipoTransaccion.EGRESO);
    }

    private BigDecimal calcularTotal(TipoTransaccion tipo) {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        BigDecimal total = movimientoRepo.sumarTotalPorTipoYFecha(tipo, inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calcularBalanceMes() {
        BigDecimal ingresos = calcularTotalIngresosMes();
        BigDecimal egresos = calcularTotalEgresosMes();
        return ingresos.subtract(egresos);
    }
    /**
     * MÉTODO MAGNO: Procesa el pago de un Judoka y reparte la plata.
     */
    @Transactional
    public void procesarPagoMensualidad(Judoka judoka, String metodoPago) {
        // 1. Validar montos (Aquí podrías recibir el monto real si varía)

        // 2. ACTIVAR AL JUDOKA (Suscripción + 30 días)
        LocalDate nuevaFecha = (judoka.getFechaVencimientoSuscripcion() == null || judoka.getFechaVencimientoSuscripcion().isBefore(LocalDate.now()))
                ? LocalDate.now().plusDays(30) // Si estaba vencido, cuenta desde hoy
                : judoka.getFechaVencimientoSuscripcion().plusDays(30); // Si paga adelantado, se suma

        judoka.setFechaVencimientoSuscripcion(nuevaFecha);
        judoka.setSuscripcionActiva(true);
        judoka.setMatriculaPagada(true); // Asumimos que estar al día implica matrícula ok
        judokaRepo.save(judoka);

        // 3. PAGAR AL SENSEI (Revenue Share)
        Sensei sensei = judoka.getSensei();
        if (sensei != null) {
            sensei.abonarComision(configService.getAsBigDecimal("COMISION_SENSEI_MENSUALIDAD")); // +5.000 a la billetera
            senseiRepo.save(sensei);
            System.out.println("💰 Finanzas: " + configService.getAsBigDecimal("COMISION_SENSEI_MENSUALIDAD") + " abonados a Sensei " + sensei.getUsuario().getNombre());
        }

        // 4. REGISTRAR MOVIMIENTO EN CAJA (Auditoría)
        // Ojo: Aquí registramos que entraron 15.000, pero internamente sabemos que 5.000 son pasivo.
        // Podrías crear un MovimientoCaja real aquí si quieres historial.
    }

    // =========================================================================
    // 2. CICLO DE COBRO (DEUDA -> PAGO -> ACTIVACIÓN)
    // =========================================================================

    /**
     * CRON JOB: Genera las cuentas de cobro mensuales para todos los judokas activos,
     * agrupados por su grupo de facturación.
     */
    @Transactional
    public void generarCobrosMensualesMasivos() {
        LocalDate hoy = LocalDate.now();
        String periodo = hoy.getMonth().name() + " " + hoy.getYear();

        // Obtener todos los grupos que tienen al menos un judoka activo
        List<GrupoEntrenamiento> grupos = grupoRepository.findAll(); // O puedes filtrar con una consulta más específica

        for (GrupoEntrenamiento grupo : grupos) {
            // Obtener judokas activos que facturan con este grupo
            // Asumiendo que tienes un repositorio de Judoka con un método como:
            // List<Judoka> findByGrupoFacturacionAndEstado(GrupoEntrenamiento grupo, EstadoJudoka estado)
            List<Judoka> judokasActivos = judokaRepo.findByGrupoFacturacionAndEstado(grupo, EstadoJudoka.ACTIVO);

            for (Judoka judoka : judokasActivos) {
                Usuario responsable = obtenerResponsableFinanciero(judoka);

                if (responsable == null) continue;

                // Evitar duplicados (misma persona, mismo período)
                if (!existeCobro(judoka, periodo)) {
                    crearCuentaCobro(
                            judoka,
                            responsable,
                            grupo.getTarifaMensual(),
                            grupo.getComisionSensei(),
                            traduccionService.get("finanzas.concepto.mensualidad", grupo.getNombre(), periodo),
                            grupo.getDiasGracia()
                    );
                }
            }
        }
    }

    /**
     * PROCESO DE PAGO (El método más importante del sistema)
     */
    @Transactional
    public void pagarCuentaCobro(Long idCuenta, MetodoPago metodoPago, String urlComprobante) {
        // 1. Validación
        CuentaCobro cuenta = cuentaCobroRepo.findById(idCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (cuenta.getEstado() == EstadoPago.PAGADO) {
            throw new RuntimeException("Esta cuenta ya fue pagada.");
        }

        // 2. Registro de Evidencia (Pago)
        Pago pago = new Pago();
        pago.setUsuario(cuenta.getResponsablePago());
        pago.setCuentaCobro(cuenta);
        pago.setMonto(cuenta.getValorTotal().doubleValue());
        pago.setMetodoPago(metodoPago);
        pago.setUrlComprobante(urlComprobante);
        pago.setEstado(EstadoPago.PAGADO);
        pago.setFechaPagoExitoso(LocalDateTime.now());
        pagoRepo.save(pago);

        // 3. Ingreso Contable (Caja)
        registrarMovimiento(
                cuenta.getValorTotal(),
                TipoTransaccion.INGRESO,
                metodoPago,
                cuenta.getConcepto(),
                cuenta.getJudokaBeneficiario(),
                "Cobro Cuenta #" + cuenta.getId(),
                urlComprobante
        );

        // 4. Dispersión de Fondos (Revenue Share) - CORREGIDO: usa la comisión de la cuenta
        if (cuenta.getValorComisionSensei() != null &&
                cuenta.getValorComisionSensei().compareTo(BigDecimal.ZERO) > 0) {

            Sensei sensei = cuenta.getJudokaBeneficiario().getSensei();
            if (sensei != null) {
                sensei.abonarComision(cuenta.getValorComisionSensei()); // ✅ Usa el valor de la cuenta
                senseiRepo.save(sensei);
            }
        }

        // 5. Activación de Servicios
        activarBeneficios(cuenta);

        // 6. Cierre
        cuenta.setEstado(EstadoPago.PAGADO);
        cuenta.setPagoAsociado(pago);
        cuentaCobroRepo.save(cuenta);
    }
    // =========================================================================
    // 3. GESTIÓN DE SENSEIS (WALLET)
    // =========================================================================

    @Transactional
    public void liquidarSaldoSensei(Sensei sensei, String urlComprobante) {
        BigDecimal saldo = sensei.getSaldoWallet();
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Saldo insuficiente para liquidar.");
        }

        // 1. Registro el Egreso en la Caja del Club
        registrarMovimiento(
                saldo,
                TipoTransaccion.EGRESO,
                MetodoPago.NEQUI,
                CONCEPTO_NOMINA,
                null, // No aplica Judoka
                "Liquidación comisiones: " + sensei.getUsuario().getNombre(),
                urlComprobante
        );

        // 2. Descuento de la Wallet Virtual
        sensei.descontarRetiro(saldo);
        senseiRepo.save(sensei);
    }

    // =========================================================================
    // 4. HELPERS Y CONSULTAS
    // =========================================================================

    private void crearCuentaCobro(Judoka j, Usuario resp, BigDecimal monto, BigDecimal comision, String concepto, int diaVence) {
        CuentaCobro cc = new CuentaCobro();
        cc.setJudokaBeneficiario(j);
        cc.setResponsablePago(resp);
        cc.setValorTotal(monto);
        cc.setValorComisionSensei(comision);
        cc.setConcepto(concepto);
        cc.setEstado(EstadoPago.PENDIENTE);
        cc.setFechaGeneracion(LocalDate.now());
        cc.setFechaVencimiento(LocalDate.now().withDayOfMonth(diaVence));
        cuentaCobroRepo.save(cc);
    }

    // Valida duplicados en el mes actual (por defecto)
    private boolean existeCobro(Judoka j, String conceptoCompleto) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        return cuentaCobroRepo.existsByJudokaBeneficiarioAndConceptoLikeAndFechaGeneracionAfter(
                j, conceptoCompleto, inicioMes);
    }

    // Valida duplicados con una fecha de inicio personalizada (ej. inicio de año)
    private boolean existeCobro(Judoka j, String conceptoCompleto, LocalDate fechaDesde) {
        return cuentaCobroRepo.existsByJudokaBeneficiarioAndConceptoLikeAndFechaGeneracionAfter(
                j, conceptoCompleto, fechaDesde);
    }

    private void activarBeneficios(CuentaCobro cuenta) {
        Judoka judoka = cuenta.getJudokaBeneficiario();
        String concepto = cuenta.getConcepto().toLowerCase();

        if (concepto.contains("mensualidad") || concepto.contains("suscripción")) {
            LocalDate base = (judoka.getFechaVencimientoSuscripcion() != null && judoka.getFechaVencimientoSuscripcion().isAfter(LocalDate.now()))
                    ? judoka.getFechaVencimientoSuscripcion()
                    : LocalDate.now();
            judoka.setFechaVencimientoSuscripcion(base.plusDays(30));
            judoka.setSuscripcionActiva(true);
        } else if (concepto.contains("judogi")) {
            judoka.setTieneJudogiAlquilado(true);
            judoka.setFechaDevolucionJudogi(LocalDate.now().plusYears(1));
        }
        judokaRepo.save(judoka);
    }

    public List<CuentaCobro> obtenerDeudasPendientes(Usuario usuario) {
        return cuentaCobroRepo.findByResponsablePagoAndEstadoWithJudoka(usuario, EstadoPago.PENDIENTE);
    }

    public BigDecimal calcularDeudaTotal(Usuario usuario) {
        return obtenerDeudasPendientes(usuario).stream()
                .map(CuentaCobro::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * MÉTODO DE ADMISIÓN (Mesa de Control):
     * Genera la primera factura según si es alumno del Master o de un Sensei externo.
     */
    /**
     * Genera las cuentas de cobro de bienvenida (matrícula + primera mensualidad)
     * basándose en el grupo de facturación del judoka.
     */
    @Transactional
    public void generarCobroBienvenida(Judoka judoka) {
        Usuario responsable = (judoka.getMecenas() != null) ?
                judoka.getMecenas().getUsuario() : judoka.getAcudiente();

        if (responsable == null) {
            throw new RuntimeException("Error: El judoka no tiene responsable financiero.");
        }

        // --- CORRECCIÓN: Herencia de grupo ---
        GrupoEntrenamiento grupo = judoka.getGrupoFacturacion();
        if (grupo == null && responsable.getGrupoTarifario() != null) {
            grupo = responsable.getGrupoTarifario();
            // Persistimos la herencia para evitar chequeos futuros
            judoka.setGrupoFacturacion(grupo);
            if (judoka.getGrupo() == null) judoka.setGrupo(grupo);
            if (judoka.getSensei() == null) judoka.setSensei(grupo.getSensei());
        }

        if (grupo == null) {
            throw new RuntimeException("Error: El judoka no tiene grupo de facturación asignado.");
        }

        // --- Evita duplicados ---
        String periodoActual = String.valueOf(LocalDate.now().getYear());
        String nombreGrupo = grupo.getNombre();

        // 1. Matrícula: Solo si no existe una para el año actual
        if (grupo.isIncluyeMatricula() && grupo.getMontoMatricula() != null &&
                grupo.getMontoMatricula().compareTo(BigDecimal.ZERO) > 0) {

            String labelMatricula = traduccionService.get("finanzas.concepto.matricula", periodoActual);
            LocalDate inicioAnio = LocalDate.now().withDayOfYear(1);

            if (!existeCobro(judoka, labelMatricula, inicioAnio)) {
                crearCuentaCobro(
                        judoka,
                        responsable,
                        grupo.getMontoMatricula(),
                        BigDecimal.ZERO,
                        labelMatricula,
                        grupo.getDiasGracia()
                );
            }
        }

        // 2. Primera mensualidad: Solo si no existe una para este grupo en el mes actual
        String labelMensualidad = traduccionService.get("finanzas.concepto.mensualidad_inicial", nombreGrupo);
        if (!existeCobro(judoka, labelMensualidad)) {
            crearCuentaCobro(
                    judoka,
                    responsable,
                    grupo.getTarifaMensual(),
                    grupo.getComisionSensei(),
                    labelMensualidad,
                    grupo.getDiasGracia()
            );
        }

        judokaRepo.save(judoka);
    }

    /**
     * CRON JOB: Ejecutar el día 6 de cada mes.
     * Busca cuentas vencidas y suspende a los judokas asociados.
     */
    @Transactional
    public void verificarImpagosYSuspender() {
        LocalDate hoy = LocalDate.now();

        // Buscamos todas las cuentas PENDIENTES cuya fecha de vencimiento ya pasó
        // Nota: Necesitarás agregar este método en tu CuentaCobroRepository:
        // List<CuentaCobro> findByEstadoAndFechaVencimientoBefore(EstadoPago estado, LocalDate fecha);
        List<CuentaCobro> vencidas = cuentaCobroRepo.findByEstadoAndFechaVencimientoBefore(EstadoPago.PENDIENTE, hoy);

        for (CuentaCobro deuda : vencidas) {
            Judoka moroso = deuda.getJudokaBeneficiario();

            // Solo suspendemos si está ACTIVO (para no procesar repetidos)
            if (moroso.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka.ACTIVO) {
                moroso.setEstado(EstadoJudoka.INACTIVO);
                judokaRepo.save(moroso);

                // Aquí podrías disparar un email: "Tu servicio ha sido suspendido por mora."
                System.out.println("SUSPENDIDO Judoka: " + moroso.getNombreAcudiente() + " por deuda: " + deuda.getConcepto());
            }
        }
    }
    // =========================================================================
    // 5. MOTOR DE ONBOARDING (APROBACIÓN DE INGRESOS)
    // =========================================================================

    /**
     * Este método conecta la aprobación del Sensei/Master con el flujo financiero real.
     * Genera la deuda inicial y la liquida automáticamente con el comprobante subido.
     */
    @Transactional
    public void procesarPagoOnboarding(Judoka judoka, String urlComprobante) {
        // 1. Generamos el cobro de bienvenida (Matrícula + Mensualidad)
        generarCobroBienvenida(judoka);

        // 2. Buscamos esos cobros (que acaban de nacer en estado PENDIENTE)
        Usuario responsable = (judoka.getMecenas() != null) ? judoka.getMecenas().getUsuario() : judoka.getAcudiente();

        List<CuentaCobro> pendientes = obtenerDeudasPendientes(responsable).stream()
                .filter(c -> c.getJudokaBeneficiario().getId().equals(judoka.getId()))
                .collect(Collectors.toList());

        // 3. Los marcamos como pagados automáticamente.
        // ¡Esto dispara el ingreso a Caja, las Comisiones y activa la Suscripción!
        for (CuentaCobro cc : pendientes) {
            pagarCuentaCobro(cc.getId(), MetodoPago.NEQUI, urlComprobante);
        }

        // 4. Actualizamos bandera vital
        judoka.setMatriculaPagada(true);
        judokaRepo.save(judoka);
    }
    /**
     * El Acudiente/Judoka reporta que hizo el pago mensual subiendo su soporte.
     * NO entra a caja todavía. Queda en espera de la auditoría del Master.
     */
    /**
     * El Acudiente reporta el pago. Se crea el objeto Pago con la URL,
     * pero queda en estado PENDIENTE esperando la auditoría del Master.
     */
    @Transactional
    public void reportarPagoParaRevision(Long cuentaCobroId, MetodoPago metodoPago, String urlSoporte) {
        CuentaCobro cuenta = cuentaCobroRepo.findById(cuentaCobroId)
                .orElseThrow(() -> new RuntimeException("Cuenta de cobro no encontrada"));

        // 1. Creamos el objeto Pago formal
        Pago intentoPago = new Pago();
        intentoPago.setUsuario(cuenta.getResponsablePago());
        intentoPago.setCuentaCobro(cuenta);

        // Asignamos el producto (Asumiendo que la cuenta de cobro tiene el producto o puedes buscar un producto genérico de "Mensualidad")

        intentoPago.setMonto(cuenta.getValorTotal().doubleValue());
        intentoPago.setFechaCreacion(LocalDateTime.now());
        intentoPago.setMetodoPago(metodoPago);
        intentoPago.setUrlComprobante(urlSoporte);
        intentoPago.setEstado(EstadoPago.PENDIENTE); // El Master lo cambiará a EXITOSO

        pagoRepo.save(intentoPago);

        cuenta.setEstado(EstadoPago.EN_REVISION);
        cuentaCobroRepo.save(cuenta);
    }

    private Usuario obtenerResponsableFinanciero(Judoka judoka) {
        if (judoka.getMecenas() != null) {
            return judoka.getMecenas().getUsuario();
        }
        return judoka.getAcudiente();
    }
    public List<SenseiLiquidacionDTO> obtenerSenseisConSaldo() {
        List<Sensei> senseis = senseiRepo.findBySaldoWalletGreaterThan(BigDecimal.ZERO);
        return senseis.stream()
                .map(s -> new SenseiLiquidacionDTO(
                        s.getId(),
                        s.getUsuario().getNombre() + " " + s.getUsuario().getApellido(),
                        s.getSaldoWallet()
                ))
                .collect(Collectors.toList());
    }

    // DTO simple (puede ser un record)
    public record SenseiLiquidacionDTO(Long id, String nombreCompleto, BigDecimal saldo) {}
    public String formatearMoneda(BigDecimal valor) {
        if (valor == null) return configService.obtenerFormatoMoneda().format(BigDecimal.ZERO);
        return configService.obtenerFormatoMoneda().format(valor);
    }
}