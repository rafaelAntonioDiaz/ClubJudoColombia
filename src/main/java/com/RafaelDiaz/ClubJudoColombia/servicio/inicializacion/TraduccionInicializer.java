package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TraduccionInicializer {

    private final TraduccionRepository traduccionRepo;

    public TraduccionInicializer(TraduccionRepository traduccionRepo) {
        this.traduccionRepo = traduccionRepo;
    }

    @Transactional
    public void inicializar() {
        // Si ya hay traducciones, no hacemos nada (opcional, puedes cambiarlo si quieres añadir nuevas)
        if (traduccionRepo.count() > 0) {
            System.out.println(">>> Traducciones ya existen. Omitiendo inicialización.");
            return;
        }

        System.out.println(">>> Cargando traducciones del sistema (ES, EN, PT)...");

        List<Traduccion> todas = new ArrayList<>();
        todas.addAll(traduccionesComunes());
        todas.addAll(traduccionesDashboard());
        todas.addAll(traduccionesPerfil());
        todas.addAll(traduccionesResultados());
        todas.addAll(traduccionesAsistencia());
        todas.addAll(traduccionesGamificacion());
        todas.addAll(traduccionesCheckIn());
        todas.addAll(traduccionesGrupos());
        todas.addAll(traduccionesCampeonatos());
        todas.addAll(traduccionesCampos());
        todas.addAll(traduccionesInventario());
        todas.addAll(traduccionesTesoreria());
        todas.addAll(traduccionesAdmisiones());
        todas.addAll(traduccionesRegistro());
        todas.addAll(traduccionesBiblioteca());
        todas.addAll(traduccionesComunidad());
        todas.addAll(traduccionesAyuda());

        traduccionRepo.saveAll(todas);
        System.out.println(">>> Traducciones cargadas exitosamente: " + todas.size());
    }

    // =========================================================================
    // Métodos auxiliares para crear traducciones
    // =========================================================================
    private List<Traduccion> crearTraducciones(String clave, String es, String en, String pt) {
        List<Traduccion> lista = new ArrayList<>();
        lista.add(new Traduccion(clave, "es", es));
        lista.add(new Traduccion(clave, "en", en));
        lista.add(new Traduccion(clave, "pt", pt));
        return lista;
    }

    private void agregarSiNoExiste(List<Traduccion> lista, String clave, String es, String en, String pt) {
        // En este método no verificamos, lo hacemos al guardar con count>0 al inicio.
        // Pero si quieres insertar solo si no existen, necesitarías un repositorio por clave.
        // Por simplicidad, asumimos que no existen.
        lista.addAll(crearTraducciones(clave, es, en, pt));
    }

    // =========================================================================
    // Secciones de traducciones
    // =========================================================================

    private List<Traduccion> traduccionesComunes() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "app.nombre", "Club de Judo Colombia", "Judo Club Colombia", "Clube de Judô Colômbia");
        agregarSiNoExiste(lista, "btn.guardar", "Guardar", "Save", "Salvar");
        agregarSiNoExiste(lista, "btn.cancelar", "Cancelar", "Cancel", "Cancelar");
        agregarSiNoExiste(lista, "btn.editar", "Editar", "Edit", "Editar");
        agregarSiNoExiste(lista, "btn.eliminar", "Eliminar", "Delete", "Excluir");
        agregarSiNoExiste(lista, "btn.crear", "Crear", "Create", "Criar");
        agregarSiNoExiste(lista, "btn.actualizar", "Actualizar", "Update", "Atualizar");
        agregarSiNoExiste(lista, "btn.guardar.cambios", "Guardar Cambios", "Save Changes", "Salvar Alterações");
        agregarSiNoExiste(lista, "btn.guardar_cambios", "Guardar Cambios", "Save Changes", "Salvar Alterações");
        agregarSiNoExiste(lista, "btn.cerrar", "Cerrar", "Close", "Fechar");
        agregarSiNoExiste(lista, "btn.cerrar.sesion", "Cerrar Sesión", "Logout", "Sair");
        agregarSiNoExiste(lista, "btn.confirmar", "Confirmar", "Confirm", "Confirmar");
        agregarSiNoExiste(lista, "btn.atras", "Atrás", "Back", "Voltar");
        agregarSiNoExiste(lista, "btn.finalizar", "Finalizar", "Finish", "Finalizar");
        agregarSiNoExiste(lista, "btn.siguiente.paso", "Siguiente", "Next", "Próximo");
        agregarSiNoExiste(lista, "btn.vender", "Vender", "Sell", "Vender");
        agregarSiNoExiste(lista, "btn.ver_pdf", "Ver PDF", "View PDF", "Ver PDF");
        agregarSiNoExiste(lista, "btn.nuevo.usuario", "Nuevo Usuario", "New User", "Novo Usuário");
        agregarSiNoExiste(lista, "btn.completado", "Completado", "Completed", "Concluído");
        agregarSiNoExiste(lista, "btn.marcar.hecho", "Marcar como Hecho", "Mark as Done", "Marcar como Feito");
        agregarSiNoExiste(lista, "btn.registrar.pensamiento", "Registrar Pensamiento", "Log Thought", "Registrar Pensamento");
        agregarSiNoExiste(lista, "btn.activar", "Activar", "Activate", "Ativar");
        agregarSiNoExiste(lista, "btn.rechazar", "Rechazar", "Reject", "Rejeitar");
        agregarSiNoExiste(lista, "btn.agregar", "Agregar", "Add", "Adicionar");
        agregarSiNoExiste(lista, "btn.quitar", "Quitar", "Remove", "Remover");
        agregarSiNoExiste(lista, "btn.filtrar", "Filtrar", "Filter", "Filtrar");

        agregarSiNoExiste(lista, "generic.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, "generic.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste(lista, "generic.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste(lista, "generic.acciones", "Acciones", "Actions", "Ações");
        agregarSiNoExiste(lista, "generic.estado", "Estado", "Status", "Estado");
        agregarSiNoExiste(lista, "generic.cantidad", "Cantidad", "Quantity", "Quantidade");
        agregarSiNoExiste(lista, "generic.judoka", "Judoka", "Judoka", "Judoca");
        agregarSiNoExiste(lista, "generic.grupo", "Grupo", "Group", "Grupo");
        agregarSiNoExiste(lista, "generic.horario", "Horario", "Schedule", "Horário");
        agregarSiNoExiste(lista, "generic.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste(lista, "generic.inicio", "Inicio", "Start", "Início");
        agregarSiNoExiste(lista, "generic.fin", "Fin", "End", "Fim");
        agregarSiNoExiste(lista, "generic.fecha_inicio", "Fecha Inicio", "Start Date", "Data Início");
        agregarSiNoExiste(lista, "generic.fecha_fin", "Fecha Fin", "End Date", "Data Fim");
        agregarSiNoExiste(lista, "generic.pts", "pts", "pts", "pts");
        agregarSiNoExiste(lista, "generic.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste(lista, "generic.pagado", "Pagado", "Paid", "Pago");
        agregarSiNoExiste(lista, "generic.aspirante", "Aspirante", "Applicant", "Candidato");
        agregarSiNoExiste(lista, "generic.decision", "Decisión", "Decision", "Decisão");
        agregarSiNoExiste(lista, "generic.cinturon", "Cinturón", "Belt", "Cinturão");

        agregarSiNoExiste(lista, "lbl.peso", "Peso", "Weight", "Peso");
        agregarSiNoExiste(lista, "lbl.altura", "Altura", "Height", "Altura");
        agregarSiNoExiste(lista, "lbl.edad", "Edad", "Age", "Idade");
        agregarSiNoExiste(lista, "lbl.anios", "años", "years", "anos");
        agregarSiNoExiste(lista, "lbl.judokas.disponibles", "Judokas Disponibles", "Available Judokas", "Judocas Disponíveis");
        agregarSiNoExiste(lista, "lbl.judokas.grupo", "Judokas en el Grupo", "Judokas in Group", "Judocas no Grupo");
        agregarSiNoExiste(lista, "lbl.selecciona.plan", "Selecciona tu Plan", "Select Your Plan", "Selecione seu Plano");
        agregarSiNoExiste(lista, "lbl.progreso.cero", "0% Completado", "0% Completed", "0% Concluído");
        agregarSiNoExiste(lista, "lbl.progreso.dia", "Completado del Día", "Completed Today", "Concluído Hoje");
        agregarSiNoExiste(lista, "lbl.seleccionar.grupo", "Seleccionar Grupo", "Select Group", "Selecionar Grupo");

        agregarSiNoExiste(lista, "col.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste(lista, "col.apellido", "Apellido", "Last Name", "Sobrenome");
        agregarSiNoExiste(lista, "col.username", "Usuario", "Username", "Usuário");
        agregarSiNoExiste(lista, "col.activo", "Activo", "Active", "Ativo");
        agregarSiNoExiste(lista, "col.nombre.completo", "Nombre Completo", "Full Name", "Nome Completo");
        agregarSiNoExiste(lista, "col.grado", "Grado", "Rank", "Graduação");
        agregarSiNoExiste(lista, "col.sexo", "Sexo", "Gender", "Sexo");
        agregarSiNoExiste(lista, "col.edad", "Edad", "Age", "Idade");
        agregarSiNoExiste(lista, "col.accion", "Acción", "Action", "Ação");

        agregarSiNoExiste(lista, "kpi.poder_combate", "Poder de Combate", "Combat Power", "Poder de Combate");
        agregarSiNoExiste(lista, "kpi.planes_activos", "Planes Activos", "Active Plans", "Planos Ativos");
        agregarSiNoExiste(lista, "kpi.tareas_hoy", "Tareas Hoy", "Tasks Today", "Tarefas Hoje");
        agregarSiNoExiste(lista, "kpi.proxima_eval", "Próxima Eval.", "Next Eval.", "Próxima Aval.");
        agregarSiNoExiste(lista, "kpi.hoy", "¡Hoy!", "Today!", "Hoje!");
        agregarSiNoExiste(lista, "kpi.dias", "días", "days", "dias");

        agregarSiNoExiste(lista, "error.generic", "Ha ocurrido un error", "An error occurred", "Ocorreu um erro");
        agregarSiNoExiste(lista, "error.upload", "Error al subir archivo", "Error uploading file", "Erro ao enviar arquivo");
        agregarSiNoExiste(lista, "error.campos_obligatorios", "Campos obligatorios incompletos", "Required fields missing", "Campos obrigatórios incompletos");
        agregarSiNoExiste(lista, "error.campos_incompletos", "Por favor, llene todos los campos", "Please fill all fields", "Por favor, preencha todos os campos");
        agregarSiNoExiste(lista, "error.contrasenas_no_coinciden", "Las contraseñas no coinciden", "Passwords do not match", "As senhas não coincidem");
        agregarSiNoExiste(lista, "error.usuario.existe", "Este correo ya está registrado", "This email is already registered", "Este e-mail já está registrado");

        agregarSiNoExiste(lista, "msg.success.saved", "Guardado exitosamente", "Saved successfully", "Salvo com sucesso");
        agregarSiNoExiste(lista, "msg.success.updated", "Actualizado exitosamente", "Updated successfully", "Atualizado com sucesso");
        agregarSiNoExiste(lista, "msg.success.deleted", "Eliminado exitosamente", "Deleted successfully", "Excluído com sucesso");
        agregarSiNoExiste(lista, "msg.success.config_saved", "Configuración guardada correctamente", "Configuration saved successfully", "Configuração salva com sucesso");
        agregarSiNoExiste(lista, "msg.foto.actualizada", "Foto actualizada correctamente", "Photo updated successfully", "Foto atualizada com sucesso");
        agregarSiNoExiste(lista, "msg.diario.vacio", "Tu diario está vacío. Empieza hoy.", "Your diary is empty. Start today.", "Seu diário está vazio. Comece hoje.");
        agregarSiNoExiste(lista, "msg.entrada.actualizada", "Entrada actualizada.", "Entry updated.", "Entrada atualizada.");
        agregarSiNoExiste(lista, "msg.exito.archivo_subido", "Documento guardado correctamente", "Document saved successfully", "Documento salvo com sucesso");
        agregarSiNoExiste(lista, "msg.exito.puede_continuar", "¡Excelente! Ya puedes finalizar", "Great! You can now finish", "Ótimo! Você pode finalizar agora");
        agregarSiNoExiste(lista, "msg.error.nube", "Error al conectar con la Nube", "Error connecting to the Cloud", "Erro ao conectar com a Nuvem");
        agregarSiNoExiste(lista, "msg.excelente.trabajo", "¡Excelente trabajo!", "Excellent work!", "Excelente trabalho!");
        agregarSiNoExiste(lista, "msg.error.guardar", "Error al guardar", "Error saving", "Erro ao salvar");
        agregarSiNoExiste(lista, "msg.dia.descanso", "Hoy es día de descanso. ¡Recupérate!", "Today is a rest day. Recover!", "Hoje é dia de descanso. Recupere-se!");
        agregarSiNoExiste(lista, "msg.entrenamiento.finalizado", "¡Entrenamiento del día finalizado!", "Daily training finished!", "Treino do dia finalizado!");
        agregarSiNoExiste(lista, "msg.error.asignacion", "Error al asignar", "Error assigning", "Erro ao atribuir");
        agregarSiNoExiste(lista, "msg.exito.asignacion", "Judoka asignado correctamente", "Judoka assigned successfully", "Judoca atribuído com sucesso");
        agregarSiNoExiste(lista, "msg.error.remocion", "Error al remover", "Error removing", "Erro ao remover");
        agregarSiNoExiste(lista, "msg.exito.remocion", "Judoka removido del grupo", "Judoka removed from group", "Judoca removido do grupo");
        agregarSiNoExiste(lista, "msg.selecciona.categoria.para.comparar", "Selecciona una categoría arriba para ver tu evolución.", "Select a category above to see your progress.", "Selecione uma categoria acima para ver sua evolução.");

        agregarSiNoExiste(lista, "menu.dashboard", "Dashboard", "Dashboard", "Dashboard");
        agregarSiNoExiste(lista, "menu.mis.microciclos", "Mis microciclos", "My microcycles", "Meus microciclos");
        agregarSiNoExiste(lista, "menu.comunidad", "Comunidad", "Community", "Comunidade");
        agregarSiNoExiste(lista, "menu.mi.perfil", "Mi Perfil", "My Profile", "Meu Perfil");
        agregarSiNoExiste(lista, "menu.invitar", "Invitar", "Invite", "Convidar");
        agregarSiNoExiste(lista, "menu.invitar.sensei", "Invitar Sensei", "Invite Sensei", "Convidar Sensei");
        agregarSiNoExiste(lista, "menu.admisiones", "Admisiones", "Admissions", "Admissões");
        agregarSiNoExiste(lista, "menu.asistencia", "Asistencia", "Attendance", "Presença");
        agregarSiNoExiste(lista, "menu.biblioteca", "Biblioteca", "Library", "Biblioteca");
        agregarSiNoExiste(lista, "menu.resultados", "Resultados", "Results", "Resultados");
        agregarSiNoExiste(lista, "menu.tatami", "Modo Tatami", "Tatami Mode", "Modo Tatame");
        agregarSiNoExiste(lista, "menu.historial", "Bitácora", "Logbook", "Diário de Bordo");
        agregarSiNoExiste(lista, "menu.reglas_gamificacion", "Premios", "Rewards", "Prêmios");

        return lista;
    }

    private List<Traduccion> traduccionesDashboard() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "dashboard.welcome", "Hola, {0}", "Hello, {0}", "Olá, {0}");
        agregarSiNoExiste(lista, "dashboard.titulo", "Panel de Control", "Dashboard", "Painel de Controle");
        agregarSiNoExiste(lista, "dashboard.boton.tomar_asistencia", "Tomar Asistencia", "Take Attendance", "Registrar Presença");
        agregarSiNoExiste(lista, "dashboard.btn.tareas", "Ir a Mis Tareas", "Go to My Tasks", "Ir para Minhas Tarefas");
        agregarSiNoExiste(lista, "dashboard.kpi.total_judokas", "Total Judokas", "Total Judokas", "Total de Judocas");
        agregarSiNoExiste(lista, "dashboard.kpi.grupos_activos", "Grupos Activos", "Active Groups", "Grupos Ativos");
        agregarSiNoExiste(lista, "dashboard.kpi.pruebas_hoy", "Para Hoy", "For Today", "Para Hoje");
        agregarSiNoExiste(lista, "dashboard.kpi.asistencia_promedio", "Asistencia Promedio", "Average Attendance", "Média de Presença");
        agregarSiNoExiste(lista, "dashboard.grafico.poder_combate_titulo", "Poder de Combate por Grupo", "Combat Power by Group", "Poder de Combate por Grupo");
        agregarSiNoExiste(lista, "dashboard.grafico.asistencia_30dias_titulo", "Asistencia últimos 30 días", "Last 30 days attendance", "Presença últimos 30 dias");
        agregarSiNoExiste(lista, "dashboard.grafico.promedio", "Promedio", "Average", "Média");
        agregarSiNoExiste(lista, "dashboard.grafico.asistencia_porcentaje", "Asistencia %", "Attendance %", "Presença %");
        agregarSiNoExiste(lista, "dashboard.mes1", "Mes 1", "Month 1", "Mês 1");

        agregarSiNoExiste(lista, "chart.radar.serie", "Nivel Actual", "Current Level", "Nível Atual");
        agregarSiNoExiste(lista, "chart.sin_datos", "Sin datos", "No data", "Sem dados");
        agregarSiNoExiste(lista, "chart.tu_progreso", "Tu Progreso", "Your Progress", "Seu Progresso");

        agregarSiNoExiste(lista, "empty.desc.realiza_pruebas", "Realiza pruebas para ver tu evolución.", "Take tests to see your progress.", "Realize testes para ver sua evolução.");

        agregarSiNoExiste(lista, "tooltip.trofeos", "Mis Trofeos e Insignias", "My Trophies & Badges", "Meus Troféus e Medalhas");
        agregarSiNoExiste(lista, "tooltip.palmares", "Mi Palmarés", "My Record", "Meu Histórico");
        agregarSiNoExiste(lista, "tooltip.cambiar.foto", "Cambiar foto de perfil", "Change profile photo", "Alterar foto de perfil");
        agregarSiNoExiste(lista, "tooltip.registro.permanente", "Registro permanente (No editable)", "Permanent record (Non-editable)", "Registro permanente (Não editável)");
        agregarSiNoExiste(lista, "tooltip.asignar.grupo", "Asignar a", "Assign to", "Atribuir a");
        agregarSiNoExiste(lista, "tooltip.remover.grupo", "Remover del grupo", "Remove from group", "Remover do grupo");

        return lista;
    }

    private List<Traduccion> traduccionesPerfil() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "perfil.tab.resumen", "Resumen", "Summary", "Resumo");
        agregarSiNoExiste(lista, "perfil.tab.pruebas", "Pruebas", "Tests", "Testes");
        agregarSiNoExiste(lista, "perfil.tab.tareas", "Tareas", "Tasks", "Tarefas");
        agregarSiNoExiste(lista, "perfil.tab.insignias", "Insignias", "Badges", "Medalhas");
        agregarSiNoExiste(lista, "perfil.tab.palmares", "Palmarés", "Record", "Histórico");
        agregarSiNoExiste(lista, "perfil.tab.documentos", "Documentos", "Documents", "Documentos");

        agregarSiNoExiste(lista, "perfil.antropometria", "Antropometría histórica", "Historical anthropometry", "Antropometria histórica");
        agregarSiNoExiste(lista, "perfil.sin_datos", "Sin datos antropométricos", "No anthropometric data", "Sem dados antropométricos");
        agregarSiNoExiste(lista, "perfil.sin_resultados", "No hay resultados registrados", "No test results recorded", "Nenhum resultado registrado");
        agregarSiNoExiste(lista, "perfil.sin_tareas", "No hay tareas ejecutadas", "No tasks executed", "Nenhuma tarefa executada");
        agregarSiNoExiste(lista, "perfil.sin_palmares", "Sin participación en competiciones", "No competition record", "Sem participação em competições");
        agregarSiNoExiste(lista, "perfil.sin_documentos", "No hay documentos subidos", "No documents uploaded", "Nenhum documento enviado");

        agregarSiNoExiste(lista, "perfil.filtro.prueba", "Prueba", "Test", "Teste");
        agregarSiNoExiste(lista, "perfil.filtro.desde", "Desde", "From", "De");
        agregarSiNoExiste(lista, "perfil.filtro.hasta", "Hasta", "To", "Até");
        agregarSiNoExiste(lista, "perfil.filtrar", "Filtrar", "Filter", "Filtrar");

        agregarSiNoExiste(lista, "perfil.grid.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, "perfil.grid.prueba", "Prueba", "Test", "Teste");
        agregarSiNoExiste(lista, "perfil.grid.metrica", "Métrica", "Metric", "Métrica");
        agregarSiNoExiste(lista, "perfil.grid.valor", "Valor", "Value", "Valor");
        agregarSiNoExiste(lista, "perfil.grid.clasificacion", "Clasificación", "Classification", "Classificação");
        agregarSiNoExiste(lista, "perfil.grid.puntos", "Puntos", "Points", "Pontos");
        agregarSiNoExiste(lista, "perfil.grid.tarea", "Tarea", "Task", "Tarefa");
        agregarSiNoExiste(lista, "perfil.grid.completada", "Completada", "Completed", "Concluída");
        agregarSiNoExiste(lista, "perfil.grid.evento", "Evento", "Event", "Evento");
        agregarSiNoExiste(lista, "perfil.grid.lugar", "Lugar", "Place", "Local");
        agregarSiNoExiste(lista, "perfil.grid.resultado", "Resultado", "Result", "Resultado");
        agregarSiNoExiste(lista, "perfil.grid.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste(lista, "perfil.grid.archivo", "Archivo", "File", "Arquivo");
        agregarSiNoExiste(lista, "perfil.grid.accion", "Acción", "Action", "Ação");
        agregarSiNoExiste(lista, "perfil.ver", "Ver", "View", "Ver");

        agregarSiNoExiste(lista, "perfil.notas.titulo", "Bitácora de Reflexión", "Reflection Journal", "Diário de Reflexão");
        agregarSiNoExiste(lista, "perfil.notas.placeholder", "Escribe aquí tus pensamientos...", "Write your thoughts here...", "Escreva aqui seus pensamentos...");
        agregarSiNoExiste(lista, "perfil.msg.guardado", "Reflexión guardada", "Reflection saved", "Reflexão salva");
        agregarSiNoExiste(lista, "title.editar.reflexion", "Editar Reflexión", "Edit Reflection", "Editar Reflexão");

        agregarSiNoExiste(lista, "view.asignacion.titulo", "Asignación de Judokas a Grupos", "Judoka Group Assignment", "Atribuição de Judocas a Grupos");
        agregarSiNoExiste(lista, "view.gestion.usuarios.titulo", "Gestión de Usuarios", "User Management", "Gestão de Usuários");
        agregarSiNoExiste(lista, "view.judoka.microciclo.titulo", "Entrenamiento de Hoy", "Today's Training", "Treino de Hoje");
        agregarSiNoExiste(lista, "view.sensei.microciclo.titulo", "Microciclo", "Microcycle", "Microciclo");
        agregarSiNoExiste(lista, "agenda.titulo", "Agenda", "Agenda", "Agenda");
        agregarSiNoExiste(lista, "finanzas.titulo", "Contabilidad", "Accounting", "contabilidade");

        return lista;
    }

    private List<Traduccion> traduccionesResultados() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "resultados.titulo", "Registro de Resultados", "Test Results Registration", "Registro de Resultados");
        agregarSiNoExiste(lista, "resultados.selector.judoka", "Seleccionar Judoka", "Select Judoka", "Selecionar Judoca");
        agregarSiNoExiste(lista, "resultados.grid.planes.header", "Planes de Evaluación", "Evaluation Plans", "Planos de Avaliação");
        agregarSiNoExiste(lista, "resultados.grid.pruebas.header", "Pruebas del Plan", "Plan Tests", "Testes do Plano");
        agregarSiNoExiste(lista, "resultados.feedback.inicio", "Resultados guardados: ", "Results saved: ", "Resultados salvos: ");
        agregarSiNoExiste(lista, "resultados.feedback.sjft", "Índice SJFT: %.2f (%s). ", "SJFT Index: %.2f (%s). ", "Índice SJFT: %.2f (%s). ");
        agregarSiNoExiste(lista, "resultados.feedback.prueba", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ");
        agregarSiNoExiste(lista, "resultados.error.guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste(lista, "resultados.sin_clasificacion", "Sin clasificación", "No classification", "Sem classificação");
        agregarSiNoExiste(lista, "resultados.sjft.error.faltan_datos", "Faltan datos para calcular el índice SJFT.", "Missing data to calculate SJFT index.", "Faltam dados para calcular o índice SJFT.");
        agregarSiNoExiste(lista, "resultados.sjft.error.total_cero", "El total de proyecciones no puede ser cero.", "Total projections cannot be zero.", "Total de projeções não pode ser zero.");
        agregarSiNoExiste(lista, "resultados.sjft.error.metrica_no_encontrada", "Métrica no encontrada.", "Metric not found.", "Métrica não encontrada.");
        agregarSiNoExiste(lista, "resultados.sjft.nota_automatica", "Índice SJFT calculado automáticamente.", "SJFT index automatically calculated.", "Índice SJFT calculado automaticamente.");

        return lista;
    }

    private List<Traduccion> traduccionesAsistencia() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "asistencia.boton.cerrar_clase", "Cerrar Clase y Guardar", "Close Class and Save", "Fechar Aula e Salvar");
        agregarSiNoExiste(lista, "asistencia.boton.cerrar", "Cerrar", "Close", "Fechar");
        agregarSiNoExiste(lista, "asistencia.selector.grupo", "Selecciona el Grupo", "Select Group", "Selecione o Grupo");
        agregarSiNoExiste(lista, "asistencia.placeholder.grupo", "Ej: Infantiles Martes", "Ex: Kids Tuesday", "Ex: Infantis Terça");
        agregarSiNoExiste(lista, "asistencia.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, "asistencia.mensaje.sin_alumnos", "Este grupo no tiene alumnos asignados.", "This group has no assigned students.", "Este grupo não tem alunos atribuídos.");
        agregarSiNoExiste(lista, "asistencia.estado.ausente", "AUSENTE", "ABSENT", "AUSENTE");
        agregarSiNoExiste(lista, "asistencia.estado.presente", "PRESENTE", "PRESENT", "PRESENTE");
        agregarSiNoExiste(lista, "asistencia.notificacion.cargados", "Cargados", "Loaded", "Carregados");
        agregarSiNoExiste(lista, "asistencia.notificacion.alumnos", "alumnos", "students", "alunos");
        agregarSiNoExiste(lista, "asistencia.notificacion.registrada", "Asistencia registrada", "Attendance registered", "Presença registrada");
        agregarSiNoExiste(lista, "asistencia.notificacion.presentes", "Presentes", "Present", "Presentes");
        agregarSiNoExiste(lista, "asistencia.notificacion.error_guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.titulo", "🚨 INFORMACIÓN DE EMERGENCIA", "🚨 EMERGENCY INFORMATION", "🚨 INFORMAÇÃO DE EMERGÊNCIA");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.acudiente_movil", "Acudiente/Móvil", "Guardian/Mobile", "Responsável/Celular");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.email", "Email", "Email", "E-mail");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.eps", "EPS", "Health Insurance", "Convênio");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.nombre_acudiente", "Nombre Acudiente", "Guardian Name", "Nome do Responsável");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.llamar_ahora", "Llamar Ahora", "Call Now", "Ligar Agora");
        agregarSiNoExiste(lista, "asistencia.dialog.sos.sin_telefono", "Sin Teléfono Registrado", "No Phone Registered", "Sem Telefone Registrado");

        return lista;
    }

    private List<Traduccion> traduccionesGamificacion() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "gamificacion.titulo", "Reglas de Gamificación", "Gamification Rules", "Regras de Gamificação");
        agregarSiNoExiste(lista, "gamificacion.nueva_regla", "Nueva Regla", "New Rule", "Nova Regra");
        agregarSiNoExiste(lista, "widget.mido.titulo", "Mi Do (La Vía)", "My Do (The Way)", "Meu Do (O Caminho)");
        agregarSiNoExiste(lista, "widget.mido.shin", "SHIN (Mente)", "SHIN (Mind)", "SHIN (Mente)");
        agregarSiNoExiste(lista, "widget.mido.gi", "GI (Técnica)", "GI (Technique)", "GI (Técnica)");
        agregarSiNoExiste(lista, "widget.mido.tai", "TAI (Cuerpo)", "TAI (Body)", "TAI (Corpo)");
        agregarSiNoExiste(lista, "widget.mido.btn_catalogo", "Ver Catálogo", "View Catalog", "Ver Catálogo");
        agregarSiNoExiste(lista, "widget.mido.catalogo_titulo", "Salón de la Fama", "Hall of Fame", "Salão da Fama");
        agregarSiNoExiste(lista, "widget.mido.msg_inicio", "¡Tu camino comienza!", "Your journey begins!", "Sua jornada começa!");
        agregarSiNoExiste(lista, "badge.estado.desbloqueada", "¡Insignia Desbloqueada!", "Badge Unlocked!", "Medalha Desbloqueada!");
        agregarSiNoExiste(lista, "badge.estado.bloqueada", "Insignia Bloqueada", "Badge Locked", "Medalha Bloqueada");
        agregarSiNoExiste(lista, "badge.estado.sin_datos", "Sin Datos", "No Data", "Sem Dados");
        agregarSiNoExiste(lista, "badge.label.obtenida", "Obtenida el", "Earned on", "Obtida em");
        agregarSiNoExiste(lista, "badge.label.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste(lista, "enum.categoriainsignia.shin", "Shin (Mente)", "Shin (Mind)", "Shin (Mente)");
        agregarSiNoExiste(lista, "enum.categoriainsignia.gi", "Gi (Técnica)", "Gi (Technique)", "Gi (Técnica)");
        agregarSiNoExiste(lista, "enum.categoriainsignia.tai", "Tai (Cuerpo)", "Tai (Body)", "Tai (Corpo)");

        return lista;
    }

    private List<Traduccion> traduccionesCheckIn() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "checkin.titulo", "Control de Asistencia GPS", "GPS Attendance Control", "Controle de Presença GPS");
        agregarSiNoExiste(lista, "checkin.status.ready", "Listo para verificar ubicación.", "Ready to check location.", "Pronto para verificar localização.");
        agregarSiNoExiste(lista, "checkin.btn.marcar", "Marcar Asistencia", "Mark Attendance", "Registrar Presença");
        agregarSiNoExiste(lista, "checkin.status.locating", "Localizando...", "Locating...", "Localizando...");
        agregarSiNoExiste(lista, "checkin.status.requesting", "Solicitando permiso GPS...", "Requesting GPS permission...", "Solicitando permissão GPS...");
        agregarSiNoExiste(lista, "checkin.btn.retry", "Reintentar Check-in", "Retry Check-in", "Tentar Novamente");
        agregarSiNoExiste(lista, "checkin.error.denied", "Error GPS: Permiso denegado.", "GPS Error: Permission denied.", "Erro GPS: Permissão negada.");
        agregarSiNoExiste(lista, "checkin.error.browser", "ERROR: Debes habilitar el GPS.", "ERROR: You must enable GPS.", "ERRO: Você deve habilitar o GPS.");
        agregarSiNoExiste(lista, "checkin.status.validating", "Validando distancia...", "Validating distance...", "Validando distância...");
        agregarSiNoExiste(lista, "checkin.btn.success", "¡Asistencia Marcada!", "Attendance Marked!", "Presença Registrada!");
        agregarSiNoExiste(lista, "checkin.status.registered", "Te has registrado correctamente.", "You have registered correctly.", "Você se registrou corretamente.");
        agregarSiNoExiste(lista, "checkin.msg.oss", "¡Asistencia registrada! Oss.", "Attendance registered! Oss.", "Presença registrada! Oss.");

        return lista;
    }

    private List<Traduccion> traduccionesGrupos() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "grupos.titulo", "Gestión de Grupos", "Group Management", "Gestão de Grupos");
        agregarSiNoExiste(lista, "grupos.btn.nuevo", "Nuevo Grupo", "New Group", "Novo Grupo");
        agregarSiNoExiste(lista, "grupos.grid.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste(lista, "grupos.grid.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste(lista, "grupos.grid.miembros", "Miembros", "Members", "Membros");
        agregarSiNoExiste(lista, "grupos.label.alumnos", "alumnos", "students", "alunos");
        agregarSiNoExiste(lista, "grupos.tooltip.gestionar_miembros", "Gestionar Miembros", "Manage Members", "Gerenciar Membros");
        agregarSiNoExiste(lista, "grupos.dialog.miembros.titulo", "Miembros de", "Members of", "Membros de");
        agregarSiNoExiste(lista, "grupos.field.buscar_alumno", "Buscar alumno para agregar...", "Search student to add...", "Buscar aluno para adicionar...");
        agregarSiNoExiste(lista, "grupos.section.agregar", "Agregar Nuevo Miembro", "Add New Member", "Adicionar Novo Membro");
        agregarSiNoExiste(lista, "grupos.section.actuales", "Miembros Actuales", "Current Members", "Membros Atuais");

        return lista;
    }

    private List<Traduccion> traduccionesCampeonatos() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "campeonatos.titulo", "Gestión de Campeonatos", "Championship Management", "Gestão de Campeonatos");
        agregarSiNoExiste(lista, "campeonatos.btn.nueva_convocatoria", "Nueva Convocatoria", "New Call-up", "Nova Convocação");
        agregarSiNoExiste(lista, "campeonatos.grid.evento", "Evento", "Event", "Evento");
        agregarSiNoExiste(lista, "campeonatos.grid.resultado", "Resultado", "Result", "Resultado");
        agregarSiNoExiste(lista, "campeonatos.dialog.convocatoria.titulo", "Crear Convocatoria", "Create Call-up", "Criar Convocação");
        agregarSiNoExiste(lista, "campeonatos.field.nombre_evento", "Nombre del Evento", "Event Name", "Nome do Evento");
        agregarSiNoExiste(lista, "campeonatos.field.lugar", "Lugar", "Place", "Local");
        agregarSiNoExiste(lista, "campeonatos.field.nivel", "Nivel", "Level", "Nível");
        agregarSiNoExiste(lista, "campeonatos.field.seleccionar_atletas", "Seleccionar Atletas", "Select Athletes", "Selecionar Atletas");
        agregarSiNoExiste(lista, "campeonatos.msg.inscritos", "atletas inscritos exitosamente.", "athletes registered successfully.", "atletas inscritos com sucesso.");
        agregarSiNoExiste(lista, "campeonatos.dialog.resultado.titulo", "Resultado:", "Result:", "Resultado:");
        agregarSiNoExiste(lista, "campeonatos.field.medalla", "Medalla / Puesto", "Medal / Place", "Medalha / Posição");
        agregarSiNoExiste(lista, "campeonatos.field.link_video", "Link Video (YouTube)", "Video Link (YouTube)", "Link do Vídeo (YouTube)");

        return lista;
    }

    private List<Traduccion> traduccionesCampos() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "campos.titulo", "Campos de Entrenamiento", "Training Camps", "Campos de Treinamento");
        agregarSiNoExiste(lista, "campos.btn.programar", "Programar Campo", "Schedule Camp", "Agendar Campo");
        agregarSiNoExiste(lista, "campos.grid.nombre", "Campo / Evento", "Camp / Event", "Campo / Evento");
        agregarSiNoExiste(lista, "campos.estado.en_curso", "En Curso", "In Progress", "Em Andamento");
        agregarSiNoExiste(lista, "campos.btn.certificar", "Certificar Cumplimiento", "Certify Compliance", "Certificar Cumprimento");
        agregarSiNoExiste(lista, "campos.dialog.programar.titulo", "Programar Campo", "Schedule Camp", "Agendar Campo");
        agregarSiNoExiste(lista, "campos.field.nombre", "Nombre del Campo", "Camp Name", "Nome do Campo");
        agregarSiNoExiste(lista, "campos.placeholder.ej_campamento", "Ej: Campamento de Altura", "Ex: Altitude Camp", "Ex: Acampamento de Altitude");
        agregarSiNoExiste(lista, "campos.field.lugar", "Ubicación", "Location", "Localização");
        agregarSiNoExiste(lista, "campos.field.objetivo", "Enfoque / Objetivo", "Focus / Goal", "Foco / Objetivo");
        agregarSiNoExiste(lista, "campos.placeholder.ej_tactico", "Ej: Táctico Competitivo", "Ex: Competitive Tactical", "Ex: Tático Competitivo");
        agregarSiNoExiste(lista, "campos.field.convocados", "Convocados", "Invited", "Convocados");
        agregarSiNoExiste(lista, "campos.msg.programado", "Campo programado para", "Camp scheduled for", "Campo agendado para");
        agregarSiNoExiste(lista, "campos.dialog.certificar.titulo", "Certificar:", "Certify:", "Certificar:");
        agregarSiNoExiste(lista, "campos.label.pregunta_cumplimiento", "¿El judoka completó satisfactoriamente el campo?", "Did the judoka complete the camp successfully?", "O judoca completou o campo satisfatoriamente?");
        agregarSiNoExiste(lista, "campos.field.puntos_ascenso", "Puntos de Ascenso", "Promotion Points", "Pontos de Promoção");
        agregarSiNoExiste(lista, "campos.btn.confirmar_puntos", "Certificar y Otorgar Puntos", "Certify and Award Points", "Certificar e Atribuir Pontos");

        return lista;
    }

    private List<Traduccion> traduccionesInventario() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "inventario.titulo", "Tienda del Dojo", "Dojo Store", "Loja do Dojo");
        agregarSiNoExiste(lista, "inventario.btn.nuevo", "Nuevo Producto", "New Product", "Novo Produto");
        agregarSiNoExiste(lista, "inventario.grid.articulo", "Artículo", "Item", "Artigo");
        agregarSiNoExiste(lista, "inventario.grid.stock", "Stock", "Stock", "Estoque");
        agregarSiNoExiste(lista, "inventario.grid.venta", "Precio Venta", "Selling Price", "Preço Venda");
        agregarSiNoExiste(lista, "inventario.grid.costo", "Costo", "Cost", "Custo");
        agregarSiNoExiste(lista, "inventario.status.agotado", "AGOTADO", "OUT OF STOCK", "ESGOTADO");
        agregarSiNoExiste(lista, "inventario.tooltip.add_stock", "Agregar Stock", "Add Stock", "Adicionar Estoque");
        agregarSiNoExiste(lista, "inventario.dialog.venta", "Registrar Venta", "Register Sale", "Registrar Venda");
        agregarSiNoExiste(lista, "inventario.msg.venta_ok", "Venta registrada y descontada del inventario", "Sale registered and deducted from inventory", "Venda registrada e deduzida do estoque");
        agregarSiNoExiste(lista, "inventario.dialog.stock", "Reabastecer Stock", "Restock", "Reabastecer Estoque");
        agregarSiNoExiste(lista, "inventario.field.cantidad_ingreso", "Cantidad a Ingresar", "Quantity to Add", "Quantidade a Adicionar");
        agregarSiNoExiste(lista, "inventario.dialog.nuevo", "Nuevo Producto", "New Product", "Novo Produto");
        agregarSiNoExiste(lista, "inventario.dialog.editar", "Editar Producto", "Edit Product", "Editar Produto");
        agregarSiNoExiste(lista, "inventario.field.costo", "Costo Compra ($)", "Purchase Cost ($)", "Custo Compra ($)");
        agregarSiNoExiste(lista, "inventario.field.precio", "Precio Venta ($)", "Selling Price ($)", "Preço Venda ($)");
        agregarSiNoExiste(lista, "inventario.field.stock_inicial", "Stock Inicial", "Initial Stock", "Estoque Inicial");

        return lista;
    }

    private List<Traduccion> traduccionesTesoreria() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "tesoreria.titulo", "Gestión Financiera", "Financial Management", "Gestão Financeira");
        agregarSiNoExiste(lista, "tesoreria.tab.registrar_ingreso", "Registrar Ingreso", "Register Income", "Registrar Receita");
        agregarSiNoExiste(lista, "tesoreria.tab.registrar_gasto", "Registrar Gasto", "Register Expense", "Registrar Despesa");
        agregarSiNoExiste(lista, "tesoreria.tab.balance_reportes", "Balance y Reportes", "Balance and Reports", "Balanço e Relatórios");
        agregarSiNoExiste(lista, "tesoreria.alumno", "Alumno", "Student", "Aluno");
        agregarSiNoExiste(lista, "tesoreria.concepto", "Concepto", "Concept", "Conceito");
        agregarSiNoExiste(lista, "tesoreria.valor", "Valor ($)", "Amount ($)", "Valor ($)");
        agregarSiNoExiste(lista, "tesoreria.valor_pagado", "Valor Pagado ($)", "Amount Paid ($)", "Valor Pago ($)");
        agregarSiNoExiste(lista, "tesoreria.metodo_pago", "Método de Pago", "Payment Method", "Método de Pagamento");
        agregarSiNoExiste(lista, "tesoreria.observacion", "Observación", "Observation", "Observação");
        agregarSiNoExiste(lista, "tesoreria.categoria_gasto", "Categoría de Gasto", "Expense Category", "Categoria de Despesa");
        agregarSiNoExiste(lista, "tesoreria.detalle_proveedor", "Detalle / Proveedor", "Detail / Supplier", "Detalhe / Fornecedor");
        agregarSiNoExiste(lista, "tesoreria.foto_factura", "Foto de Factura", "Invoice Photo", "Foto da Fatura");
        agregarSiNoExiste(lista, "tesoreria.soporte", "Soporte", "Support", "Suporte");
        agregarSiNoExiste(lista, "tesoreria.boton.registrar_generar_recibo", "Registrar y Generar Recibo", "Register and Generate Receipt", "Registrar e Gerar Recibo");
        agregarSiNoExiste(lista, "tesoreria.boton.registrar_salida", "Registrar Salida", "Register Outflow", "Registrar Saída");
        agregarSiNoExiste(lista, "tesoreria.boton.guardar", "Guardar", "Save", "Salvar");
        agregarSiNoExiste(lista, "tesoreria.kpi.ingresos_mes", "Ingresos Mes", "Monthly Income", "Receitas do Mês");
        agregarSiNoExiste(lista, "tesoreria.kpi.egresos_mes", "Egresos Mes", "Monthly Expenses", "Despesas do Mês");
        agregarSiNoExiste(lista, "tesoreria.kpi.balance", "Balance", "Balance", "Balanço");
        agregarSiNoExiste(lista, "tesoreria.grid.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, "tesoreria.grid.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste(lista, "tesoreria.grid.concepto", "Concepto", "Concept", "Conceito");
        agregarSiNoExiste(lista, "tesoreria.grid.monto", "Monto", "Amount", "Valor");
        agregarSiNoExiste(lista, "tesoreria.grid.judoka", "Judoka", "Judoka", "Judoca");
        agregarSiNoExiste(lista, "tesoreria.grid.soporte", "Soporte", "Support", "Suporte");
        agregarSiNoExiste(lista, "tesoreria.dialog.nuevo_concepto.titulo", "Nuevo Concepto de", "New Concept for", "Novo Conceito de");
        agregarSiNoExiste(lista, "tesoreria.dialog.nuevo_concepto.nombre", "Nombre del Concepto", "Concept Name", "Nome do Conceito");
        agregarSiNoExiste(lista, "tesoreria.dialog.nuevo_concepto.valor_sugerido", "Valor Sugerido", "Suggested Value", "Valor Sugerido");
        agregarSiNoExiste(lista, "tesoreria.validacion.concepto_monto", "Concepto y Monto son obligatorios", "Concept and Amount are required", "Conceito e Valor são obrigatórios");
        agregarSiNoExiste(lista, "tesoreria.validacion.categoria_monto", "Categoría y Monto obligatorios", "Category and Amount are required", "Categoria e Valor são obrigatórios");
        agregarSiNoExiste(lista, "tesoreria.notificacion.ingreso_exitoso", "Ingreso registrado con éxito", "Income registered successfully", "Receita registrada com sucesso");
        agregarSiNoExiste(lista, "tesoreria.notificacion.soporte_cargado", "Soporte cargado", "Support uploaded", "Suporte carregado");
        agregarSiNoExiste(lista, "tesoreria.notificacion.error_subir", "Error al subir: ", "Error uploading: ", "Erro ao enviar: ");
        agregarSiNoExiste(lista, "tesoreria.notificacion.gasto_registrado", "Gasto registrado", "Expense registered", "Despesa registrada");
        agregarSiNoExiste(lista, "tesoreria.notificacion.concepto_creado", "Concepto Creado", "Concept Created", "Conceito Criado");

        return lista;
    }

    private List<Traduccion> traduccionesAdmisiones() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "admisiones.titulo", "Validación de Ingresos", "Admission Validation", "Validação de Ingressos");
        agregarSiNoExiste(lista, "admisiones.descripcion", "Revise los documentos y pagos de los aspirantes.", "Review applicants' documents and payments.", "Revise os documentos e pagamentos dos candidatos.");
        agregarSiNoExiste(lista, "admisiones.grid.registrado", "Registrado", "Registered", "Registrado");
        agregarSiNoExiste(lista, "admisiones.grid.documentos", "Documentos", "Documents", "Documentos");
        agregarSiNoExiste(lista, "admisiones.grid.pago", "Pago Matrícula", "Enrollment Payment", "Pagamento Matrícula");
        agregarSiNoExiste(lista, "admisiones.btn.marcar_pago", "Marcar Pago Manual", "Mark Manual Payment", "Marcar Pagamento Manual");
        agregarSiNoExiste(lista, "admisiones.msg.activado", "¡Judoka activado con éxito!", "Judoka activated successfully!", "Judoca ativado com sucesso!");
        agregarSiNoExiste(lista, "admisiones.msg.rechazado", "Aspirante rechazado.", "Applicant rejected.", "Candidato rejeitado.");

        agregarSiNoExiste(lista, "error.admisiones.requisitos_incompletos", "Requisitos incompletos", "Incomplete requirements", "Requisitos incompletos");
        agregarSiNoExiste(lista, "error.admisiones.falta_waiver", "Falta Exoneración de Responsabilidad", "Missing Waiver", "Falta Exoneração de Responsabilidade");
        agregarSiNoExiste(lista, "error.admisiones.falta_eps", "Falta Certificado EPS", "Missing Health Insurance", "Falta Comprovante de Convênio");
        agregarSiNoExiste(lista, "error.admisiones.falta_pago", "Falta Comprobante de Pago", "Missing Payment Receipt", "Falta Comprovante de Pagamento");

        agregarSiNoExiste(lista, "vista.wizard.titulo", "Asistente de Admisión", "Admission Wizard", "Assistente de Admissão");
        agregarSiNoExiste(lista, "vista.wizard.paso1.titulo", "Paso 1: Datos Físicos", "Step 1: Physical Data", "Passo 1: Dados Físicos");
        agregarSiNoExiste(lista, "vista.wizard.paso1.desc", "Para personalizar tu entrenamiento, necesitamos conocer tu categoría.", "To personalize your training, we need to know your category.", "Para personalizar seu treino, precisamos conhecer sua categoria.");
        agregarSiNoExiste(lista, "vista.wizard.paso2.titulo", "Paso 2: Subir Documentos", "Step 2: Upload Documents", "Passo 2: Enviar Documentos");
        agregarSiNoExiste(lista, "vista.wizard.paso2.desc.completa", "Sube los documentos requeridos.", "Upload the required documents.", "Envie os documentos necessários.");
        agregarSiNoExiste(lista, "vista.wizard.paso2.desc.descarga", "Descarga el formato, fírmalo y súbelo.", "Download the form, sign it, and upload it.", "Baixe o formulário, assine e envie.");
        agregarSiNoExiste(lista, "vista.wizard.paso3.titulo", "¡Casi listo!", "Almost done!", "Quase pronto!");
        agregarSiNoExiste(lista, "vista.wizard.paso3.mensaje", "Tus documentos han sido enviados. Tu Sensei revisará la información y activará tu cuenta pronto.", "Your documents have been sent. Your Sensei will review and activate your account soon.", "Seus documentos foram enviados. Seu Sensei revisará e ativará sua conta em breve.");
        agregarSiNoExiste(lista, "vista.wizard.paso3.mensaje.alt", "Tus documentos han sido enviados. ¡Gracias!", "Your documents have been sent. Thank you!", "Seus documentos foram enviados. Obrigado!");
        agregarSiNoExiste(lista, "boton.enviar_invitacion", "Enviar", "Send", "Enviar");
        agregarSiNoExiste(lista, "exito.invitacion_enviada", "Invitación enviada con éxito", "Invitation sent successfully", "Convite enviado com sucesso");
        agregarSiNoExiste(lista, "error.sistema", "Error del sistema, trata de nuevo por favor.", "System error, please try again.", "Erro do sistema, tente novamente.");

        return lista;
    }

    private List<Traduccion> traduccionesRegistro() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "registro.titulo", "Registro de Aspirante", "Applicant Registration", "Registro de Candidato");
        agregarSiNoExiste(lista, "registro.subtitulo", "Únete a nuestro Dojo", "Join our Dojo", "Junte-se ao nosso Dojo");
        agregarSiNoExiste(lista, "registro.btn.siguiente", "Siguiente", "Next", "Próximo");
        agregarSiNoExiste(lista, "registro.btn.volver", "Ya tengo cuenta", "I already have an account", "Já tenho conta");
        agregarSiNoExiste(lista, "registro.exito", "Registro Exitoso. Inicia Sesión.", "Registration successful. Log in.", "Registro bem-sucedido. Faça login.");
        agregarSiNoExiste(lista, "login.btn.registrar", "¿No tienes cuenta? Regístrate aquí.", "Don't have an account? Register here.", "Não tem conta? Registre-se aqui.");
        agregarSiNoExiste(lista, "login.form.titulo", "Iniciar Sesión", "Sign In", "Entrar");
        agregarSiNoExiste(lista, "login.lbl.usuario", "Usuario", "Username", "Usuário");
        agregarSiNoExiste(lista, "login.lbl.password", "Contraseña", "Password", "Senha");
        agregarSiNoExiste(lista, "login.btn.ingresar", "Entrar", "Log in", "Entrar");
        agregarSiNoExiste(lista, "login.link.olvido", "¿Olvidaste tu contraseña?", "Forgot password?", "Esqueceu a senha?");
        agregarSiNoExiste(lista, "login.error.titulo", "Usuario o contraseña incorrectos", "Incorrect username or password", "Usuário ou senha incorretos");
        agregarSiNoExiste(lista, "login.error.mensaje", "Por favor, verifica tus credenciales.", "Please check your credentials.", "Por favor, verifique suas credenciais.");

        agregarSiNoExiste(lista, "label.fecha_nacimiento", "Fecha de Nacimiento", "Birth Date", "Data de Nascimento");
        agregarSiNoExiste(lista, "label.peso_kg", "Peso (kg)", "Weight (kg)", "Peso (kg)");
        agregarSiNoExiste(lista, "label.contrasena", "Contraseña", "Password", "Senha");
        agregarSiNoExiste(lista, "label.confirmar_contrasena", "Confirmar Contraseña", "Confirm Password", "Confirmar Senha");
        agregarSiNoExiste(lista, "registro.crear_contrasena", "Crea tu Contraseña", "Create your Password", "Crie sua Senha");
        agregarSiNoExiste(lista, "registro.crear_cuenta", "Crear una Cuenta", "Create an Account", "Criar uma Conta");
        agregarSiNoExiste(lista, "mensaje.soporte", "Contacta al soporte deportivo.", "Contact sports support.", "Contate o suporte esportivo.");

        agregarSiNoExiste(lista, "error.titulo_ops", "¡Vaya! Algo salió mal.", "Oops! Something went wrong.", "Ops! Algo deu errado.");

        return lista;
    }

    private List<Traduccion> traduccionesBiblioteca() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "biblioteca.titulo", "Biblioteca de Tareas Diarias", "Daily Tasks Library", "Biblioteca de Tarefas Diárias");
        agregarSiNoExiste(lista, "biblioteca.boton.nueva_tarea", "Añadir Nueva Tarea", "Add New Task", "Adicionar Nova Tarefa");
        agregarSiNoExiste(lista, "biblioteca.grid.nombre_tarea", "Nombre Tarea", "Task Name", "Nome da Tarefa");
        agregarSiNoExiste(lista, "biblioteca.grid.meta", "Meta", "Goal", "Meta");
        agregarSiNoExiste(lista, "biblioteca.grid.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste(lista, "biblioteca.grid.video", "Video", "Video", "Vídeo");
        agregarSiNoExiste(lista, "biblioteca.grid.tooltip.tiene_video", "Tiene video", "Has video", "Tem vídeo");
        agregarSiNoExiste(lista, "biblioteca.grid.tooltip.editar_tarea", "Editar tarea", "Edit task", "Editar tarefa");
        agregarSiNoExiste(lista, "biblioteca.grid.acciones", "Acciones", "Actions", "Ações");
        agregarSiNoExiste(lista, "biblioteca.error.sensei_no_autenticado", "Sensei no autenticado", "Sensei not authenticated", "Sensei não autenticado");
        agregarSiNoExiste(lista, "biblioteca.notificacion.tarea_guardada", "Tarea guardada: %s", "Task saved: %s", "Tarefa salva: %s");
        agregarSiNoExiste(lista, "biblioteca.notificacion.error_guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");

        return lista;
    }

    private List<Traduccion> traduccionesComunidad() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "comunidad.tab.muro", "Muro del Dojo", "Dojo Wall", "Mural do Dojo");
        agregarSiNoExiste(lista, "comunidad.tab.chat", "Chat Grupal", "Group Chat", "Chat em Grupo");
        agregarSiNoExiste(lista, "comunidad.post.placeholder", "Comparte algo con el dojo...", "Share something with the dojo...", "Compartilhe algo com o dojo...");
        agregarSiNoExiste(lista, "comunidad.btn.subir_foto", "Subir Foto/Video", "Upload Photo/Video", "Enviar Foto/Vídeo");
        agregarSiNoExiste(lista, "comunidad.label.drop", "Arrastra archivos aquí...", "Drag files here...", "Arraste arquivos aqui...");
        agregarSiNoExiste(lista, "comunidad.btn.publicar", "Publicar", "Post", "Publicar");
        agregarSiNoExiste(lista, "comunidad.msg.publicado", "¡Publicado en el muro!", "Posted on the wall!", "Publicado no mural!");
        agregarSiNoExiste(lista, "comunidad.msg.archivo_listo", "Archivo listo", "File ready", "Arquivo pronto");
        agregarSiNoExiste(lista, "comunidad.warn.empty_post", "Escribe algo o sube una foto", "Write something or upload a photo", "Escreva algo ou envie uma foto");
        agregarSiNoExiste(lista, "comunidad.btn.comentar", "Comentar", "Comment", "Comentar");
        agregarSiNoExiste(lista, "comunidad.comment.placeholder", "Escribe una respuesta...", "Write a reply...", "Escreva uma resposta...");
        agregarSiNoExiste(lista, "comunidad.msg.comment_sent", "Comentario enviado", "Comment sent", "Comentário enviado");
        agregarSiNoExiste(lista, "comunidad.label.image_of", "Imagen de", "Image of", "Imagem de");
        agregarSiNoExiste(lista, "comunidad.chat.escribir", "Escribe un mensaje...", "Type a message...", "Digite uma mensagem...");
        agregarSiNoExiste(lista, "comunidad.chat.enviar", "Enviar", "Send", "Enviar");

        return lista;
    }

    private List<Traduccion> traduccionesAyuda() {
        List<Traduccion> lista = new ArrayList<>();
        agregarSiNoExiste(lista, "help.poder_combate.titulo", "¿Qué es el Poder de Combate?", "What is Combat Power?", "O que é Poder de Combate?");
        agregarSiNoExiste(lista, "help.poder_combate.contenido",
                "El Poder de Combate es un indicador global (1000-5000) que resume tu rendimiento físico basado en las pruebas evaluadas. Se calcula a partir de las clasificaciones (Excelente, Bueno, etc.) de cada prueba, agrupadas en los 5 bloques de Agudelo. El radar muestra tu nivel actual en cada bloque (de 1 a 5 estrellas). Las líneas de motivador en las gráficas representan el siguiente escalón a alcanzar según normas científicas (PROESP/CBJ) o el récord del dojo.",
                "Combat Power is a global indicator (1000-5000) that summarizes your physical performance based on evaluated tests. It is calculated from the classifications (Excellent, Good, etc.) of each test, grouped into Agudelo's 5 blocks. The radar shows your current level in each block (1 to 5 stars). The motivator lines in the charts represent the next step to achieve according to scientific norms (PROESP/CBJ) or the dojo record.",
                "O Poder de Combate é um indicador global (1000-5000) que resume seu desempenho físico com base nos testes avaliados. É calculado a partir das classificações (Excelente, Bom, etc.) de cada teste, agrupadas nos 5 blocos de Agudelo. O radar mostra seu nível atual em cada bloco (de 1 a 5 estrelas). As linhas motivadoras nos gráficos representam o próximo degrau a alcançar de acordo com normas científicas (PROESP/CBJ) ou o recorde do dojo.");

        agregarSiNoExiste(lista, "sabiduria.titulo", "Sabiduría del Sensei", "Sensei's Wisdom", "Sabedoria do Sensei");

        return lista;
    }

}