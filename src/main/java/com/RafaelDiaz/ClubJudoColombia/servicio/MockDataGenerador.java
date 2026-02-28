package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Configuration
public class MockDataGenerador {

    @Bean
    @Order(2)
    public CommandLineRunner inyectarEscenarioFamiliaJaimes(
            UsuarioRepository usuarioRepository,
            SenseiRepository senseiRepository,
            JudokaRepository judokaRepository,
            GrupoEntrenamientoRepository grupoRepository,
            MacrocicloRepository macrocicloRepository,
            MicrocicloRepository microcicloRepository,
            TareaDiariaRepository tareaRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            System.err.println("\n\n=======================================================");
            System.err.println("🚨 MOCK DATA V8: VALIDACIÓN DE MICROCICLO 🚨");
            System.err.println("=======================================================\n");

            String miUsuarioLogin = "master_admin";

            // 1. BUSCAR O CREAR AL SENSEI MASTER
            Sensei master = senseiRepository.findAll().stream()
                    .filter(s -> s.getUsuario().getUsername().equals(miUsuarioLogin))
                    .findFirst()
                    .orElse(null);

            if (master == null) {
                Rol rolMaster = rolRepository.findAll().stream()
                        .filter(r -> "ROLE_MASTER".equals(r.getNombre()))
                        .findFirst()
                        .orElseGet(() -> rolRepository.save(new Rol("ROLE_MASTER")));

                Rol rolSensei = rolRepository.findAll().stream()
                        .filter(r -> "ROLE_SENSEI".equals(r.getNombre()))
                        .findFirst()
                        .orElseGet(() -> rolRepository.save(new Rol("ROLE_SENSEI")));

                Usuario adminUser = new Usuario();
                adminUser.setUsername(miUsuarioLogin);
                adminUser.setNombre("Rafael");
                adminUser.setApellido("Master");
                adminUser.setEmail("master@clubjudo.com");
                adminUser.setPasswordHash(passwordEncoder.encode("1234"));
                adminUser.setRoles(Set.of(rolMaster, rolSensei));
                adminUser = usuarioRepository.save(adminUser);

                master = new Sensei();
                master.setUsuario(adminUser);
                master.setNombreDojo("Dojo Central Colombia");
                master.setGrado(GradoCinturon.BLANCO);
                master = senseiRepository.save(master);
            }

            // 2. EVITAR DUPLICADOS V8
            if (usuarioRepository.findByUsername("juliana_v8@test.com").isPresent()) {
                System.err.println("✅ El escenario V8 ya está montado.");
                return;
            }

            // 3. CREAR ROL ACUDIENTE
            Rol rolAcudiente = rolRepository.findAll().stream()
                    .filter(r -> "ROLE_ACUDIENTE".equals(r.getNombre()))
                    .findFirst()
                    .orElseGet(() -> rolRepository.save(new Rol("ROLE_ACUDIENTE")));

            // 4. CREAR A JULIANA
            Usuario juliana = new Usuario();
            juliana.setUsername("juliana_v8@test.com");
            juliana.setNombre("Juliana");
            juliana.setApellido("Jaimes");
            juliana.setEmail("juliana_v8@test.com");
            juliana.setPasswordHash(passwordEncoder.encode("1234"));
            juliana.setRoles(Set.of(rolAcudiente));
            usuarioRepository.save(juliana);

            // 5. CREAR GRUPO
            GrupoEntrenamiento grupo = new GrupoEntrenamiento();
            grupo.setNombre("Jóvenes Girón - V8");
            grupo.setSensei(master);
            grupoRepository.save(grupo);

            // 6. CREAR JUDOKAS
            String[] nombres = {"Thaliana", "Nahomy", "Marian", "Johan"};
            for (String n : nombres) {
                Judoka j = new Judoka();
                j.setNombre(n);
                j.setApellido("Jaimes");
                j.setAcudiente(juliana);
                j.setSensei(master);
                j.setFechaNacimiento(LocalDate.now().minusYears(15));
                j.setSexo(n.equals("Johan") ? Sexo.MASCULINO : Sexo.FEMENINO);
                j.setGrado(GradoCinturon.BLANCO);
                j.setEstado(EstadoJudoka.ACTIVO);
                j.setMatriculaPagada(true);
                j.setSuscripcionActiva(true);
                j.setFechaVencimientoSuscripcion(LocalDate.now().plusMonths(1));
                judokaRepository.save(j);
            }

            // 7. MACROCICLO
            Macrociclo macro = new Macrociclo();
            macro.setNombre("Adquisición Inicial");
            macro.setObjetivoPrincipal("Adaptación base");
            macro.setSensei(master);
            macro.setFechaInicio(LocalDate.now());
            macro.setFechaFin(LocalDate.now().plusMonths(4));
            macrocicloRepository.save(macro);

            // 8. TAREAS (Usando CategoríaEjercicio)
            TareaDiaria t1 = new TareaDiaria();
            t1.setNombre("Movilidad Articular");
            t1.setSenseiCreador(master);
            t1.setCategoria(CategoriaEjercicio.AGILIDAD);

            TareaDiaria t2 = new TareaDiaria();
            t2.setNombre("Uchikomi");
            t2.setSenseiCreador(master);
            t2.setCategoria(CategoriaEjercicio.TECNICA);

            tareaRepository.saveAll(List.of(t1, t2));

            // 9. MICROCICLO (CORREGIDO CON CAMPOS OBLIGATORIOS SEGÚN EL LOG)
            Microciclo micro = new Microciclo();
            micro.setNombre("Semana de Prueba V8");
            micro.setMacrociclo(macro);
            micro.setSensei(master);
            micro.setFechaInicio(LocalDate.now());
            micro.setFechaFin(LocalDate.now().plusDays(7));

            // AJUSTE: Estos campos aparecen en tu consulta INSERT como obligatorios
            // Verifica si son Enums o Strings en tu entidad Microciclo.java
            micro.setEstado(EstadoMicrociclo.valueOf("ACTIVO"));
            micro.setTipoMicrociclo(TipoMicrociclo.valueOf("AJUSTE"));
            micro.setMesocicloATC(MesocicloATC.valueOf("ADQUISICION"));

            micro.getGruposAsignados().add(grupo);

            // 10. EJERCICIOS PLANIFICADOS
            EjercicioPlanificado ep1 = new EjercicioPlanificado();
            ep1.setTareaDiaria(t1);
            ep1.setDuracionMinutos(15);
            ep1.setNotaAjuste("Fase inicial");
            micro.addEjercicio(ep1);

            EjercicioPlanificado ep2 = new EjercicioPlanificado();
            ep2.setTareaDiaria(t2);
            ep2.setDuracionMinutos(45);
            ep2.setNotaAjuste("Fase técnica");
            micro.addEjercicio(ep2);

            microcicloRepository.save(micro);

            System.err.println("\n=======================================================");
            System.err.println("✅ ¡MOCK DATA V8 INYECTADO CON ÉXITO!");
            System.err.println("=======================================================\n\n");
        };
    }
}