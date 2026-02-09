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
    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final ConfiguracionService configService;
    private static final String CONCEPTO_OTROS = "OTROS";
    public FinanzasService(JudokaRepository judokaRepo,
                           SenseiRepository senseiRepo,
                           MovimientoCajaRepository movimientoRepo,
                           CuentaCobroRepository cuentaCobroRepo,
                           ConceptoFinancieroRepository conceptoRepo,
                           PagoRepository pagoRepo,
                           SecurityService securityService,
                           TraduccionService traduccionService,
                           ConfiguracionService configService) {
        this.judokaRepo = judokaRepo;
        this.senseiRepo = senseiRepo;
        this.movimientoRepo = movimientoRepo;
        this.cuentaCobroRepo = cuentaCobroRepo;
        this.conceptoRepo = conceptoRepo;
        this.pagoRepo = pagoRepo;
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
        return movimientoRepo.findByFechaBetweenOrderByFechaDesc(inicio, fin);
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
     * CRON JOB: Generación masiva de deudas el día 1 de cada mes.
     */
    @Transactional
    public void generarCobrosMensualesMasivos() {
        ConfiguracionSistema config = configService.obtenerConfiguracion();
        LocalDate hoy = LocalDate.now();
        String periodo = hoy.getMonth().name() + " " + hoy.getYear();

        List<Judoka> activos = judokaRepo.findByEstado(EstadoJudoka.ACTIVO);

        for (Judoka judoka : activos) {
            // A. Definir Responsable
            Usuario responsable = (judoka.getMecenas() != null) ?
                    judoka.getMecenas().getUsuario() : judoka.getAcudiente();

            if (responsable == null) continue; // Safety check

            // B. Definir Tarifas (Lógica SaaS vs Club)
            // TODO: Mejorar la detección de "Es mi alumno" vs "Es de un Sensei externo"
            // Por ahora usamos la lógica: Si el Sensei es el del usuario ADMIN, es alumno propio.
            boolean esAlumnoPropio = judoka.getSensei().getUsuario().getUsername().equals("admin");

            BigDecimal monto;
            String conceptoKey;
            BigDecimal comisionSensei = BigDecimal.ZERO;

            if (esAlumnoPropio) {
                monto = config.getFIN_SENSEI_MASTER_MENSUALIDAD(); // $50.000
                conceptoKey = CONCEPTO_MENSUALIDAD_CLUB;
            } else {
                monto = config.getFIN_SAAS_CANON_FIJO(); // $15.000
                conceptoKey = CONCEPTO_MENSUALIDAD_SAAS;
                // El Sensei externo gana una comisión (Ej. $5.000) que se descuenta al pagar
                // Nota: Aquí podrías usar una configuración global o específica del Sensei
                comisionSensei = new BigDecimal("5000");
            }

            // C. Crear la Deuda
            // Evitamos duplicados
            if (!existeCobro(judoka, periodo)) {
                crearCuentaCobro(judoka, responsable, monto, comisionSensei,
                        traduccionService.get(conceptoKey, periodo),
                        config.getFIN_DIA_VENCIMIENTO().intValue());
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
                cuenta.getConcepto(), // Usamos el concepto de la cuenta como nombre
                cuenta.getJudokaBeneficiario(),
                "Cobro Cuenta #" + cuenta.getId(),
                urlComprobante
        );

        // 4. Dispersión de Fondos (Revenue Share)
        if (cuenta.getValorComisionSensei() != null && cuenta.getValorComisionSensei().compareTo(BigDecimal.ZERO) > 0) {
            Sensei sensei = cuenta.getJudokaBeneficiario().getSensei();
            if (sensei != null) {
                sensei.abonarComision(cuenta.getValorComisionSensei());
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

    private boolean existeCobro(Judoka j, String periodo) {
        // Lógica simplificada: Buscar si existe cobro con el mismo concepto (mes/año) generado este mes
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        return cuentaCobroRepo.existsByJudokaBeneficiarioAndConceptoLikeAndFechaGeneracionAfter(j, "%" + periodo + "%", inicio);
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
        return cuentaCobroRepo.findByResponsablePagoAndEstado(usuario, EstadoPago.PENDIENTE);
    }

    public BigDecimal calcularDeudaTotal(Usuario usuario) {
        return obtenerDeudasPendientes(usuario).stream()
                .map(CuentaCobro::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // ... dentro de FinanzasService ...

    /**
     * MÉTODO DE ADMISIÓN (Mesa de Control):
     * Genera la primera factura según si es alumno del Master o de un Sensei externo.
     */
    @Transactional
    public void generarCobroBienvenida(Judoka judoka) {
        ConfiguracionSistema config = configService.obtenerConfiguracion();
        Usuario responsable = (judoka.getMecenas() != null) ?
                judoka.getMecenas().getUsuario() : judoka.getAcudiente();

        if (responsable == null) {
            throw new RuntimeException("Error: El judoka no tiene responsable financiero (Acudiente/Mecenas).");
        }

        // 1. DETECTAR EL TIPO DE ALUMNO (Regla: master_admin)
        // Verificamos si el Sensei del alumno es el dueño del sistema
        boolean esAlumnoPropio = judoka.getSensei().getUsuario().getUsername().equals("master_admin");

        if (esAlumnoPropio) {
            // --- CASO A: ALUMNO PROPIO (Club Judo Colombia) ---
            // Debe pagar: Matrícula + Mensualidad Club

            // 1. Matrícula Anual
            if (config.getFIN_MATRICULA_ANUAL().compareTo(BigDecimal.ZERO) > 0) {
                crearCuentaCobro(judoka, responsable,
                        config.getFIN_MATRICULA_ANUAL(),
                        BigDecimal.ZERO, // Sin comisión
                        traduccionService.get("finanzas.concepto.matricula", LocalDate.now().getYear()),
                        5); // Vence en 5 días
            }

            // 2. Primera Mensualidad
            crearCuentaCobro(judoka, responsable,
                    config.getFIN_SENSEI_MASTER_MENSUALIDAD(),
                    BigDecimal.ZERO,
                    traduccionService.get("finanzas.concepto.mensualidad_club", "Mes 1"),
                    5);

        } else {
            // --- CASO B: ALUMNO EXTERNO (SaaS) ---
            // Solo paga: Uso de Plataforma

            crearCuentaCobro(judoka, responsable,
                    config.getFIN_SAAS_CANON_FIJO(),
                    BigDecimal.ZERO, // O la comisión definida
                    traduccionService.get("finanzas.concepto.inscripcion_saas"),
                    3); // Vence en 3 días
        }

        // 3. CAMBIO DE ESTADO
        // No activamos al usuario todavía.
        // Lo dejamos en PENDIENTE (o ESPERANDO_PAGO si tienes el enum) pero con deuda generada.
        // El sistema de Login o el Dashboard le mostrará "Tienes pagos pendientes" y no lo dejará entrar al Dojo.
        // Nota: Si mantienes el estado PENDIENTE, asegúrate de que tu lógica de login revise deudas.
        // Si usas un estado nuevo, actualízalo aquí:
        // judoka.setEstado(EstadoJudoka.ESPERANDO_PAGO);
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
}