package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.OperadorComparacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoEventoGamificacion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GamificationService {

    private final JudokaInsigniaRepository logroRepo;
    private final InsigniaRepository insigniaRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final ReglaGamificacionRepository reglaRepo;

    public GamificationService(JudokaInsigniaRepository logroRepo,
                               InsigniaRepository insigniaRepo,
                               AsistenciaRepository asistenciaRepo,
                               ReglaGamificacionRepository reglaRepo) {
        this.logroRepo = logroRepo;
        this.insigniaRepo = insigniaRepo;
        this.asistenciaRepo = asistenciaRepo;
        this.reglaRepo = reglaRepo;
    }

    @Transactional
    public void verificarLogrosFisicos(Judoka judoka, ResultadoPrueba resultado) {
        Sensei sensei = judoka.getSensei();
        if (sensei == null) return;

        List<ReglaGamificacion> reglas = reglaRepo.findBySenseiAndTipoEventoWithInsignia(sensei, TipoEventoGamificacion.RESULTADO_PRUEBA);

        for (ReglaGamificacion regla : reglas) {
            if (!regla.getMetrica().equals(resultado.getMetrica())) continue;

            BigDecimal valorObtenido = BigDecimal.valueOf(resultado.getValor());
            if (evaluarComparacion(valorObtenido, regla.getOperador(), regla.getValorObjetivo())) {
                desbloquear(judoka, regla.getInsignia().getClave());
            }
        }
    }

    @Transactional
    public void verificarLogrosAsistencia(Judoka judoka) {
        Sensei sensei = judoka.getSensei();
        if (sensei == null) return;

        List<ReglaGamificacion> reglas = reglaRepo.findBySenseiAndTipoEventoWithInsignia(sensei, TipoEventoGamificacion.ASISTENCIA);

        long totalAsistencias = asistenciaRepo.countByJudokaAndEstado(judoka, EstadoAsistencia.PRESENTE);
        BigDecimal valor = BigDecimal.valueOf(totalAsistencias);

        for (ReglaGamificacion regla : reglas) {
            if (evaluarComparacion(valor, regla.getOperador(), regla.getValorObjetivo())) {
                desbloquear(judoka, regla.getInsignia().getClave());
            }
        }
    }

    @Transactional
    public void verificarLogrosGrado(Judoka judoka) {
        Sensei sensei = judoka.getSensei();
        if (sensei == null) return;

        List<ReglaGamificacion> reglas = reglaRepo.findBySenseiAndTipoEventoWithInsignia(sensei, TipoEventoGamificacion.GRADO_ALCANZADO);

        int ordenGrado = judoka.getGrado().ordinal(); // 0 = BLANCO, 1 = AMARILLO, etc.

        for (ReglaGamificacion regla : reglas) {
            if (regla.getValorObjetivo() != null) {
                if (evaluarComparacion(BigDecimal.valueOf(ordenGrado), regla.getOperador(), regla.getValorObjetivo())) {
                    desbloquear(judoka, regla.getInsignia().getClave());
                }
            }
        }
    }

    private boolean evaluarComparacion(BigDecimal valor, OperadorComparacion operador, BigDecimal objetivo) {
        if (objetivo == null) return false;
        int comparacion = valor.compareTo(objetivo);
        switch (operador) {
            case MAYOR_QUE: return comparacion > 0;
            case MENOR_QUE: return comparacion < 0;
            case IGUAL_A: return comparacion == 0;
            case MAYOR_O_IGUAL: return comparacion >= 0;
            case MENOR_O_IGUAL: return comparacion <= 0;
            default: return false;
        }
    }

    private void desbloquear(Judoka judoka, String claveInsignia) {
        if (!logroRepo.existsByJudokaAndInsignia_Clave(judoka, claveInsignia)) {
            insigniaRepo.findByClave(claveInsignia).ifPresent(insignia -> {
                JudokaInsignia logro = new JudokaInsignia();
                logro.setJudoka(judoka);
                logro.setInsignia(insignia);
                logroRepo.save(logro);
                System.out.println(">>> ¡GAMIFICATION! " + judoka.getNombre() + " ganó " + claveInsignia);
            });
        }
    }
}