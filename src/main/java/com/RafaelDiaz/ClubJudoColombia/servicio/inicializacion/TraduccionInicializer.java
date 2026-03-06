package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TraduccionInicializer {

    private final TraduccionRepository traduccionRepo;

    public TraduccionInicializer(TraduccionRepository traduccionRepo) {
        this.traduccionRepo = traduccionRepo;
    }

    @Transactional
    public void inicializar() {
        System.out.println(">>> Verificando y cargando traducciones faltantes...");

        // ================== TRADUCCIONES COMUNES ==================
        agregarSiNoExiste("app.nombre", "Club de Judo Colombia", "Judo Club Colombia", "Clube de Judô Colômbia");
        agregarSiNoExiste("btn.guardar", "Guardar", "Save", "Salvar");
        agregarSiNoExiste("btn.cancelar", "Cancelar", "Cancel", "Cancelar");
        agregarSiNoExiste("btn.editar", "Editar", "Edit", "Editar");
        agregarSiNoExiste("btn.eliminar", "Eliminar", "Delete", "Excluir");
        agregarSiNoExiste("btn.crear", "Crear", "Create", "Criar");
        agregarSiNoExiste("btn.actualizar", "Actualizar", "Update", "Atualizar");
        agregarSiNoExiste("btn.guardar.cambios", "Guardar Cambios", "Save Changes", "Salvar Alterações");
        agregarSiNoExiste("btn.guardar_cambios", "Guardar Cambios", "Save Changes", "Salvar Alterações");
        agregarSiNoExiste("btn.cerrar", "Cerrar", "Close", "Fechar");
        agregarSiNoExiste("btn.cerrar.sesion", "Cerrar Sesión", "Logout", "Sair");
        agregarSiNoExiste("btn.confirmar", "Confirmar", "Confirm", "Confirmar");
        agregarSiNoExiste("btn.atras", "Atrás", "Back", "Voltar");
        agregarSiNoExiste("btn.finalizar", "Finalizar", "Finish", "Finalizar");
        agregarSiNoExiste("btn.siguiente.paso", "Siguiente", "Next", "Próximo");
        agregarSiNoExiste("btn.vender", "Vender", "Sell", "Vender");
        agregarSiNoExiste("btn.ver_pdf", "Ver PDF", "View PDF", "Ver PDF");
        agregarSiNoExiste("btn.nuevo.usuario", "Nuevo Usuario", "New User", "Novo Usuário");
        agregarSiNoExiste("btn.completado", "Completado", "Completed", "Concluído");
        agregarSiNoExiste("btn.marcar.hecho", "Marcar como Hecho", "Mark as Done", "Marcar como Feito");
        agregarSiNoExiste("btn.registrar.pensamiento", "Registrar Pensamiento", "Log Thought", "Registrar Pensamento");
        agregarSiNoExiste("btn.activar", "Activar", "Activate", "Ativar");
        agregarSiNoExiste("btn.rechazar", "Rechazar", "Reject", "Rejeitar");
        agregarSiNoExiste("btn.agregar", "Agregar", "Add", "Adicionar");
        agregarSiNoExiste("btn.quitar", "Quitar", "Remove", "Remover");
        agregarSiNoExiste("btn.filtrar", "Filtrar", "Filter", "Filtrar");
        agregarSiNoExiste("btn.pagar", "Pagar", "Pay", "Pagar");
        agregarSiNoExiste("btn.copiar_enlace", "Copiar Enlace", "Copy Link", "Copiar Link");
        agregarSiNoExiste("btn.copiar_mensaje", "Copiar Mensaje", "Copy Message", "Copiar Mensagem");
        agregarSiNoExiste("btn.descargar_formato", "Descargar Formato", "Download Form", "Baixar Formulário");
        agregarSiNoExiste("btn.descargar_formato_vacio", "Descargar Formato Vacío", "Download Empty Form", "Baixar Formulário Vazio");
        agregarSiNoExiste("btn.enviar_revision", "Enviar a Revisión", "Send for Review", "Enviar para Revisão");
        agregarSiNoExiste("btn.finalizar_sesion", "Finalizar Sesión", "End Session", "Finalizar Sessão");
        agregarSiNoExiste("btn.generar_enlace", "Generar Enlace", "Generate Link", "Gerar Link");
        agregarSiNoExiste("btn.guardar_finalizar", "Guardar y Finalizar", "Save and Finish", "Salvar e Finalizar");
        agregarSiNoExiste("btn.hecho", "Hecho", "Done", "Feito");
        agregarSiNoExiste("btn.salir", "Salir", "Exit", "Sair");

        // ================== TEXTO GENÉRICO ==================
        agregarSiNoExiste("generic.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste("generic.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste("generic.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste("generic.acciones", "Acciones", "Actions", "Ações");
        agregarSiNoExiste("generic.estado", "Estado", "Status", "Estado");
        agregarSiNoExiste("generic.cantidad", "Cantidad", "Quantity", "Quantidade");
        agregarSiNoExiste("generic.judoka", "Judoka", "Judoka", "Judoca");
        agregarSiNoExiste("generic.grupo", "Grupo", "Group", "Grupo");
        agregarSiNoExiste("generic.horario", "Horario", "Schedule", "Horário");
        agregarSiNoExiste("generic.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste("generic.inicio", "Inicio", "Start", "Início");
        agregarSiNoExiste("generic.fin", "Fin", "End", "Fim");
        agregarSiNoExiste("generic.fecha_inicio", "Fecha Inicio", "Start Date", "Data Início");
        agregarSiNoExiste("generic.fecha_fin", "Fecha Fin", "End Date", "Data Fim");
        agregarSiNoExiste("generic.pts", "pts", "pts", "pts");
        agregarSiNoExiste("generic.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste("generic.pagado", "Pagado", "Paid", "Pago");
        agregarSiNoExiste("generic.aspirante", "Aspirante", "Applicant", "Candidato");
        agregarSiNoExiste("generic.decision", "Decisión", "Decision", "Decisão");
        agregarSiNoExiste("generic.cinturon", "Cinturón", "Belt", "Cinturão");
        agregarSiNoExiste("generic.atleta", "Atleta", "Athlete", "Atleta");
        agregarSiNoExiste("generic.catalogo", "Catálogo", "Catalog", "Catálogo");
        agregarSiNoExiste("generic.celular", "Celular", "Mobile", "Celular");
        agregarSiNoExiste("generic.club_judo", "Club Judo", "Judo Club", "Clube de Judô");
        agregarSiNoExiste("generic.configuracion", "Configuración", "Configuration", "Configuração");
        agregarSiNoExiste("generic.deportista", "Deportista (Judoka)", "Athlete (Judoka)", "Atleta (Judoca)");
        agregarSiNoExiste("generic.deportista_adulto", "Deportista Adulto", "Adult Athlete", "Atleta Adulto");
        agregarSiNoExiste("generic.descripcion_opcional", "Descripción (opcional)", "Description (optional)", "Descrição (opcional)");
        agregarSiNoExiste("generic.enlace_acceso", "Enlace de Acceso Directo", "Direct Access Link", "Link de Acesso Direto");
        agregarSiNoExiste("generic.error.nombre_obligatorio", "El nombre es obligatorio", "The name is mandatory", "O nome é obrigatório");
        agregarSiNoExiste("generic.familia", "Familia", "Family", "Família");
        agregarSiNoExiste("generic.fechas", "Fechas", "Dates", "Datas");
        agregarSiNoExiste("generic.grupos", "Grupos", "Groups", "Grupos");
        agregarSiNoExiste("generic.grupos_plural", "Grupo(s)", "Group(s)", "Grupo(s)");
        agregarSiNoExiste("generic.hola", "Hola", "Hello", "Olá");
        agregarSiNoExiste("generic.id_dojo", "ID del Dojo", "Dojo ID", "ID do Dojo");
        agregarSiNoExiste("generic.insignia", "Insignia", "Badge", "Medalha");
        agregarSiNoExiste("generic.mas_informacion", "Más información", "More information", "Mais informações");
        agregarSiNoExiste("generic.menu", "Menú", "Menu", "Menu");
        agregarSiNoExiste("generic.ninguna", "Ninguna", "None", "Nenhuma");
        agregarSiNoExiste("generic.notas_opcional", "Notas (opcional)", "Notes (optional)", "Notas (opcional)");
        agregarSiNoExiste("generic.operador", "Operador", "Operator", "Operador");
        agregarSiNoExiste("generic.orden", "Orden", "Order", "Ordem");
        agregarSiNoExiste("generic.periodo", "Periodo", "Period", "Período");
        agregarSiNoExiste("generic.profesor_rafael", "el Profesor Rafael", "Professor Rafael", "o Professor Rafael");
        agregarSiNoExiste("generic.progreso", "Progreso", "Progress", "Progresso");
        agregarSiNoExiste("generic.sensei", "Sensei", "Sensei", "Sensei");
        agregarSiNoExiste("generic.sensei_cargo", "Sensei a Cargo", "Sensei in Charge", "Sensei Responsável");
        agregarSiNoExiste("generic.sin_fechas", "Sin fechas", "No dates", "Sem datas");
        agregarSiNoExiste("generic.sin_nombre", "Sin Nombre", "No Name", "Sem Nome");
        agregarSiNoExiste("generic.tipo_evento", "Tipo de Evento", "Event Type", "Tipo de Evento");
        agregarSiNoExiste("generic.todos", "Todos", "All", "Todos");
        agregarSiNoExiste("generic.torneo", "Torneo", "Tournament", "Torneio");
        agregarSiNoExiste("generic.valores", "Valores", "Values", "Valores");
        agregarSiNoExiste("generic.vas_a_registrar", "Vas a registrar", "You are going to register", "Você vai registrar");
        agregarSiNoExiste("generic.vence", "Vence", "Expires", "Vence");
        agregarSiNoExiste("generic.ver_detalles", "Ver Detalles", "View Details", "Ver Detalhes");
        agregarSiNoExiste("generic.indice", "Índice", "Index", "Índice");

        // ================== DASHBOARD ==================
        agregarSiNoExiste("dashboard.welcome", "Hola, {0}", "Hello, {0}", "Olá, {0}");
        agregarSiNoExiste("dashboard.titulo", "Panel de Control", "Dashboard", "Painel de Controle");
        agregarSiNoExiste("dashboard.boton.tomar_asistencia", "Tomar Asistencia", "Take Attendance", "Registrar Presença");
        agregarSiNoExiste("dashboard.btn.tareas", "Ir a Mis Tareas", "Go to My Tasks", "Ir para Minhas Tarefas");
        agregarSiNoExiste("dashboard.kpi.total_judokas", "Total Judokas", "Total Judokas", "Total de Judocas");
        agregarSiNoExiste("dashboard.kpi.grupos_activos", "Grupos Activos", "Active Groups", "Grupos Ativos");
        agregarSiNoExiste("dashboard.kpi.pruebas_hoy", "Para Hoy", "For Today", "Para Hoje");
        agregarSiNoExiste("dashboard.kpi.asistencia_promedio", "Asistencia Promedio", "Average Attendance", "Média de Presença");
        agregarSiNoExiste("dashboard.grafico.poder_combate_titulo", "Poder de Combate por Grupo", "Combat Power by Group", "Poder de Combate por Grupo");
        agregarSiNoExiste("dashboard.grafico.asistencia_30dias_titulo", "Asistencia últimos 30 días", "Last 30 days attendance", "Presença últimos 30 dias");
        agregarSiNoExiste("dashboard.grafico.promedio", "Promedio", "Average", "Média");
        agregarSiNoExiste("dashboard.grafico.asistencia_porcentaje", "Asistencia %", "Attendance %", "Presença %");
        agregarSiNoExiste("dashboard.mes1", "Mes 1", "Month 1", "Mês 1");
        agregarSiNoExiste("dashboard.mi_progreso", "Mi Progreso", "My Progress", "Meu Progresso");
        agregarSiNoExiste("dashboard.poder_oculto", "Poder Oculto", "Hidden Power", "Poder Oculto");
        agregarSiNoExiste("dashboard.camino_shodan", "Camino al Cinturón Negro (Shodan)", "Road to Black Belt (Shodan)", "Caminho para a Faixa Preta (Shodan)");
        agregarSiNoExiste("dashboard.alumnos.principal", "Alumnos Dojo Principal", "Main Dojo Students", "Alunos do Dojo Principal");
        agregarSiNoExiste("dashboard.navegando_progreso", "Navegando al progreso de", "Navigating to the progress of", "Navegando para o progresso de");

        // ================== CHART ==================
        agregarSiNoExiste("chart.radar.serie", "Nivel Actual", "Current Level", "Nível Atual");
        agregarSiNoExiste("chart.sin_datos", "Sin datos", "No data", "Sem dados");
        agregarSiNoExiste("chart.tu_progreso", "Tu Progreso", "Your Progress", "Seu Progresso");

        // ================== EMPTY ==================
        agregarSiNoExiste("empty.desc.realiza_pruebas", "Realiza pruebas para ver tu evolución.", "Take tests to see your progress.", "Realize testes para ver sua evolução.");

        // ================== TOOLTIP ==================
        agregarSiNoExiste("tooltip.trofeos", "Mis Trofeos e Insignias", "My Trophies & Badges", "Meus Troféus e Medalhas");
        agregarSiNoExiste("tooltip.palmares", "Mi Palmarés", "My Record", "Meu Histórico");
        agregarSiNoExiste("tooltip.cambiar.foto", "Cambiar foto de perfil", "Change profile photo", "Alterar foto de perfil");
        agregarSiNoExiste("tooltip.registro.permanente", "Registro permanente (No editable)", "Permanent record (Non-editable)", "Registro permanente (Não editável)");
        agregarSiNoExiste("tooltip.asignar.grupo", "Asignar a", "Assign to", "Atribuir a");
        agregarSiNoExiste("tooltip.remover.grupo", "Remover del grupo", "Remove from group", "Remover do grupo");

        // ================== PERFIL ==================
        agregarSiNoExiste("perfil.tab.resumen", "Resumen", "Summary", "Resumo");
        agregarSiNoExiste("perfil.tab.pruebas", "Pruebas", "Tests", "Testes");
        agregarSiNoExiste("perfil.tab.tareas", "Tareas", "Tasks", "Tarefas");
        agregarSiNoExiste("perfil.tab.insignias", "Insignias", "Badges", "Medalhas");
        agregarSiNoExiste("perfil.tab.palmares", "Palmarés", "Record", "Histórico");
        agregarSiNoExiste("perfil.tab.documentos", "Documentos", "Documents", "Documentos");
        agregarSiNoExiste("perfil.antropometria", "Antropometría histórica", "Historical anthropometry", "Antropometria histórica");
        agregarSiNoExiste("perfil.sin_datos", "Sin datos antropométricos", "No anthropometric data", "Sem dados antropométricos");
        agregarSiNoExiste("perfil.sin_resultados", "No hay resultados registrados", "No test results recorded", "Nenhum resultado registrado");
        agregarSiNoExiste("perfil.sin_tareas", "No hay tareas ejecutadas", "No tasks executed", "Nenhuma tarefa executada");
        agregarSiNoExiste("perfil.sin_palmares", "Sin participación en competiciones", "No competition record", "Sem participação em competições");
        agregarSiNoExiste("perfil.sin_documentos", "No hay documentos subidos", "No documents uploaded", "Nenhum documento enviado");
        agregarSiNoExiste("perfil.filtro.prueba", "Prueba", "Test", "Teste");
        agregarSiNoExiste("perfil.filtro.desde", "Desde", "From", "De");
        agregarSiNoExiste("perfil.filtro.hasta", "Hasta", "To", "Até");
        agregarSiNoExiste("perfil.filtrar", "Filtrar", "Filter", "Filtrar");
        agregarSiNoExiste("perfil.grid.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste("perfil.grid.prueba", "Prueba", "Test", "Teste");
        agregarSiNoExiste("perfil.grid.metrica", "Métrica", "Metric", "Métrica");
        agregarSiNoExiste("perfil.grid.valor", "Valor", "Value", "Valor");
        agregarSiNoExiste("perfil.grid.clasificacion", "Clasificación", "Classification", "Classificação");
        agregarSiNoExiste("perfil.grid.puntos", "Puntos", "Points", "Pontos");
        agregarSiNoExiste("perfil.grid.tarea", "Tarea", "Task", "Tarefa");
        agregarSiNoExiste("perfil.grid.completada", "Completada", "Completed", "Concluída");
        agregarSiNoExiste("perfil.grid.evento", "Evento", "Event", "Evento");
        agregarSiNoExiste("perfil.grid.lugar", "Lugar", "Place", "Local");
        agregarSiNoExiste("perfil.grid.resultado", "Resultado", "Result", "Resultado");
        agregarSiNoExiste("perfil.grid.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste("perfil.grid.archivo", "Archivo", "File", "Arquivo");
        agregarSiNoExiste("perfil.grid.accion", "Acción", "Action", "Ação");
        agregarSiNoExiste("perfil.ver", "Ver", "View", "Ver");
        agregarSiNoExiste("perfil.notas.titulo", "Bitácora de Reflexión", "Reflection Journal", "Diário de Reflexão");
        agregarSiNoExiste("perfil.notas.placeholder", "Escribe aquí tus pensamientos...", "Write your thoughts here...", "Escreva aqui seus pensamentos...");
        agregarSiNoExiste("perfil.msg.guardado", "Reflexión guardada", "Reflection saved", "Reflexão salva");
        agregarSiNoExiste("title.editar.reflexion", "Editar Reflexión", "Edit Reflection", "Editar Reflexão");
        agregarSiNoExiste("perfil.estatura_cm", "Estatura (cm)", "Height (cm)", "Altura (cm)");
        agregarSiNoExiste("perfil.imc", "IMC", "BMI", "IMC");
        agregarSiNoExiste("perfil.tu_grado", "Tu Grado Actual", "Your Current Rank", "Sua Graduação Atual");
        agregarSiNoExiste("perfil.anos_practica", "Años de Práctica", "Years of Practice", "Anos de Prática");
        agregarSiNoExiste("perfil.palmares_deportivo", "Palmarés Deportivo", "Sports Record", "Histórico Esportivo");
        agregarSiNoExiste("perfil.btn.guardar_revision", "Guardar Perfil y Enviar a Revisión", "Save Profile and Send for Review", "Salvar Perfil e Enviar para Revisão");

        // ================== VISTAS DE ADMINISTRACIÓN ==================
        agregarSiNoExiste("view.asignacion.titulo", "Asignación de Judokas a Grupos", "Judoka Group Assignment", "Atribuição de Judocas a Grupos");
        agregarSiNoExiste("view.gestion.usuarios.titulo", "Gestión de Usuarios", "User Management", "Gestão de Usuários");
        agregarSiNoExiste("view.judoka.plan.titulo", "Entrenamiento de Hoy", "Today's Training", "Treino de Hoje");

        // ================== RESULTADOS ==================
        agregarSiNoExiste("resultados.titulo", "Registro de Resultados", "Test Results Registration", "Registro de Resultados");
        agregarSiNoExiste("resultados.selector.judoka", "Seleccionar Judoka", "Select Judoka", "Selecionar Judoca");
        agregarSiNoExiste("resultados.grid.planes.header", "Planes de Evaluación", "Evaluation Plans", "Planos de Avaliação");
        agregarSiNoExiste("resultados.grid.pruebas.header", "Pruebas del Plan", "Plan Tests", "Testes do Plano");
        agregarSiNoExiste("resultados.feedback.inicio", "Resultados guardados: ", "Results saved: ", "Resultados salvos: ");
        agregarSiNoExiste("resultados.feedback.sjft", "Índice SJFT: %.2f (%s). ", "SJFT Index: %.2f (%s). ", "Índice SJFT: %.2f (%s). ");
        agregarSiNoExiste("resultados.feedback.prueba", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ");
        agregarSiNoExiste("resultados.error.guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste("resultados.sin_clasificacion", "Sin clasificación", "No classification", "Sem classificação");
        agregarSiNoExiste("resultados.sjft.error.faltan_datos", "Faltan datos para calcular el índice SJFT.", "Missing data to calculate SJFT index.", "Faltam dados para calcular o índice SJFT.");
        agregarSiNoExiste("resultados.sjft.error.total_cero", "El total de proyecciones no puede ser cero.", "Total projections cannot be zero.", "Total de projeções não pode ser zero.");
        agregarSiNoExiste("resultados.sjft.error.metrica_no_encontrada", "Métrica no encontrada.", "Metric not found.", "Métrica não encontrada.");
        agregarSiNoExiste("resultados.sjft.nota_automatica", "Índice SJFT calculado automáticamente.", "SJFT index automatically calculated.", "Índice SJFT calculado automaticamente.");
        agregarSiNoExiste("resultados.btn.guardar", "Guardar Resultados", "Save Results", "Salvar Resultados");

        // ================== ASISTENCIA ==================
        agregarSiNoExiste("asistencia.boton.cerrar_clase", "Cerrar Clase y Guardar", "Close Class and Save", "Fechar Aula e Salvar");
        agregarSiNoExiste("asistencia.boton.cerrar", "Cerrar", "Close", "Fechar");
        agregarSiNoExiste("asistencia.selector.grupo", "Selecciona el Grupo", "Select Group", "Selecione o Grupo");
        agregarSiNoExiste("asistencia.placeholder.grupo", "Ej: Infantiles Martes", "Ex: Kids Tuesday", "Ex: Infantis Terça");
        agregarSiNoExiste("asistencia.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste("asistencia.mensaje.sin_alumnos", "Este grupo no tiene alumnos asignados.", "This group has no assigned students.", "Este grupo não tem alunos atribuídos.");
        agregarSiNoExiste("asistencia.estado.ausente", "AUSENTE", "ABSENT", "AUSENTE");
        agregarSiNoExiste("asistencia.estado.presente", "PRESENTE", "PRESENT", "PRESENTE");
        agregarSiNoExiste("asistencia.notificacion.cargados", "Cargados", "Loaded", "Carregados");
        agregarSiNoExiste("asistencia.notificacion.alumnos", "alumnos", "students", "alunos");
        agregarSiNoExiste("asistencia.notificacion.registrada", "Asistencia registrada", "Attendance registered", "Presença registrada");
        agregarSiNoExiste("asistencia.notificacion.presentes", "Presentes", "Present", "Presentes");
        agregarSiNoExiste("asistencia.notificacion.error_guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste("asistencia.dialog.sos.titulo", "🚨 INFORMACIÓN DE EMERGENCIA", "🚨 EMERGENCY INFORMATION", "🚨 INFORMAÇÃO DE EMERGÊNCIA");
        agregarSiNoExiste("asistencia.dialog.sos.acudiente_movil", "Acudiente/Móvil", "Guardian/Mobile", "Responsável/Celular");
        agregarSiNoExiste("asistencia.dialog.sos.email", "Email", "Email", "E-mail");
        agregarSiNoExiste("asistencia.dialog.sos.eps", "EPS", "Health Insurance", "Convênio");
        agregarSiNoExiste("asistencia.dialog.sos.nombre_acudiente", "Nombre Acudiente", "Guardian Name", "Nome do Responsável");
        agregarSiNoExiste("asistencia.dialog.sos.llamar_ahora", "Llamar Ahora", "Call Now", "Ligar Agora");
        agregarSiNoExiste("asistencia.dialog.sos.sin_telefono", "Sin Teléfono Registrado", "No Phone Registered", "Sem Telefone Registrado");
        agregarSiNoExiste("asistencia.btn.pasar_lista_rapida", "Pasar Lista (Modo Rápido)", "Roll Call (Fast Mode)", "Fazer Chamada (Modo Rápido)");
        agregarSiNoExiste("asistencia.msg.lista_terminada", "¡Lista terminada!", "Roll call finished!", "Chamada concluída!");
        agregarSiNoExiste("asistencia.badge.asistio", "✅ Asististe", "✅ Attended", "✅ Compareceu");
        agregarSiNoExiste("asistencia.badge.no_asistio", "❌ No asististe", "❌ Did not attend", "❌ Não compareceu");
        agregarSiNoExiste("generic.badge.programada", "📅 Programada", "📅 Scheduled", "📅 Agendada");

        // ================== GAMIFICACIÓN ==================
        agregarSiNoExiste("gamificacion.titulo", "Reglas de Gamificación", "Gamification Rules", "Regras de Gamificação");
        agregarSiNoExiste("gamificacion.nueva_regla", "Nueva Regla", "New Rule", "Nova Regra");
        agregarSiNoExiste("gamificacion.msg.regla_guardada", "Regla guardada", "Rule saved", "Regra salva");
        agregarSiNoExiste("gamificacion.msg.regla_eliminada", "Regla eliminada", "Rule deleted", "Regra excluída");

        // ================== WIDGET MI DO ==================
        agregarSiNoExiste("widget.mido.titulo", "Mi Do (La Vía)", "My Do (The Way)", "Meu Do (O Caminho)");
        agregarSiNoExiste("widget.mido.shin", "SHIN (Mente)", "SHIN (Mind)", "SHIN (Mente)");
        agregarSiNoExiste("widget.mido.gi", "GI (Técnica)", "GI (Technique)", "GI (Técnica)");
        agregarSiNoExiste("widget.mido.tai", "TAI (Cuerpo)", "TAI (Body)", "TAI (Corpo)");
        agregarSiNoExiste("widget.mido.btn_catalogo", "Ver Catálogo", "View Catalog", "Ver Catálogo");
        agregarSiNoExiste("widget.mido.catalogo_titulo", "Salón de la Fama", "Hall of Fame", "Salão da Fama");
        agregarSiNoExiste("widget.mido.msg_inicio", "¡Tu camino comienza!", "Your journey begins!", "Sua jornada começa!");

        // ================== BADGES ==================
        agregarSiNoExiste("badge.estado.desbloqueada", "¡Insignia Desbloqueada!", "Badge Unlocked!", "Medalha Desbloqueada!");
        agregarSiNoExiste("badge.estado.bloqueada", "Insignia Bloqueada", "Badge Locked", "Medalha Bloqueada");
        agregarSiNoExiste("badge.estado.sin_datos", "Sin Datos", "No Data", "Sem Dados");
        agregarSiNoExiste("badge.label.obtenida", "Obtenida el", "Earned on", "Obtida em");
        agregarSiNoExiste("badge.label.pendiente", "Pendiente", "Pending", "Pendente");

        // ================== CHECK-IN ==================
        agregarSiNoExiste("checkin.titulo", "Control de Asistencia GPS", "GPS Attendance Control", "Controle de Presença GPS");
        agregarSiNoExiste("checkin.status.ready", "Listo para verificar ubicación.", "Ready to check location.", "Pronto para verificar localização.");
        agregarSiNoExiste("checkin.btn.marcar", "Marcar Asistencia", "Mark Attendance", "Registrar Presença");
        agregarSiNoExiste("checkin.status.locating", "Localizando...", "Locating...", "Localizando...");
        agregarSiNoExiste("checkin.status.requesting", "Solicitando permiso GPS...", "Requesting GPS permission...", "Solicitando permissão GPS...");
        agregarSiNoExiste("checkin.btn.retry", "Reintentar Check-in", "Retry Check-in", "Tentar Novamente");
        agregarSiNoExiste("checkin.error.denied", "Error GPS: Permiso denegado.", "GPS Error: Permission denied.", "Erro GPS: Permissão negada.");
        agregarSiNoExiste("checkin.error.browser", "ERROR: Debes habilitar el GPS.", "ERROR: You must enable GPS.", "ERRO: Você deve habilitar o GPS.");
        agregarSiNoExiste("checkin.status.validating", "Validando distancia...", "Validating distance...", "Validando distância...");
        agregarSiNoExiste("checkin.btn.success", "¡Asistencia Marcada!", "Attendance Marked!", "Presença Registrada!");
        agregarSiNoExiste("checkin.status.registered", "Te has registrado correctamente.", "You have registered correctly.", "Você se registrou corretamente.");
        agregarSiNoExiste("checkin.msg.oss", "¡Asistencia registrada! Oss.", "Attendance registered! Oss.", "Presença registrada! Oss.");
        agregarSiNoExiste("checkin.pase_qr", "Pase QR", "QR Pass", "Passe QR");

        // ================== GRUPOS ==================
        agregarSiNoExiste("grupos.titulo", "Gestión de Grupos", "Group Management", "Gestão de Grupos");
        agregarSiNoExiste("grupos.btn.nuevo", "Nuevo Grupo", "New Group", "Novo Grupo");
        agregarSiNoExiste("grupos.btn.aprobar", "Aprobar Grupo", "Approve Group", "Aprovar Grupo");
        agregarSiNoExiste("grupos.grid.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste("grupos.grid.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste("grupos.grid.miembros", "Miembros", "Members", "Membros");
        agregarSiNoExiste("grupos.label.alumnos", "alumnos", "students", "alunos");
        agregarSiNoExiste("grupos.tooltip.gestionar_miembros", "Gestionar Miembros", "Manage Members", "Gerenciar Membros");
        agregarSiNoExiste("grupos.dialog.miembros.titulo", "Miembros de", "Members of", "Membros de");
        agregarSiNoExiste("grupos.field.buscar_alumno", "Buscar alumno para agregar...", "Search student to add...", "Buscar aluno para adicionar...");
        agregarSiNoExiste("grupos.section.agregar", "Agregar Nuevo Miembro", "Add New Member", "Adicionar Novo Membro");
        agregarSiNoExiste("grupos.section.actuales", "Miembros Actuales", "Current Members", "Membros Atuais");
        agregarSiNoExiste("grupos.asignados", "Grupos Asignados", "Assigned Groups", "Grupos Atribuídos");

        // ================== CAMPENATOS ==================
        agregarSiNoExiste("campeonatos.titulo", "Gestión de Campeonatos", "Championship Management", "Gestão de Campeonatos");
        agregarSiNoExiste("campeonatos.btn.nueva_convocatoria", "Nueva Convocatoria", "New Call-up", "Nova Convocação");
        agregarSiNoExiste("campeonatos.grid.evento", "Evento", "Event", "Evento");
        agregarSiNoExiste("campeonatos.grid.resultado", "Resultado", "Result", "Resultado");
        agregarSiNoExiste("campeonatos.dialog.convocatoria.titulo", "Crear Convocatoria", "Create Call-up", "Criar Convocação");
        agregarSiNoExiste("campeonatos.field.nombre_evento", "Nombre del Evento", "Event Name", "Nome do Evento");
        agregarSiNoExiste("campeonatos.field.lugar", "Lugar", "Place", "Local");
        agregarSiNoExiste("campeonatos.field.nivel", "Nivel", "Level", "Nível");
        agregarSiNoExiste("campeonatos.field.seleccionar_atletas", "Seleccionar Atletas", "Select Athletes", "Selecionar Atletas");
        agregarSiNoExiste("campeonatos.msg.inscritos", "atletas inscritos exitosamente.", "athletes registered successfully.", "atletas inscritos com sucesso.");
        agregarSiNoExiste("campeonatos.dialog.resultado.titulo", "Resultado:", "Result:", "Resultado:");
        agregarSiNoExiste("campeonatos.field.medalla", "Medalla / Puesto", "Medal / Place", "Medalha / Posição");
        agregarSiNoExiste("campeonatos.field.link_video", "Link Video (YouTube)", "Video Link (YouTube)", "Link do Vídeo (YouTube)");
        agregarSiNoExiste("campeonatos.proximos", "Mis Próximos Campeonatos", "My Upcoming Championships", "Meus Próximos Campeonatos");

        // ================== CAMPOS ==================
        agregarSiNoExiste("campos.titulo", "Campos de Entrenamiento", "Training Camps", "Campos de Treinamento");
        agregarSiNoExiste("campos.btn.programar", "Programar Campo", "Schedule Camp", "Agendar Campo");
        agregarSiNoExiste("campos.grid.nombre", "Campo / Evento", "Camp / Event", "Campo / Evento");
        agregarSiNoExiste("campos.estado.en_curso", "En Curso", "In Progress", "Em Andamento");
        agregarSiNoExiste("campos.btn.certificar", "Certificar Cumplimiento", "Certify Compliance", "Certificar Cumprimento");
        agregarSiNoExiste("campos.dialog.programar.titulo", "Programar Campo", "Schedule Camp", "Agendar Campo");
        agregarSiNoExiste("campos.field.nombre", "Nombre del Campo", "Camp Name", "Nome do Campo");
        agregarSiNoExiste("campos.placeholder.ej_campamento", "Ej: Campamento de Altura", "Ex: Altitude Camp", "Ex: Acampamento de Altitude");
        agregarSiNoExiste("campos.field.lugar", "Ubicación", "Location", "Localização");
        agregarSiNoExiste("campos.field.objetivo", "Enfoque / Objetivo", "Focus / Goal", "Foco / Objetivo");
        agregarSiNoExiste("campos.placeholder.ej_tactico", "Ej: Táctico Competitivo", "Ex: Competitive Tactical", "Ex: Tático Competitivo");
        agregarSiNoExiste("campos.field.convocados", "Convocados", "Invited", "Convocados");
        agregarSiNoExiste("campos.msg.programado", "Campo programado para", "Camp scheduled for", "Campo agendado para");
        agregarSiNoExiste("campos.dialog.certificar.titulo", "Certificar:", "Certify:", "Certificar:");
        agregarSiNoExiste("campos.label.pregunta_cumplimiento", "¿El judoka completó satisfactoriamente el campo?", "Did the judoka complete the camp successfully?", "O judoca completou o campo satisfatoriamente?");
        agregarSiNoExiste("campos.field.puntos_ascenso", "Puntos de Ascenso", "Promotion Points", "Pontos de Promoção");
        agregarSiNoExiste("campos.btn.confirmar_puntos", "Certificar y Otorgar Puntos", "Certify and Award Points", "Certificar e Atribuir Pontos");

        // ================== INVENTARIO ==================
        agregarSiNoExiste("inventario.titulo", "Tienda del Dojo", "Dojo Store", "Loja do Dojo");
        agregarSiNoExiste("inventario.btn.nuevo", "Nuevo Producto", "New Product", "Novo Produto");
        agregarSiNoExiste("inventario.grid.articulo", "Artículo", "Item", "Artigo");
        agregarSiNoExiste("inventario.grid.stock", "Stock", "Stock", "Estoque");
        agregarSiNoExiste("inventario.grid.venta", "Precio Venta", "Selling Price", "Preço Venda");
        agregarSiNoExiste("inventario.grid.costo", "Costo", "Cost", "Custo");
        agregarSiNoExiste("inventario.status.agotado", "AGOTADO", "OUT OF STOCK", "ESGOTADO");
        agregarSiNoExiste("inventario.tooltip.add_stock", "Agregar Stock", "Add Stock", "Adicionar Estoque");
        agregarSiNoExiste("inventario.dialog.venta", "Registrar Venta", "Register Sale", "Registrar Venda");
        agregarSiNoExiste("inventario.msg.venta_ok", "Venta registrada y descontada del inventario", "Sale registered and deducted from inventory", "Venda registrada e deduzida do estoque");
        agregarSiNoExiste("inventario.dialog.stock", "Reabastecer Stock", "Restock", "Reabastecer Estoque");
        agregarSiNoExiste("inventario.field.cantidad_ingreso", "Cantidad a Ingresar", "Quantity to Add", "Quantidade a Adicionar");
        agregarSiNoExiste("inventario.dialog.nuevo", "Nuevo Producto", "New Product", "Novo Produto");
        agregarSiNoExiste("inventario.dialog.editar", "Editar Producto", "Edit Product", "Editar Produto");
        agregarSiNoExiste("inventario.field.costo", "Costo Compra ($)", "Purchase Cost ($)", "Custo Compra ($)");
        agregarSiNoExiste("inventario.field.precio", "Precio Venta ($)", "Selling Price ($)", "Preço Venda ($)");
        agregarSiNoExiste("inventario.field.stock_inicial", "Stock Inicial", "Initial Stock", "Estoque Inicial");

        // ================== TESORERÍA ==================
        agregarSiNoExiste("tesoreria.titulo", "Gestión Financiera", "Financial Management", "Gestão Financeira");
        agregarSiNoExiste("tesoreria.tab.registrar_ingreso", "Registrar Ingreso", "Register Income", "Registrar Receita");
        agregarSiNoExiste("tesoreria.tab.registrar_gasto", "Registrar Gasto", "Register Expense", "Registrar Despesa");
        agregarSiNoExiste("tesoreria.tab.balance_reportes", "Balance y Reportes", "Balance and Reports", "Balanço e Relatórios");
        agregarSiNoExiste("tesoreria.alumno", "Alumno", "Student", "Aluno");
        agregarSiNoExiste("tesoreria.concepto", "Concepto", "Concept", "Conceito");
        agregarSiNoExiste("tesoreria.valor", "Valor ($)", "Amount ($)", "Valor ($)");
        agregarSiNoExiste("tesoreria.valor_pagado", "Valor Pagado ($)", "Amount Paid ($)", "Valor Pago ($)");
        agregarSiNoExiste("tesoreria.metodo_pago", "Método de Pago", "Payment Method", "Método de Pagamento");
        agregarSiNoExiste("tesoreria.observacion", "Observación", "Observation", "Observação");
        agregarSiNoExiste("tesoreria.categoria_gasto", "Categoría de Gasto", "Expense Category", "Categoria de Despesa");
        agregarSiNoExiste("tesoreria.detalle_proveedor", "Detalle / Proveedor", "Detail / Supplier", "Detalhe / Fornecedor");
        agregarSiNoExiste("tesoreria.foto_factura", "Foto de Factura", "Invoice Photo", "Foto da Fatura");
        agregarSiNoExiste("tesoreria.soporte", "Soporte", "Support", "Suporte");
        agregarSiNoExiste("tesoreria.boton.registrar_generar_recibo", "Registrar y Generar Recibo", "Register and Generate Receipt", "Registrar e Gerar Recibo");
        agregarSiNoExiste("tesoreria.boton.registrar_salida", "Registrar Salida", "Register Outflow", "Registrar Saída");
        agregarSiNoExiste("tesoreria.boton.guardar", "Guardar", "Save", "Salvar");
        agregarSiNoExiste("tesoreria.kpi.ingresos_mes", "Ingresos Mes", "Monthly Income", "Receitas do Mês");
        agregarSiNoExiste("tesoreria.kpi.egresos_mes", "Egresos Mes", "Monthly Expenses", "Despesas do Mês");
        agregarSiNoExiste("tesoreria.kpi.balance", "Balance", "Balance", "Balanço");
        agregarSiNoExiste("tesoreria.grid.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste("tesoreria.grid.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste("tesoreria.grid.concepto", "Concepto", "Concept", "Conceito");
        agregarSiNoExiste("tesoreria.grid.monto", "Monto", "Amount", "Valor");
        agregarSiNoExiste("tesoreria.grid.judoka", "Judoka", "Judoka", "Judoca");
        agregarSiNoExiste("tesoreria.grid.soporte", "Soporte", "Support", "Suporte");
        agregarSiNoExiste("tesoreria.dialog.nuevo_concepto.titulo", "Nuevo Concepto de", "New Concept for", "Novo Conceito de");
        agregarSiNoExiste("tesoreria.dialog.nuevo_concepto.nombre", "Nombre del Concepto", "Concept Name", "Nome do Conceito");
        agregarSiNoExiste("tesoreria.dialog.nuevo_concepto.valor_sugerido", "Valor Sugerido", "Suggested Value", "Valor Sugerido");
        agregarSiNoExiste("tesoreria.validacion.concepto_monto", "Concepto y Monto son obligatorios", "Concept and Amount are required", "Conceito e Valor são obrigatórios");
        agregarSiNoExiste("tesoreria.validacion.categoria_monto", "Categoría y Monto obligatorios", "Category and Amount are required", "Categoria e Valor são obrigatórios");
        agregarSiNoExiste("tesoreria.notificacion.ingreso_exitoso", "Ingreso registrado con éxito", "Income registered successfully", "Receita registrada com sucesso");
        agregarSiNoExiste("tesoreria.notificacion.soporte_cargado", "Soporte cargado", "Support uploaded", "Suporte carregado");
        agregarSiNoExiste("tesoreria.notificacion.error_subir", "Error al subir: ", "Error uploading: ", "Erro ao enviar: ");
        agregarSiNoExiste("tesoreria.notificacion.gasto_registrado", "Gasto registrado", "Expense registered", "Despesa registrada");
        agregarSiNoExiste("tesoreria.notificacion.concepto_creado", "Concepto Creado", "Concept Created", "Conceito Criado");
        agregarSiNoExiste("finanzas.beneficiario", "Beneficiario", "Beneficiary", "Beneficiário");
        agregarSiNoExiste("finanzas.msg.nequi_cargado", "Comprobante Nequi cargado con éxito", "Nequi receipt uploaded successfully", "Comprovante Nequi enviado com sucesso");
        agregarSiNoExiste("finanzas.compromisos_pendientes", "Compromisos Pendientes", "Pending Commitments", "Compromissos Pendentes");
        agregarSiNoExiste("finanzas.btn.confirmar_pago", "Confirmar Pago", "Confirm Payment", "Confirmar Pagamento");
        agregarSiNoExiste("finanzas.codigo_aprobacion_nequi", "Código de Aprobación (Solo Nequi)", "Approval Code (Nequi only)", "Código de Aprovação (Apenas Nequi)");
        agregarSiNoExiste("finanzas.msg.una_transferencia", "Realiza UNA SOLA transferencia por", "Make ONLY ONE transfer for", "Faça APENAS UMA transferência para");
        agregarSiNoExiste("finanzas.referencia_nequi", "Referencia Nequi o URL del Comprobante", "Nequi Reference or Receipt URL", "Referência Nequi ou URL do Comprovante");
        agregarSiNoExiste("finanzas.btn.enviar_soporte", "Enviar Soporte", "Send Support", "Enviar Suporte");
        agregarSiNoExiste("finanzas.estado_cuenta", "Estado de Cuenta", "Account Status", "Status da Conta");
        agregarSiNoExiste("finanzas.pago_pendiente", "Pago Pendiente", "Pending Payment", "Pagamento Pendente");
        agregarSiNoExiste("finanzas.pendiente_pago", "Pendiente de Pago", "Pending Payment", "Pendente de Pagamento");
        agregarSiNoExiste("finanzas.total_pagar", "Total a Pagar", "Total to Pay", "Total a Pagar");
        agregarSiNoExiste("finanzas.btn.subir_nequi", "Subir Pantallazo Nequi", "Upload Nequi Screenshot", "Enviar Captura de Tela Nequi");
        agregarSiNoExiste("finanzas.btn.ver_recibo_nequi", "Ver Recibo Nequi", "View Nequi Receipt", "Ver Recibo Nequi");
        agregarSiNoExiste("finanzas.msg.comprobante_exito", "¡Comprobante subido con éxito!", "Receipt uploaded successfully!", "Comprovante enviado com sucesso!");
        agregarSiNoExiste("finanzas.msg.grupo_familiar_activado", "¡Grupo familiar activado y finanzas procesadas exitosamente!", "Family group activated and finances processed successfully!", "Grupo familiar ativado e finanças processadas com sucesso!");

        // ================== ADMISIONES ==================
        agregarSiNoExiste("admisiones.titulo", "Validación de Ingresos", "Admission Validation", "Validação de Ingressos");
        agregarSiNoExiste("admisiones.descripcion", "Revise los documentos y pagos de los aspirantes.", "Review applicants' documents and payments.", "Revise os documentos e pagamentos dos candidatos.");
        agregarSiNoExiste("admisiones.grid.registrado", "Registrado", "Registered", "Registrado");
        agregarSiNoExiste("admisiones.grid.documentos", "Documentos", "Documents", "Documentos");
        agregarSiNoExiste("admisiones.grid.pago", "Pago Matrícula", "Enrollment Payment", "Pagamento Matrícula");
        agregarSiNoExiste("admisiones.btn.marcar_pago", "Marcar Pago Manual", "Mark Manual Payment", "Marcar Pagamento Manual");
        agregarSiNoExiste("admisiones.msg.activado", "¡Judoka activado con éxito!", "Judoka activated successfully!", "Judoca ativado com sucesso!");
        agregarSiNoExiste("admisiones.msg.rechazado", "Aspirante rechazado.", "Applicant rejected.", "Candidato rejeitado.");
        agregarSiNoExiste("admisiones.btn.anadir_deportista", "Añadir otro deportista", "Add another athlete", "Adicionar outro atleta");
        agregarSiNoExiste("admisiones.en_revision_auditoria", "En Revisión (Auditoría)", "Under Review (Audit)", "Em Revisão (Auditoria)");
        agregarSiNoExiste("admisiones.rechazado_master", "Rechazado por el Master", "Rejected by Master", "Rejeitado pelo Master");
        agregarSiNoExiste("admisiones.msg.disponible_aprobacion", "Disponible cuando el Master apruebe el ingreso", "Available when the Master approves entry", "Disponível quando o Master aprovar a entrada");
        agregarSiNoExiste("admisiones.btn.subir_eps", "Subir EPS", "Upload Health Insurance", "Enviar Comprovante de Convênio");
        agregarSiNoExiste("admisiones.btn.subir_waiver", "Subir Waiver firmado", "Upload signed Waiver", "Enviar Waiver assinado");

        // ================== ERRORES ==================
        agregarSiNoExiste("error.generic", "Ha ocurrido un error", "An error occurred", "Ocorreu um erro");
        agregarSiNoExiste("error.upload", "Error al subir archivo", "Error uploading file", "Erro ao enviar arquivo");
        agregarSiNoExiste("error.campos_obligatorios", "Campos obligatorios incompletos", "Required fields missing", "Campos obrigatórios incompletos");
        agregarSiNoExiste("error.campos_incompletos", "Por favor, llene todos los campos", "Please fill all fields", "Por favor, preencha todos os campos");
        agregarSiNoExiste("error.contrasenas_no_coinciden", "Las contraseñas no coinciden", "Passwords do not match", "As senhas não coincidem");
        agregarSiNoExiste("error.usuario.existe", "Este correo ya está registrado", "This email is already registered", "Este e-mail já está registrado");
        agregarSiNoExiste("error.judoka_no_autenticado", "Judoka no autenticado", "Unauthenticated Judoka", "Judoca não autenticado");
        agregarSiNoExiste("error.judoka_no_encontrado", "Judoka no encontrado", "Judoka not found", "Judoca não encontrado");
        agregarSiNoExiste("error.no_judoka_autenticado", "No hay judoka autenticado", "No authenticated judoka", "Nenhum judoca autenticado");
        agregarSiNoExiste("error.sin_permiso_perfil", "No tienes permiso para ver este perfil", "You do not have permission to view this profile", "Você não tem permissão para ver este perfil");
        agregarSiNoExiste("error.envio", "Error en el envío", "Sending error", "Erro no envio");

        // ================== MENSAJES DE ÉXITO ==================
        agregarSiNoExiste("msg.success.saved", "Guardado exitosamente", "Saved successfully", "Salvo com sucesso");
        agregarSiNoExiste("msg.success.updated", "Actualizado exitosamente", "Updated successfully", "Atualizado com sucesso");
        agregarSiNoExiste("msg.success.deleted", "Eliminado exitosamente", "Deleted successfully", "Excluído com sucesso");
        agregarSiNoExiste("msg.success.config_saved", "Configuración guardada correctamente", "Configuration saved successfully", "Configuração salva com sucesso");
        agregarSiNoExiste("msg.foto.actualizada", "Foto actualizada correctamente", "Photo updated successfully", "Foto atualizada com sucesso");
        agregarSiNoExiste("msg.diario.vacio", "Tu diario está vacío. Empieza hoy.", "Your diary is empty. Start today.", "Seu diário está vazio. Comece hoje.");
        agregarSiNoExiste("msg.entrada.actualizada", "Entrada actualizada.", "Entry updated.", "Entrada atualizada.");
        agregarSiNoExiste("msg.exito.archivo_subido", "Documento guardado correctamente", "Document saved successfully", "Documento salvo com sucesso");
        agregarSiNoExiste("msg.exito.puede_continuar", "¡Excelente! Ya puedes finalizar", "Great! You can now finish", "Ótimo! Você pode finalizar agora");
        agregarSiNoExiste("msg.error.nube", "Error al conectar con la Nube", "Error connecting to the Cloud", "Erro ao conectar com a Nuvem");
        agregarSiNoExiste("msg.excelente.trabajo", "¡Excelente trabajo!", "Excellent work!", "Excelente trabalho!");
        agregarSiNoExiste("msg.error.guardar", "Error al guardar", "Error saving", "Erro ao salvar");
        agregarSiNoExiste("msg.dia.descanso", "Hoy es día de descanso. ¡Recupérate!", "Today is a rest day. Recover!", "Hoje é dia de descanso. Recupere-se!");
        agregarSiNoExiste("msg.entrenamiento.finalizado", "¡Entrenamiento del día finalizado!", "Daily training finished!", "Treino do dia finalizado!");
        agregarSiNoExiste("msg.error.asignacion", "Error al asignar", "Error assigning", "Erro ao atribuir");
        agregarSiNoExiste("msg.exito.asignacion", "Judoka asignado correctamente", "Judoka assigned successfully", "Judoca atribuído com sucesso");
        agregarSiNoExiste("msg.error.remocion", "Error al remover", "Error removing", "Erro ao remover");
        agregarSiNoExiste("msg.exito.remocion", "Judoka removido del grupo", "Judoka removed from group", "Judoca removido do grupo");
        agregarSiNoExiste("msg.selecciona.categoria.para.comparar", "Selecciona una categoría arriba para ver tu evolución.", "Select a category above to see your progress.", "Selecione uma categoria acima para ver sua evolução.");
        agregarSiNoExiste("msg.enlace_copiado", "Enlace copiado al portapapeles", "Link copied to clipboard", "Link copiado para a área de transferência");
        agregarSiNoExiste("msg.configurado_exito", "¡Configurado con éxito!", "Configured successfully!", "Configurado com sucesso!");
        agregarSiNoExiste("msg.subido_exito", "¡Subido con éxito!", "Uploaded successfully!", "Enviado com sucesso!");
        agregarSiNoExiste("msg.bienvenido_a", "¡Bienvenido a", "Welcome to", "Bem-vindo a");
        agregarSiNoExiste("msg.bienvenido_academia", "¡Bienvenido a la Academia!", "Welcome to the Academy!", "Bem-vindo à Academia!");
        agregarSiNoExiste("msg.bienvenido_saas", "¡Bienvenido a tu nuevo SaaS!", "Welcome to your new SaaS!", "Bem-vindo ao seu novo SaaS!");
        agregarSiNoExiste("msg.bienvenido_dojo", "¡Bienvenido al Dojo!", "Welcome to the Dojo!", "Bem-vindo ao Dojo!");
        agregarSiNoExiste("generic.hola_exclamacion", "¡Hola", "Hello", "Olá");
        agregarSiNoExiste("generic.msg.recibido", "¡Recibido!", "Received!", "Recebido!");

        // ================== REGISTRO / LOGIN ==================
        agregarSiNoExiste("registro.titulo", "Registro de Aspirante", "Applicant Registration", "Registro de Candidato");
        agregarSiNoExiste("registro.subtitulo", "Únete a nuestro Dojo", "Join our Dojo", "Junte-se ao nosso Dojo");
        agregarSiNoExiste("registro.btn.siguiente", "Siguiente", "Next", "Próximo");
        agregarSiNoExiste("registro.btn.volver", "Ya tengo cuenta", "I already have an account", "Já tenho conta");
        agregarSiNoExiste("registro.exito", "Registro Exitoso. Inicia Sesión.", "Registration successful. Log in.", "Registro bem-sucedido. Faça login.");
        agregarSiNoExiste("registro.email_usuario", "Email (Será su Usuario)", "Email (Will be your Username)", "E-mail (Será seu Usuário)");
        agregarSiNoExiste("registro.email", "Email (Usuario)", "Email (Username)", "E-mail (Usuário)");
        agregarSiNoExiste("registro.crear_dojo", "Crear mi Dojo", "Create my Dojo", "Criar meu Dojo");
        agregarSiNoExiste("registro.crear_cuenta", "Crear una Cuenta", "Create an Account", "Criar uma Conta");
        agregarSiNoExiste("registro.crear_contrasena", "Crea tu Contraseña", "Create your Password", "Crie sua Senha");
        agregarSiNoExiste("registro.profesor_nuevo_dojo", "Profesor de Judo (Nuevo Dojo)", "Judo Teacher (New Dojo)", "Professor de Judô (Novo Dojo)");
        agregarSiNoExiste("registro.tu_profesor_judo", "tu Profesor de Judo", "your Judo Teacher", "seu Professor de Judô");
        agregarSiNoExiste("login.btn.registrar", "¿No tienes cuenta? Regístrate aquí.", "Don't have an account? Register here.", "Não tem conta? Registre-se aqui.");
        agregarSiNoExiste("login.form.titulo", "Iniciar Sesión", "Sign In", "Entrar");
        agregarSiNoExiste("login.lbl.usuario", "Usuario", "Username", "Usuário");
        agregarSiNoExiste("login.lbl.password", "Contraseña", "Password", "Senha");
        agregarSiNoExiste("login.btn.ingresar", "Entrar", "Log in", "Entrar");
        agregarSiNoExiste("login.link.olvido", "¿Olvidaste tu contraseña?", "Forgot password?", "Esqueceu a senha?");
        agregarSiNoExiste("login.error.titulo", "Usuario o contraseña incorrectos", "Incorrect username or password", "Usuário ou senha incorretos");
        agregarSiNoExiste("login.error.mensaje", "Por favor, verifica tus credenciales.", "Please check your credentials.", "Por favor, verifique suas credenciais.");
        agregarSiNoExiste("login.btn.ir_inicio", "Ir al inicio de sesión", "Go to login", "Ir para o login");

        // ================== LABELS ==================
        agregarSiNoExiste("label.fecha_nacimiento", "Fecha de Nacimiento", "Birth Date", "Data de Nascimento");
        agregarSiNoExiste("label.peso_kg", "Peso (kg)", "Weight (kg)", "Peso (kg)");
        agregarSiNoExiste("label.contrasena", "Contraseña", "Password", "Senha");
        agregarSiNoExiste("label.confirmar_contrasena", "Confirmar Contraseña", "Confirm Password", "Confirmar Senha");

        // ================== BIBLIOTECA ==================
        agregarSiNoExiste("biblioteca.titulo", "Biblioteca de Tareas Diarias", "Daily Tasks Library", "Biblioteca de Tarefas Diárias");
        agregarSiNoExiste("biblioteca.boton.nueva_tarea", "Añadir Nueva Tarea", "Add New Task", "Adicionar Nova Tarefa");
        agregarSiNoExiste("biblioteca.grid.nombre_tarea", "Nombre Tarea", "Task Name", "Nome da Tarefa");
        agregarSiNoExiste("biblioteca.grid.meta", "Meta", "Goal", "Meta");
        agregarSiNoExiste("biblioteca.grid.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste("biblioteca.grid.video", "Video", "Video", "Vídeo");
        agregarSiNoExiste("biblioteca.grid.tooltip.tiene_video", "Tiene video", "Has video", "Tem vídeo");
        agregarSiNoExiste("biblioteca.grid.tooltip.editar_tarea", "Editar tarea", "Edit task", "Editar tarefa");
        agregarSiNoExiste("biblioteca.grid.acciones", "Acciones", "Actions", "Ações");
        agregarSiNoExiste("biblioteca.error.sensei_no_autenticado", "Sensei no autenticado", "Sensei not authenticated", "Sensei não autenticado");
        agregarSiNoExiste("biblioteca.notificacion.tarea_guardada", "Tarea guardada: %s", "Task saved: %s", "Tarefa salva: %s");
        agregarSiNoExiste("biblioteca.notificacion.error_guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste("biblioteca.descripcion_ejecucion", "Descripción (¿Cómo se ejecuta?)", "Description (How is it executed?)", "Descrição (Como é executado?)");
        agregarSiNoExiste("biblioteca.error.nombre_tarea", "El nombre de la tarea es obligatorio", "The task name is mandatory", "O nome da tarefa é obrigatório");

        // ================== COMUNIDAD ==================
        agregarSiNoExiste("comunidad.tab.muro", "Muro del Dojo", "Dojo Wall", "Mural do Dojo");
        agregarSiNoExiste("comunidad.tab.chat", "Chat Grupal", "Group Chat", "Chat em Grupo");
        agregarSiNoExiste("comunidad.post.placeholder", "Comparte algo con el dojo...", "Share something with the dojo...", "Compartilhe algo com o dojo...");
        agregarSiNoExiste("comunidad.btn.subir_foto", "Subir Foto/Video", "Upload Photo/Video", "Enviar Foto/Vídeo");
        agregarSiNoExiste("comunidad.label.drop", "Arrastra archivos aquí...", "Drag files here...", "Arraste arquivos aqui...");
        agregarSiNoExiste("comunidad.btn.publicar", "Publicar", "Post", "Publicar");
        agregarSiNoExiste("comunidad.msg.publicado", "¡Publicado en el muro!", "Posted on the wall!", "Publicado no mural!");
        agregarSiNoExiste("comunidad.msg.archivo_listo", "Archivo listo", "File ready", "Arquivo pronto");
        agregarSiNoExiste("comunidad.warn.empty_post", "Escribe algo o sube una foto", "Write something or upload a photo", "Escreva algo ou envie uma foto");
        agregarSiNoExiste("comunidad.btn.comentar", "Comentar", "Comment", "Comentar");
        agregarSiNoExiste("comunidad.comment.placeholder", "Escribe una respuesta...", "Write a reply...", "Escreva uma resposta...");
        agregarSiNoExiste("comunidad.msg.comment_sent", "Comentario enviado", "Comment sent", "Comentário enviado");
        agregarSiNoExiste("comunidad.label.image_of", "Imagen de", "Image of", "Imagem de");
        agregarSiNoExiste("comunidad.chat.escribir", "Escribe un mensaje...", "Type a message...", "Digite uma mensagem...");
        agregarSiNoExiste("comunidad.chat.enviar", "Enviar", "Send", "Enviar");
        agregarSiNoExiste("comunidad.imagen_post", "Imagen Post", "Post Image", "Imagem do Post");

        // ================== AYUDA ==================
        agregarSiNoExiste("help.poder_combate.titulo", "¿Qué es el Poder de Combate?", "What is Combat Power?", "O que é Poder de Combate?");
        agregarSiNoExiste("help.poder_combate.contenido",
                "El Poder de Combate es un indicador global (1000-5000) que resume tu rendimiento físico basado en las pruebas evaluadas. Se calcula a partir de las clasificaciones (Excelente, Bueno, etc.) de cada prueba, agrupadas en los 5 bloques de Agudelo. El radar muestra tu nivel actual en cada bloque (de 1 a 5 estrellas). Las líneas de motivador en las gráficas representan el siguiente escalón a alcanzar según normas científicas (PROESP/CBJ) o el récord del dojo.",
                "Combat Power is a global indicator (1000-5000) that summarizes your physical performance based on evaluated tests. It is calculated from the classifications (Excellent, Good, etc.) of each test, grouped into Agudelo's 5 blocks. The radar shows your current level in each block (1 to 5 stars). The motivator lines in the charts represent the next step to achieve according to scientific norms (PROESP/CBJ) or the dojo record.",
                "O Poder de Combate é um indicador global (1000-5000) que resume seu desempenho físico com base nos testes avaliados. É calculado a partir das classificações (Excelente, Bom, etc.) de cada teste, agrupadas nos 5 blocos de Agudelo. O radar mostra seu nível atual em cada bloco (de 1 a 5 estrelas). As linhas motivadoras nos gráficos representam o próximo degrau a alcançar de acordo com normas científicas (PROESP/CBJ) ou o recorde do dojo.");
        agregarSiNoExiste("sabiduria.titulo", "Sabiduría del Sensei", "Sensei's Wisdom", "Sabedoria do Sensei");

        // ================== ENUMS (YA DEBERÍAN ESTAR, PERO LOS DEJAMOS POR SI ACASO) ==================
        // Grados de cinturón
        agregarSiNoExiste("enum.gradocinturon.blanco", "Blanco", "White", "Branco");
        agregarSiNoExiste("enum.gradocinturon.amarillo", "Amarillo", "Yellow", "Amarelo");
        agregarSiNoExiste("enum.gradocinturon.naranja", "Naranja", "Orange", "Laranja");
        agregarSiNoExiste("enum.gradocinturon.verde", "Verde", "Green", "Verde");
        agregarSiNoExiste("enum.gradocinturon.azul", "Azul", "Blue", "Azul");
        agregarSiNoExiste("enum.gradocinturon.marron", "Marrón", "Brown", "Marrom");
        agregarSiNoExiste("enum.gradocinturon.negro_1_dan", "Negro 1 Dan", "Black 1st Dan", "Preto 1 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_2_dan", "Negro 2 Dan", "Black 2nd Dan", "Preto 2 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_3_dan", "Negro 3 Dan", "Black 3rd Dan", "Preto 3 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_4_dan", "Negro 4 Dan", "Black 4th Dan", "Preto 4 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_5_dan", "Negro 5 Dan", "Black 5th Dan", "Preto 5 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_6_dan", "Negro 6 Dan", "Black 6th Dan", "Preto 6 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_7_dan", "Negro 7 Dan", "Black 7th Dan", "Preto 7 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_8_dan", "Negro 8 Dan", "Black 8th Dan", "Preto 8 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_9_dan", "Negro 9 Dan", "Black 9th Dan", "Preto 9 Dan");
        agregarSiNoExiste("enum.gradocinturon.negro_10_dan", "Negro 10 Dan", "Black 10th Dan", "Preto 10 Dan");

        // Estado judoka
        agregarSiNoExiste("enum.estadojudoka.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste("enum.estadojudoka.en_revision", "En revisión", "Under review", "Em revisão");
        agregarSiNoExiste("enum.estadojudoka.activo", "Activo", "Active", "Ativo");
        agregarSiNoExiste("enum.estadojudoka.inactivo", "Inactivo", "Inactive", "Inativo");
        agregarSiNoExiste("enum.estadojudoka.rechazado", "Rechazado", "Rejected", "Rejeitado");

        // Tipo documento
        agregarSiNoExiste("enum.tipodocumento.waiver", "Exoneración", "Waiver", "Termo de responsabilidade");
        agregarSiNoExiste("enum.tipodocumento.certificado_medico", "Certificado médico", "Medical certificate", "Atestado médico");
        agregarSiNoExiste("enum.tipodocumento.eps", "EPS", "EPS", "EPS");
        agregarSiNoExiste("enum.tipodocumento.documento_identidad", "Documento de identidad", "Identity document", "Documento de identidade");
        agregarSiNoExiste("enum.tipodocumento.comprobante_pago", "Comprobante de pago", "Payment receipt", "Comprovante de pagamento");
        agregarSiNoExiste("enum.tipodocumento.otro", "Otro", "Other", "Outro");

        // Tipo transacción
        agregarSiNoExiste("enum.tipotransaccion.ingreso", "Ingreso", "Income", "Receita");
        agregarSiNoExiste("enum.tipotransaccion.egreso", "Egreso", "Expense", "Despesa");

        // Nivel organizacional
        agregarSiNoExiste("enum.nivelorganizacional.club", "Club", "Club", "Clube");
        agregarSiNoExiste("enum.nivelorganizacional.liga", "Liga", "League", "Liga");
        agregarSiNoExiste("enum.nivelorganizacional.federacion", "Federación", "Federation", "Federação");

        // Estado microciclo
        agregarSiNoExiste("enum.estadomicrociclo.activo", "Activo", "Active", "Ativo");
        agregarSiNoExiste("enum.estadomicrociclo.completado", "Completado", "Completed", "Concluído");
        agregarSiNoExiste("enum.estadomicrociclo.cancelado", "Cancelado", "Canceled", "Cancelado");
        agregarSiNoExiste("enum.estadomicrociclo.borrador", "Borrador", "Draft", "Rascunho");

        // Categoría ejercicio
        agregarSiNoExiste("enum.categoriaejercicio.medicion_antropometrica", "Medición antropométrica", "Anthropometric measurement", "Medição antropométrica");
        agregarSiNoExiste("enum.categoriaejercicio.potencia", "Potencia", "Power", "Potência");
        agregarSiNoExiste("enum.categoriaejercicio.velocidad", "Velocidad", "Speed", "Velocidade");
        agregarSiNoExiste("enum.categoriaejercicio.resistencia_dinamica", "Resistencia dinámica", "Dynamic endurance", "Resistência dinâmica");
        agregarSiNoExiste("enum.categoriaejercicio.resistencia_muscular_localizada", "Resistencia muscular localizada", "Localized muscular endurance", "Resistência muscular localizada");
        agregarSiNoExiste("enum.categoriaejercicio.resistencia_isometrica", "Resistencia isométrica", "Isometric endurance", "Resistência isométrica");
        agregarSiNoExiste("enum.categoriaejercicio.aptitud_anaerobica", "Aptitud anaeróbica", "Anaerobic fitness", "Aptidão anaeróbica");
        agregarSiNoExiste("enum.categoriaejercicio.aptitud_aerobica", "Aptitud aeróbica", "Aerobic fitness", "Aptidão aeróbica");
        agregarSiNoExiste("enum.categoriaejercicio.flexibilidad", "Flexibilidad", "Flexibility", "Flexibilidade");
        agregarSiNoExiste("enum.categoriaejercicio.agilidad", "Agilidad", "Agility", "Agilidade");
        agregarSiNoExiste("enum.categoriaejercicio.tecnica", "Técnica", "Technique", "Técnica");
        agregarSiNoExiste("enum.categoriaejercicio.anticipacion", "Anticipación", "Anticipation", "Antecipação");

        // Tipo microciclo
        agregarSiNoExiste("enum.tipomicrociclo.corriente", "Corriente", "Ordinary", "Corrente");
        agregarSiNoExiste("enum.tipomicrociclo.choque", "Choque", "Shock", "Choque");
        agregarSiNoExiste("enum.tipomicrociclo.aproximacion", "Aproximación", "Approach", "Aproximação");
        agregarSiNoExiste("enum.tipomicrociclo.competitivo", "Competitivo", "Competitive", "Competitivo");
        agregarSiNoExiste("enum.tipomicrociclo.restauracion", "Restauración", "Restoration", "Restauração");
        agregarSiNoExiste("enum.tipomicrociclo.ajuste", "Ajuste", "Adjustment", "Ajuste");
        agregarSiNoExiste("enum.tipomicrociclo.control", "Control", "Control", "Controle");

        // Nivel competencia
        agregarSiNoExiste("enum.nivelcompetencia.local", "Local", "Local", "Local");
        agregarSiNoExiste("enum.nivelcompetencia.departamental", "Departamental", "Departmental", "Departamental");
        agregarSiNoExiste("enum.nivelcompetencia.nacional", "Nacional", "National", "Nacional");
        agregarSiNoExiste("enum.nivelcompetencia.internacional", "Internacional", "International", "Internacional");
        agregarSiNoExiste("enum.nivelcompetencia.club", "Club", "Club", "Clube");

        // Tipo suscripción
        agregarSiNoExiste("enum.tiposuscripcion.mensual", "Mensual", "Monthly", "Mensal");
        agregarSiNoExiste("enum.tiposuscripcion.bimensual", "Bimensual", "Bimonthly", "Bimestral");
        agregarSiNoExiste("enum.tiposuscripcion.trimestral", "Trimestral", "Quarterly", "Trimestral");
        agregarSiNoExiste("enum.tiposuscripcion.semestral", "Semestral", "Semiannual", "Semestral");
        agregarSiNoExiste("enum.tiposuscripcion.anual", "Anual", "Annual", "Anual");
        agregarSiNoExiste("enum.tiposuscripcion.pago_unico", "Pago único", "Single payment", "Pagamento único");

        // Tipo sesión
        agregarSiNoExiste("enum.tiposesion.tecnica", "Técnica", "Technique", "Técnica");
        agregarSiNoExiste("enum.tiposesion.randori", "Randori", "Randori", "Randori");
        agregarSiNoExiste("enum.tiposesion.uchikomi", "Uchikomi", "Uchikomi", "Uchikomi");
        agregarSiNoExiste("enum.tiposesion.nagekomi", "Nagekomi", "Nagekomi", "Nagekomi");
        agregarSiNoExiste("enum.tiposesion.shiai", "Shiai", "Shiai", "Shiai");
        agregarSiNoExiste("enum.tiposesion.mondokai", "Mondokai", "Mondokai", "Mondokai");
        agregarSiNoExiste("enum.tiposesion.yakusokugeiko", "Yakusokugeiko", "Yakusokugeiko", "Yakusokugeiko");
        agregarSiNoExiste("enum.tiposesion.kakarigeiko", "Kakarigeiko", "Kakarigeiko", "Kakarigeiko");
        agregarSiNoExiste("enum.tiposesion.pesas", "Pesas", "Weights", "Pesos");
        agregarSiNoExiste("enum.tiposesion.acondicionamiento", "Acondicionamiento", "Conditioning", "Condicionamento");
        agregarSiNoExiste("enum.tiposesion.evaluacion", "Evaluación", "Evaluation", "Avaliação");

        // Resultado competencia
        agregarSiNoExiste("enum.resultadocompetencia.participacion", "Participación", "Participation", "Participação");
        agregarSiNoExiste("enum.resultadocompetencia.oro", "Oro", "Gold", "Ouro");
        agregarSiNoExiste("enum.resultadocompetencia.plata", "Plata", "Silver", "Prata");
        agregarSiNoExiste("enum.resultadocompetencia.bronce", "Bronce", "Bronze", "Bronze");
        agregarSiNoExiste("enum.resultadocompetencia.quinto", "Quinto", "Fifth", "Quinto");
        agregarSiNoExiste("enum.resultadocompetencia.septimo", "Séptimo", "Seventh", "Sétimo");

        // Estado asistencia
        agregarSiNoExiste("enum.estadoasistencia.presente", "Presente", "Present", "Presente");
        agregarSiNoExiste("enum.estadoasistencia.ausente", "Ausente", "Absent", "Ausente");
        agregarSiNoExiste("enum.estadoasistencia.excusado", "Excusado", "Excused", "Dispensado");
        agregarSiNoExiste("enum.estadoasistencia.llegada_tarde", "Llegada tarde", "Late arrival", "Chegada atrasada");

        // Bloque Agudelo
        agregarSiNoExiste("enum.bloqueagudelo.definitorio", "Definitorio", "Defining", "Definitório");
        agregarSiNoExiste("enum.bloqueagudelo.sustento", "Sustento", "Sustenance", "Sustento");
        agregarSiNoExiste("enum.bloqueagudelo.eficiencia", "Eficiencia", "Efficiency", "Eficiência");
        agregarSiNoExiste("enum.bloqueagudelo.proteccion", "Protección", "Protection", "Proteção");
        agregarSiNoExiste("enum.bloqueagudelo.tecnico_coordinativo", "Técnico coordinativo", "Coordinative technical", "Técnico coordenativo");

        // Estado pago
        agregarSiNoExiste("enum.estadopago.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste("enum.estadopago.pagado", "Pagado", "Paid", "Pago");
        agregarSiNoExiste("enum.estadopago.en_revision", "En revisión", "Under review", "Em revisão");
        agregarSiNoExiste("enum.estadopago.fallido", "Fallido", "Failed", "Falho");

        // Clasificación rendimiento
        agregarSiNoExiste("enum.clasificacionrendimiento.excelente", "Excelente", "Excellent", "Excelente");
        agregarSiNoExiste("enum.clasificacionrendimiento.muy_bien", "Muy bien", "Very good", "Muito bom");
        agregarSiNoExiste("enum.clasificacionrendimiento.bueno", "Bueno", "Good", "Bom");
        agregarSiNoExiste("enum.clasificacionrendimiento.regular", "Regular", "Regular", "Regular");
        agregarSiNoExiste("enum.clasificacionrendimiento.razonable", "Razonable", "Reasonable", "Razoável");
        agregarSiNoExiste("enum.clasificacionrendimiento.debil", "Débil", "Weak", "Fraco");
        agregarSiNoExiste("enum.clasificacionrendimiento.muy_debil", "Muy débil", "Very weak", "Muito fraco");
        agregarSiNoExiste("enum.clasificacionrendimiento.zona_de_riesgo", "Zona de riesgo", "Risk zone", "Zona de risco");
        agregarSiNoExiste("enum.clasificacionrendimiento.zona_saludable", "Zona saludable", "Healthy zone", "Zona saudável");

        // Categoría insignia
        agregarSiNoExiste("enum.categoriainsignia.shin", "Shin", "Shin", "Shin");
        agregarSiNoExiste("enum.categoriainsignia.gi", "Gi", "Gi", "Gi");
        agregarSiNoExiste("enum.categoriainsignia.tai", "Tai", "Tai", "Tai");

        // Tipo evento gamificación
        agregarSiNoExiste("enum.tipoeventogamificacion.asistencia", "Asistencia", "Attendance", "Presença");
        agregarSiNoExiste("enum.tipoeventogamificacion.resultado_prueba", "Resultado de prueba", "Test result", "Resultado de teste");
        agregarSiNoExiste("enum.tipoeventogamificacion.grado_alcanzado", "Grado alcanzado", "Degree achieved", "Grau alcançado");

        // Método pago
        agregarSiNoExiste("enum.metodopago.efectivo", "Efectivo", "Cash", "Dinheiro");
        agregarSiNoExiste("enum.metodopago.transferencia", "Transferencia", "Transfer", "Transferência");
        agregarSiNoExiste("enum.metodopago.tarjeta", "Tarjeta", "Card", "Cartão");
        agregarSiNoExiste("enum.metodopago.nequi", "Nequi", "Nequi", "Nequi");
        agregarSiNoExiste("enum.metodopago.daviplata", "Daviplata", "Daviplata", "Daviplata");

        // Operador comparación
        agregarSiNoExiste("enum.operadorcomparacion.mayor_que", "Mayor que", "Greater than", "Maior que");
        agregarSiNoExiste("enum.operadorcomparacion.menor_que", "Menor que", "Less than", "Menor que");
        agregarSiNoExiste("enum.operadorcomparacion.igual_a", "Igual a", "Equal to", "Igual a");
        agregarSiNoExiste("enum.operadorcomparacion.mayor_o_igual", "Mayor o igual", "Greater than or equal to", "Maior ou igual");
        agregarSiNoExiste("enum.operadorcomparacion.menor_o_igual", "Menor o igual", "Less than or equal to", "Menor ou igual");

        // Sexo
        agregarSiNoExiste("enum.sexo.masculino", "Masculino", "Male", "Masculino");
        agregarSiNoExiste("enum.sexo.femenino", "Femenino", "Female", "Feminino");

        // Mesociclo ATC
        agregarSiNoExiste("enum.mesocicloatc.adquisicion", "Adquisición", "Acquisition", "Aquisição");
        agregarSiNoExiste("enum.mesocicloatc.transferencia", "Transferencia", "Transfer", "Transferência");
        agregarSiNoExiste("enum.mesocicloatc.competencia", "Competencia", "Competition", "Competição");
        agregarSiNoExiste("enum.mesocicloatc.recuperacion", "Recuperación", "Recovery", "Recuperação");
        agregarSiNoExiste("enum.mesocicloatc.refuerzo", "Refuerzo", "Reinforcement", "Reforço");
        agregarSiNoExiste("enum.mesocicloatc.nivelacion", "Nivelación", "Leveling", "Nivelamento");

        // Tipo mecenas
        agregarSiNoExiste("enum.tipomecenas.persona_natural", "Persona natural", "Natural person", "Pessoa física");
        agregarSiNoExiste("enum.tipomecenas.empresa", "Empresa", "Company", "Empresa");

        // ================== NUEVAS TRADUCCIONES DE VISTAS (DEL CSV) ==================
        agregarSiNoExiste("sala_espera.titulo", "(Sala de Espera)", "(Waiting Room)", "(Sala de Espera)");
        agregarSiNoExiste("agenda.gps.titulo", "Agenda GPS", "GPS Agenda", "Agenda GPS");
        agregarSiNoExiste("estado.al_dia", "Al día", "Up to date", "Em dia");
        agregarSiNoExiste("aspirante.sin_nombre", "Aspirante Sin Nombre", "Unnamed Applicant", "Candidato Sem Nome");
        agregarSiNoExiste("acudiente.panel", "Panel de Acudiente", "Guardian Panel", "Painel do Responsável");
        agregarSiNoExiste("mecenas.padrino_anonimo", "Padrino Anónimo", "Anonymous Sponsor", "Padrinho Anônimo");
        agregarSiNoExiste("mecenas.patrocinado", "Patrocinado", "Sponsored", "Patrocinado");
        agregarSiNoExiste("mecenas.atletas_apoyados", "Atletas Apoyados", "Supported Athletes", "Atletas Apoiados");
        agregarSiNoExiste("mecenas.tus_ahijados", "Tus Ahijados", "Your Sponsored Athletes", "Seus Afilhados");
        agregarSiNoExiste("microciclo.bitacora_fase_r", "Bitácora (Fase R)", "Logbook (Phase R)", "Diário (Fase R)");
        agregarSiNoExiste("microciclo.bitacora_clases", "Bitácora de Clases", "Class Logbook", "Diário de Aulas");
        agregarSiNoExiste("microciclo.bloque", "Bloque", "Block", "Bloco");
        agregarSiNoExiste("microciclo.bloque_metodologico", "Bloque Metodológico", "Methodological Block", "Bloco Metodológico");
        agregarSiNoExiste("microciclo.descanso_segundos", "Descanso (Segundos)", "Rest (Seconds)", "Descanso (Segundos)");
        agregarSiNoExiste("microciclo.dictar_observaciones", "Dicta o escribe tus observaciones (Fase R)", "Dictate or write your observations (Phase R)", "Dite ou escreva suas observações (Fase R)");
        agregarSiNoExiste("microciclo.dosificacion", "Dosificación Específica", "Specific Dosage", "Dosagem Específica");
        agregarSiNoExiste("microciclo.duracion_minutos", "Duración (Minutos)", "Duration (Minutes)", "Duração (Minutos)");
        agregarSiNoExiste("microciclo.ejercicio", "Ejercicio", "Exercise", "Exercício");
        agregarSiNoExiste("microciclo.ejercicios_plan", "Ejercicios del Plan", "Plan Exercises", "Exercícios do Plano");
        agregarSiNoExiste("microciclo.error.nombre_fase", "Debe ingresar el nombre y la Fase ATC", "You must enter the name and ATC Phase", "Você deve inserir o nome e a Fase ATC");
        agregarSiNoExiste("microciclo.error.bloque_obligatorio", "El bloque metodológico es obligatorio", "The methodological block is mandatory", "O bloco metodológico é obrigatório");
        agregarSiNoExiste("microciclo.fase_atc", "Fase (ATC)", "Phase (ATC)", "Fase (ATC)");
        agregarSiNoExiste("microciclo.fase_modelamiento", "Fase de Modelamiento (ATC)", "Modeling Phase (ATC)", "Fase de Modelagem (ATC)");
        agregarSiNoExiste("microciclo.historial", "Historial de Microciclos", "Microcycles History", "Histórico de Microciclos");
        agregarSiNoExiste("microciclo.linea_tiempo", "Línea de Tiempo (Microciclos)", "Timeline (Microcycles)", "Linha do Tempo (Microciclos)");
        agregarSiNoExiste("microciclo.activo", "Microciclo Activo", "Active Microcycle", "Microciclo Ativo");
        agregarSiNoExiste("microciclo.msg.guardado", "Microciclo guardado exitosamente", "Microcycle saved successfully", "Microciclo salvo com sucesso");
        agregarSiNoExiste("microciclo.plural", "Microciclos", "Microcycles", "Microciclos");
        agregarSiNoExiste("microciclo.nombre", "Nombre del Microciclo", "Microcycle Name", "Nome do Microciclo");
        agregarSiNoExiste("microciclo.repeticiones", "Repeticiones", "Repetitions", "Repetições");
        agregarSiNoExiste("microciclo.tipo", "Tipo de Microciclo", "Microcycle Type", "Tipo de Microciclo");
        agregarSiNoExiste("microciclo.trabajo_segundos", "Trabajo (Segundos)", "Work (Seconds)", "Trabalho (Segundos)");
        agregarSiNoExiste("microciclo.vacio_taller", "Vacío (Agrega microciclos desde el Taller)", "Empty (Add microcycles from the Workshop)", "Vazio (Adicione microciclos da Oficina)");
        agregarSiNoExiste("microciclo.btn.guardar", "Guardar Microciclo", "Save Microcycle", "Salvar Microciclo");
        agregarSiNoExiste("microciclo.btn.nuevo", "Nuevo Microciclo", "New Microcycle", "Novo Microciclo");

        agregarSiNoExiste("macrociclo.titulo", "Macrociclo", "Macrocycle", "Macrociclo");
        agregarSiNoExiste("macrociclo.opcional", "Macrociclo (Opcional)", "Macrocycle (Optional)", "Macrociclo (Opcional)");
        agregarSiNoExiste("macrociclo.msg.guardado", "Macrociclo guardado", "Macrocycle saved", "Macrociclo salvo");
        agregarSiNoExiste("macrociclo.mis_macrociclos", "Mis Macrociclos (Temporadas)", "My Macrocycles (Seasons)", "Meus Macrociclos (Temporadas)");
        agregarSiNoExiste("macrociclo.btn.nuevo", "Nuevo Macrociclo", "New Macrocycle", "Novo Macrociclo");
        agregarSiNoExiste("macrociclo.objetivo_principal", "Objetivo Principal", "Main Objective", "Objetivo Principal");

        agregarSiNoExiste("sensei.mis_deportistas", "Mis Deportistas", "My Athletes", "Meus Atletas");
        agregarSiNoExiste("invitaciones.titulo", "Centro de Invitaciones", "Invitation Center", "Centro de Convites");
        agregarSiNoExiste("invitaciones.btn.invitar_otro", "Invitar a otro contacto", "Invite another contact", "Convidar outro contato");
        agregarSiNoExiste("invitaciones.msg.whatsapp_listo", "Mensaje listo para WhatsApp", "Message ready for WhatsApp", "Mensagem pronta para WhatsApp");
        agregarSiNoExiste("invitaciones.msg.enlace_generado", "¡Enlace Generado!", "Link Generated!", "Link Gerado!");
        agregarSiNoExiste("invitaciones.error.pase_invalido", "Pase Mágico Inválido o Expirado", "Invalid or Expired Magic Pass", "Passe Mágico Inválido ou Expirado");
        agregarSiNoExiste("invitaciones.a_quien_invitar", "¿A quién deseas invitar?", "Who do you want to invite?", "Quem você deseja convidar?");

        agregarSiNoExiste("evaluacion.error.unidad_medida", "Debe seleccionar al menos una unidad de medida", "You must select at least one unit of measurement", "Você deve selecionar pelo menos uma unidade de medida");
        agregarSiNoExiste("evaluacion.distancia_cm", "Distancia (cm)", "Distance (cm)", "Distância (cm)");
        agregarSiNoExiste("evaluacion.titulo", "Evaluaciones", "Evaluations", "Avaliações");
        agregarSiNoExiste("evaluacion.msg.guardada", "Evaluación guardada exitosamente", "Evaluation saved successfully", "Avaliação salva com sucesso");
        agregarSiNoExiste("evaluacion.btn.evalua", "Evalúa", "Evaluate", "Avaliar");
        agregarSiNoExiste("evaluacion.error.pruebas_globales", "Las pruebas globales no se pueden editar", "Global tests cannot be edited", "Testes globais não podem ser editados");
        agregarSiNoExiste("evaluacion.metrica_solo_pruebas", "Métrica (solo para pruebas)", "Metric (only for tests)", "Métrica (apenas para testes)");
        agregarSiNoExiste("evaluacion.nombre", "Nombre de la Evaluación", "Evaluation Name", "Nome da Avaliação");
        agregarSiNoExiste("evaluacion.btn.nueva_prueba", "Nueva Prueba", "New Test", "Novo Teste");
        agregarSiNoExiste("evaluacion.objetivo_mide", "Objetivo (¿Qué mide?)", "Objective (What does it measure?)", "Objetivo (O que mede?)");
        agregarSiNoExiste("evaluacion.prueba_autor", "Prueba de Autor (Sensei)", "Author Test (Sensei)", "Teste de Autor (Sensei)");
        agregarSiNoExiste("evaluacion.error.no_encontrada", "Prueba no encontrada", "Test not found", "Teste não encontrado");
        agregarSiNoExiste("evaluacion.tiempo_s", "Tiempo (s)", "Time (s)", "Tempo (s)");
        agregarSiNoExiste("evaluacion.unidades_medida", "Unidades de Medida a Evaluar", "Measurement Units to Evaluate", "Unidades de Medida a Avaliar");
        agregarSiNoExiste("evaluacion.valor_objetivo", "Valor Objetivo", "Target Value", "Valor Objetivo");

        agregarSiNoExiste("tatami.btn.entrar", "ENTRAR AL TATAMI", "ENTER THE TATAMI", "ENTRAR NO TATAME");
        agregarSiNoExiste("tatami.btn.iniciar_cronometro", "Iniciar Cronómetro", "Start Timer", "Iniciar Cronômetro");
        agregarSiNoExiste("tatami.btn.iniciar_personalizado", "Iniciar Personalizado", "Start Custom", "Iniciar Personalizado");
        agregarSiNoExiste("tatami.btn.pausar", "PAUSAR", "PAUSE", "PAUSAR");
        agregarSiNoExiste("tatami.toca_pantalla", "TOCA LA PANTALLA PARA INICIAR", "TOUCH SCREEN TO START", "TOQUE NA TELA PARA INICIAR");
        agregarSiNoExiste("tatami.randori_oficial", "Randori Oficial (4' x 1')", "Official Randori (4' x 1')", "Randori Oficial (4' x 1')");
        agregarSiNoExiste("tatami.uchikomi_30_10", "Uchikomi (30'' x 10'')", "Uchikomi (30'' x 10'')", "Uchikomi (30'' x 10'')");
        agregarSiNoExiste("tatami.btn.ver_combate", "Ver Combate", "Watch Match", "Ver Combate");

        agregarSiNoExiste("auth.codigo_verificacion", "Código de Verificación", "Verification Code", "Código de Verificação");
        agregarSiNoExiste("auth.codigo_enviado", "Código enviado a", "Code sent to", "Código enviado para");
        agregarSiNoExiste("auth.codigo_incorrecto", "Código incorrecto", "Incorrect code", "Código incorreto");
        agregarSiNoExiste("auth.verificar_email", "Verificar Email", "Verify Email", "Verificar E-mail");
        agregarSiNoExiste("auth.verificar_finalizar", "Verificar y Finalizar", "Verify and Finish", "Verificar e Finalizar");

        agregarSiNoExiste("master.nuevas_suscripciones", "Nuevas Suscripciones SaaS (Otros Dojos)", "New SaaS Subscriptions (Other Dojos)", "Novas Assinaturas SaaS (Outros Dojos)");

        // Nota: algunas claves como "estado.aprobado.master.sin.soporte" no se incluyeron porque no estaban en el CSV, pero si las necesitas, agrégalas aquí.
        agregarSiNoExiste("mecenas.btn.ver_perfil", "Ver Perfil", "View Profile", "Ver Perfil");
        agregarSiNoExiste("sensei.btn.ver_perfil", "Ver Perfil", "View Profile", "Ver Perfil");
        agregarSiNoExiste("grid.judoka.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste("grid.judoka.apellido", "Apellido", "Last Name", "Sobrenome");
        agregarSiNoExiste("grid.judoka.grado", "Grado", "Rank", "Graduação");
        agregarSiNoExiste("grid.judoka.accion", "Acción", "Action", "Ação");
        System.out.println(">>> Verificación de traducciones completada.");
    }

    private void agregarSiNoExiste(String clave, String es, String en, String pt) {
        if (!traduccionRepo.findByClaveAndIdioma(clave, "es").isPresent()) {
            traduccionRepo.save(new Traduccion(clave, "es", es));
        }
        if (!traduccionRepo.findByClaveAndIdioma(clave, "en").isPresent()) {
            traduccionRepo.save(new Traduccion(clave, "en", en));
        }
        if (!traduccionRepo.findByClaveAndIdioma(clave, "pt").isPresent()) {
            traduccionRepo.save(new Traduccion(clave, "pt", pt));
        }
    }
}