package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ResultadoCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class CompetenciaService {

    private final ParticipacionCompetenciaRepository competenciaRepo;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TraduccionService traduccionService;
    private final CompetenciaRepository competenciaRepository;
    private final DocumentoRequisitoRepository documentoRepo;
    public CompetenciaService(ParticipacionCompetenciaRepository competenciaRepo,
                              UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              TraduccionService traduccionService,
                              CompetenciaRepository competenciaRepository,
                              DocumentoRequisitoRepository documentoRepo) {
        this.competenciaRepo = competenciaRepo;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.traduccionService = traduccionService;
        this.competenciaRepository = competenciaRepository;
        this.documentoRepo = documentoRepo;
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
    public Competencia guardarCompetenciaDefinicion(Competencia c) {
        return competenciaRepository.save(c);
    }

    public List<Competencia> obtenerTorneosDisponibles() {
        return competenciaRepository.findAll();
    }

    // Inscribir usando la Entidad Competencia (Reemplaza o sobrecarga el anterior)
    public void inscribirJudokaEnCompetencia(Judoka judoka, Competencia competencia) {
        ParticipacionCompetencia participacion = new ParticipacionCompetencia(judoka, competencia);
        competenciaRepo.save(participacion);

        // (Aquí puedes mantener la lógica de rol COMPETIDOR si deseas)
    }

    // --- GESTIÓN DE DOCUMENTOS (LO QUE PEDISTE) ---

    public Map<String, String> obtenerEstadoDocumentos(Judoka judoka, Competencia competencia) {
        Map<String, String> estado = new HashMap<>();
        if (competencia.getDocumentosRequeridos() != null) {
            for (String req : competencia.getDocumentosRequeridos()) {
                String clave = competencia.getNombre() + " - " + req;
                // Asumiendo que DocumentoRequisitoRepository tiene findByEventoAsociadoAndJudoka
                // Si no, usar findByJudoka y filtrar en memoria
                Optional<DocumentoRequisito> doc = documentoRepo.findByEventoAsociadoAndJudoka(clave, judoka);
                estado.put(req, doc.map(DocumentoRequisito::getUrlArchivo).orElse(null));
            }
        }
        return estado;
    }

    @Transactional
    public void recibirDocumentoTorneo(Judoka judoka, Competencia competencia, String nombreRequisito, String urlArchivo) {
        String claveEvento = competencia.getNombre() + " - " + nombreRequisito;

        DocumentoRequisito doc = documentoRepo.findByEventoAsociadoAndJudoka(claveEvento, judoka)
                .orElse(new DocumentoRequisito());

        if (doc.getId() == null) { // Es nuevo
            doc.setJudoka(judoka);
            doc.setTipo(TipoDocumento.OTRO);
            doc.setEventoAsociado(claveEvento);
        }

        doc.setUrlArchivo(urlArchivo);
        doc.setValidadoPorSensei(false);
        documentoRepo.save(doc);
    }
    // --- AGREGAR ESTE MÉTODO EN CompetenciaService ---

    public List<Competencia> obtenerInscripcionesDeJudoka(Judoka judoka) {
        // Usamos el repositorio de participaciones para buscar el historial
        return competenciaRepo.findByJudokaOrderByFechaDesc(judoka)
                .stream()
                .map(ParticipacionCompetencia::getCompetencia)
                .toList();
    }
}