package com.RafaelDiaz.ClubJudoColombia.servicio;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class TareasProgramadasService {

    private final FinanzasService finanzasService;

    public TareasProgramadasService(FinanzasService finanzasService) {
        this.finanzasService = finanzasService;
    }

    /**
     * Ciclo de Facturación SaaS: Se ejecuta el día 1 de cada mes a las 00:05.
     */
    @Scheduled(cron = "0 5 0 1 * ?")
    @Transactional
    public void ejecutarFacturacionMensual() {
        System.out.println(">>> [CRON] Iniciando generación de cobros masivos SaaS...");
        finanzasService.generarCobrosMensualesMasivos();
        System.out.println(">>> [CRON] Cobros generados con éxito.");
    }

    /**
     * Revisa suspensiones por mora: Se ejecuta cada medianoche.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void ejecutarRevisionDeMora() {
        finanzasService.verificarImpagosYSuspender();
    }
}
