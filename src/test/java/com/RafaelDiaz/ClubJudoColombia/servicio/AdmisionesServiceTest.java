package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.DocumentoRequisito;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.DocumentoRequisitoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
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

    @InjectMocks
    private AdmisionesService admisionesService;

    @Test
    @DisplayName("Debe fallar si intenta activar sin Waiver")
    void activar_SinWaiver_LanzaExcepcion() {
        // 1. PREPARAR: Judoka pagó pero NO tiene waiver
        Judoka novato = new Judoka();
        novato.setMatriculaPagada(true);
        novato.setEstado(EstadoJudoka.PENDIENTE);

        // 2. & 3. ACTUAR Y VERIFICAR
        Exception ex = assertThrows(RuntimeException.class, () -> {
            admisionesService.activarJudoka(novato);
        });

        assertTrue(ex.getMessage().contains("Falta cargar el Waiver"));
        assertEquals(EstadoJudoka.PENDIENTE, novato.getEstado()); // No debió cambiar
    }

    @Test
    @DisplayName("Debe activar correctamente si cumple todo")
    void activar_TodoCumplido_Exito() {
        // 1. PREPARAR
        Usuario usuarioMock = new Usuario();

        Judoka aspirante = new Judoka();
        aspirante.setAcudiente(usuarioMock);
        aspirante.setMatriculaPagada(true);
        // Le agregamos el Waiver
        aspirante.getDocumentos().add(new DocumentoRequisito(aspirante, TipoDocumento.WAIVER, "path/file.pdf"));

        // Mock del Rol
        Rol rolJudoka = new Rol("ROLE_JUDOKA");
        when(rolRepository.findByNombre("ROLE_JUDOKA")).thenReturn(Optional.of(rolJudoka));

        // 2. ACTUAR
        admisionesService.activarJudoka(aspirante);

        // 3. VERIFICAR
        assertEquals(EstadoJudoka.ACTIVO, aspirante.getEstado(), "El estado debe cambiar a ACTIVO");
        assertTrue(usuarioMock.isActivo(), "El usuario debe quedar habilitado para login");
        verify(judokaRepository).save(aspirante);
    }
}