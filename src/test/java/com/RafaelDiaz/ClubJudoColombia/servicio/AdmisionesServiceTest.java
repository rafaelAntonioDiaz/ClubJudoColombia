package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmisionesServiceTest {

    @Mock private JudokaRepository judokaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DocumentoRequisitoRepository documentoRepository;
    @Mock private RolRepository rolRepository;
    @Mock private TraduccionService traduccionService;
    @Mock private SenseiService senseiService;

    @InjectMocks
    private AdmisionesService admisionesService;

    @Test
    @DisplayName("Debe fallar si intenta activar sin Waiver")
    void activar_SinWaiver_LanzaExcepcion() {
        Judoka novato = new Judoka();
        novato.setMatriculaPagada(true);
        novato.setEstado(EstadoJudoka.PENDIENTE);

        when(traduccionService.get("error.admisiones.falta_waiver"))
                .thenReturn("Falta cargar el Waiver");

        Exception ex = assertThrows(RuntimeException.class, () -> {
            admisionesService.activarJudoka(novato);
        });

        assertTrue(ex.getMessage().contains("Waiver"));
        assertEquals(EstadoJudoka.PENDIENTE, novato.getEstado());
    }

    @Test
    @DisplayName("Debe activar correctamente si cumple todo")
    void activar_TodoCumplido_Exito() {
        // 1. Preparar usuario del judoka (adulto)
        Usuario usuarioJudoka = new Usuario();
        usuarioJudoka.setUsername("test@test.com");
        usuarioJudoka.setActivo(false);

        // 2. Crear sensei de prueba (mock, no persistido)
        Usuario usuarioSensei = new Usuario("sensei@test.com", "pass", "Sensei", "Test");
        usuarioSensei.setActivo(true);
        Sensei senseiTest = new Sensei();
        senseiTest.setUsuario(usuarioSensei);
        senseiTest.setNombreClub("Club Test");

        // 3. Judoka adulto (tiene su propio usuario)
        Judoka aspirante = new Judoka();
        aspirante.setSensei(senseiTest);
        aspirante.setAcudiente(usuarioJudoka);
        aspirante.setMatriculaPagada(true);
        aspirante.getDocumentos().add(new DocumentoRequisito(aspirante, TipoDocumento.WAIVER, "path/file.pdf"));

        // 4. Mockear repositorios
        Rol rolJudoka = new Rol("ROLE_JUDOKA");
        when(rolRepository.findByNombre("ROLE_JUDOKA")).thenReturn(Optional.of(rolJudoka));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArguments()[0]);

        // 5. Actuar
        admisionesService.activarJudoka(aspirante);

        // 6. Verificar
        assertEquals(EstadoJudoka.ACTIVO, aspirante.getEstado());
        assertTrue(usuarioJudoka.isActivo());
        // Verificar que se asignó el rol
        assertTrue(usuarioJudoka.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_JUDOKA")));
        verify(judokaRepository).save(aspirante);
        verify(usuarioRepository).save(usuarioJudoka);
    }
}