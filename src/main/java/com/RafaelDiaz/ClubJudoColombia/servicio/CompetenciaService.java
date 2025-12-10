package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ResultadoCompetencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ParticipacionCompetenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class CompetenciaService {

    private final ParticipacionCompetenciaRepository competenciaRepo;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TraduccionService traduccionService; // <--- INYECCIÓN

    public CompetenciaService(ParticipacionCompetenciaRepository competenciaRepo,
                              UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              TraduccionService traduccionService) {
        this.competenciaRepo = competenciaRepo;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.traduccionService = traduccionService;
    }

    @Transactional(readOnly = true)
    public List<ParticipacionCompetencia> findAll() {
        return competenciaRepo.findAllWithDetails();
    }

    public void inscribirJudoka(Judoka judoka, String nombreEvento, String lugar, LocalDate fecha, NivelCompetencia nivel) {
        ParticipacionCompetencia participacion = new ParticipacionCompetencia(
                judoka, nombreEvento, lugar, fecha, nivel, ResultadoCompetencia.PARTICIPACION, null
        );
        competenciaRepo.save(participacion);

        // Ascenso automático a Competidor
        Usuario usuario = judoka.getUsuario();
        rolRepository.findByNombre("ROLE_COMPETIDOR").ifPresent(rolCompetidor -> {
            Set<Rol> roles = new HashSet<>(usuario.getRoles());
            if (!roles.contains(rolCompetidor)) {
                roles.add(rolCompetidor);
                usuario.setRoles(roles);
                usuarioRepository.save(usuario);
            }
        });
    }

    public void registrarResultado(ParticipacionCompetencia participacion, ResultadoCompetencia resultado, String videoUrl) {
        participacion.setResultado(resultado);
        participacion.setUrlVideo(videoUrl);
        competenciaRepo.save(participacion);
    }

    public void eliminarParticipacion(ParticipacionCompetencia p) {
        competenciaRepo.delete(p);
    }
}