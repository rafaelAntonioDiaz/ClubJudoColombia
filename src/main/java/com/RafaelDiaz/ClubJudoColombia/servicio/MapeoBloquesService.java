package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.BloqueAgudelo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class MapeoBloquesService {

    private final Map<CategoriaEjercicio, BloqueAgudelo> mapeo = new EnumMap<>(CategoriaEjercicio.class);

    public MapeoBloquesService() {
        // Definitorio
        mapeo.put(CategoriaEjercicio.POTENCIA, BloqueAgudelo.DEFINITORIO);
        mapeo.put(CategoriaEjercicio.VELOCIDAD, BloqueAgudelo.DEFINITORIO);

        // Sustento
        mapeo.put(CategoriaEjercicio.RESISTENCIA_DINAMICA, BloqueAgudelo.SUSTENTO);
        mapeo.put(CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA, BloqueAgudelo.SUSTENTO);
        mapeo.put(CategoriaEjercicio.RESISTENCIA_ISOMETRICA, BloqueAgudelo.SUSTENTO);

        // Eficiencia
        mapeo.put(CategoriaEjercicio.APTITUD_ANAEROBICA, BloqueAgudelo.EFICIENCIA);
        mapeo.put(CategoriaEjercicio.APTITUD_AEROBICA, BloqueAgudelo.EFICIENCIA);

        // Protección
        mapeo.put(CategoriaEjercicio.FLEXIBILIDAD, BloqueAgudelo.PROTECCION);

        // Técnico-Coordinativo
        mapeo.put(CategoriaEjercicio.AGILIDAD, BloqueAgudelo.TECNICO_COORDINATIVO);
        mapeo.put(CategoriaEjercicio.TECNICA, BloqueAgudelo.TECNICO_COORDINATIVO);
        mapeo.put(CategoriaEjercicio.ANTICIPACION, BloqueAgudelo.TECNICO_COORDINATIVO);
    }

    public BloqueAgudelo getBloque(CategoriaEjercicio categoria) {
        return mapeo.get(categoria);
    }

    public boolean tieneBloque(CategoriaEjercicio categoria) {
        return mapeo.containsKey(categoria);
    }
}