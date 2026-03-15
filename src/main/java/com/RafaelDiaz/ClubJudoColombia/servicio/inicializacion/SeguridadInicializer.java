package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SeguridadInicializer {

    private final RolRepository rolRepo;
    private final UsuarioRepository usuarioRepo;
    private final SenseiRepository senseiRepo;
    private final PasswordEncoder passwordEncoder;

    public SeguridadInicializer(RolRepository rolRepo,
                                UsuarioRepository usuarioRepo,
                                SenseiRepository senseiRepo,
                                PasswordEncoder passwordEncoder) {
        this.rolRepo = rolRepo;
        this.usuarioRepo = usuarioRepo;
        this.senseiRepo = senseiRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Sensei inicializar() {
        crearRolSiNoExiste("ROLE_MASTER");
        crearRolSiNoExiste("ROLE_ADMIN");
        crearRolSiNoExiste("ROLE_SENSEI");
        crearRolSiNoExiste("ROLE_JUDOKA");
        crearRolSiNoExiste("ROLE_COMPETIDOR");
        crearRolSiNoExiste("ROLE_ACUDIENTE");

        return configurarUsuarioMaster();
    }

    private void crearRolSiNoExiste(String nombre) {
        if (rolRepo.findByNombre(nombre).isEmpty()) {
            Rol r = new Rol();
            r.setNombre(nombre);
            rolRepo.save(r);
        }
    }

    private Sensei configurarUsuarioMaster() {
        String masterUsername = "master_admin";
        if (usuarioRepo.findByUsername(masterUsername).isPresent()) {
            return senseiRepo.findByUsuario_Username(masterUsername).orElseThrow();
        }

        Rol rolMaster = rolRepo.findByNombre("ROLE_MASTER").orElseThrow();
        Rol rolSensei = rolRepo.findByNombre("ROLE_SENSEI").orElseThrow();

        Usuario masterUser = new Usuario(masterUsername, passwordEncoder.encode("contraseña"),
                "Rafael", "Díaz");
        masterUser.setEmail("rafael.antonio.diaz@gmail.com");
        masterUser.setActivo(true);
        masterUser.setRoles(Set.of(rolMaster, rolSensei));
        masterUser = usuarioRepo.save(masterUser);

        Sensei masterSensei = new Sensei();
        masterSensei.setUsuario(masterUser);
        masterSensei.setGrado(GradoCinturon.NEGRO_4_DAN);
        masterSensei.setAnosPractica(25);
        masterSensei.setBiografia("Director de la Plataforma SaaS.");
        return senseiRepo.save(masterSensei);
    }
}