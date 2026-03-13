package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class SenseiService {

    private final SenseiRepository senseiRepository;
    private final AlmacenamientoCloudService almacenamientoCloudService;

    public SenseiService(SenseiRepository senseiRepository,
                         AlmacenamientoCloudService almacenamientoCloudService) {
        this.senseiRepository = senseiRepository;
        this.almacenamientoCloudService = almacenamientoCloudService;
    }

    @Transactional
    public void actualizarFotoPerfil(Sensei sensei, InputStream inputStream, String filename) {
        try {
            String nombreFinal = almacenamientoCloudService.subirArchivo(sensei.getId(), filename, inputStream);
            String urlCompleta = almacenamientoCloudService.obtenerUrl(sensei.getId(), nombreFinal);
            sensei.setUrlFotoPerfil(urlCompleta);
            senseiRepository.save(sensei);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar foto del sensei: " + e.getMessage(), e);
        }
    }
}