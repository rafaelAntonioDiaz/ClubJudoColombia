package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TraduccionInicializer {

    private final TraduccionRepository traduccionRepo;

    public TraduccionInicializer(TraduccionRepository traduccionRepo) {
        this.traduccionRepo = traduccionRepo;
    }

    @Transactional
    public void inicializar() {
        System.out.println(">>> Verificando y cargando traducciones faltantes...");
        Set<String> clavesExistentes = new HashSet<>();
        for (Traduccion t : traduccionRepo.findAll()) {
            clavesExistentes.add(t.getClave());
        }

        List<Traduccion> lista = new ArrayList<>();


        // ================== TRADUCCIONES COMUNES ==================
        agregarSiNoExiste(lista, clavesExistentes, "app.nombre", "Club de Judo Colombia", "Judo Club Colombia", "Clube de Judô Colômbia");
        agregarSiNoExiste(lista, clavesExistentes, "btn.guardar", "Guardar", "Save", "Salvar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.cancelar", "Cancelar", "Cancel", "Cancelar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.editar", "Editar", "Edit", "Editar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.eliminar", "Eliminar", "Delete", "Excluir");
        agregarSiNoExiste(lista, clavesExistentes, "btn.crear", "Crear", "Create", "Criar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.actualizar", "Actualizar", "Update", "Atualizar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.guardar.cambios", "Guardar Cambios", "Save Changes", "Salvar Alterações");
        agregarSiNoExiste(lista, clavesExistentes, "btn.guardar_cambios", "Guardar Cambios", "Save Changes", "Salvar Alterações");
        agregarSiNoExiste(lista, clavesExistentes, "btn.cerrar", "Cerrar", "Close", "Fechar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.cerrar.sesion", "Cerrar Sesión", "Logout", "Sair");
        agregarSiNoExiste(lista, clavesExistentes, "btn.confirmar", "Confirmar", "Confirm", "Confirmar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.atras", "Atrás", "Back", "Voltar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.finalizar", "Finalizar", "Finish", "Finalizar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.siguiente.paso", "Siguiente", "Next", "Próximo");
        agregarSiNoExiste(lista, clavesExistentes, "btn.vender", "Vender", "Sell", "Vender");
        agregarSiNoExiste(lista, clavesExistentes, "btn.ver_pdf", "Ver PDF", "View PDF", "Ver PDF");
        agregarSiNoExiste(lista, clavesExistentes, "btn.nuevo.usuario", "Nuevo Usuario", "New User", "Novo Usuário");
        agregarSiNoExiste(lista, clavesExistentes, "btn.completado", "Completado", "Completed", "Concluído");
        agregarSiNoExiste(lista, clavesExistentes, "btn.marcar.hecho", "Marcar como Hecho", "Mark as Done", "Marcar como Feito");
        agregarSiNoExiste(lista, clavesExistentes, "btn.registrar.pensamiento", "Registrar Pensamiento", "Log Thought", "Registrar Pensamento");
        agregarSiNoExiste(lista, clavesExistentes, "btn.activar", "Activar", "Activate", "Ativar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.rechazar", "Rechazar", "Reject", "Rejeitar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.agregar", "Agregar", "Add", "Adicionar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.quitar", "Quitar", "Remove", "Remover");
        agregarSiNoExiste(lista, clavesExistentes, "btn.filtrar", "Filtrar", "Filter", "Filtrar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.pagar", "Pagar", "Pay", "Pagar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.copiar_enlace", "Copiar Enlace", "Copy Link", "Copiar Link");
        agregarSiNoExiste(lista, clavesExistentes, "btn.copiar_mensaje", "Copiar Mensaje", "Copy Message", "Copiar Mensagem");
        agregarSiNoExiste(lista, clavesExistentes, "btn.descargar_formato", "Descargar Formato", "Download Form", "Baixar Formulário");
        agregarSiNoExiste(lista, clavesExistentes, "btn.descargar_formato_vacio", "Descargar Formato Vacío", "Download Empty Form", "Baixar Formulário Vazio");
        agregarSiNoExiste(lista, clavesExistentes, "btn.enviar_revision", "Enviar a Revisión", "Send for Review", "Enviar para Revisão");
        agregarSiNoExiste(lista, clavesExistentes, "btn.finalizar_sesion", "Finalizar Sesión", "End Session", "Finalizar Sessão");
        agregarSiNoExiste(lista, clavesExistentes, "btn.generar_enlace", "Generar Enlace", "Generate Link", "Gerar Link");
        agregarSiNoExiste(lista, clavesExistentes, "btn.guardar_finalizar", "Guardar y Finalizar", "Save and Finish", "Salvar e Finalizar");
        agregarSiNoExiste(lista, clavesExistentes, "btn.hecho", "Hecho", "Done", "Feito");
        agregarSiNoExiste(lista, clavesExistentes, "btn.salir", "Salir", "Exit", "Sair");

        // ================== TEXTO GENÉRICO ==================
        agregarSiNoExiste(lista, clavesExistentes, "generic.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, clavesExistentes, "generic.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste(lista, clavesExistentes, "generic.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste(lista, clavesExistentes, "generic.acciones", "Acciones", "Actions", "Ações");
        agregarSiNoExiste(lista, clavesExistentes, "generic.estado", "Estado", "Status", "Estado");
        agregarSiNoExiste(lista, clavesExistentes, "generic.cantidad", "Cantidad", "Quantity", "Quantidade");
        agregarSiNoExiste(lista, clavesExistentes, "generic.judoka", "Judoka", "Judoka", "Judoca");
        agregarSiNoExiste(lista, clavesExistentes, "generic.grupo", "Grupo", "Group", "Grupo");
        agregarSiNoExiste(lista, clavesExistentes, "generic.horario", "Horario", "Schedule", "Horário");
        agregarSiNoExiste(lista, clavesExistentes, "generic.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste(lista, clavesExistentes, "generic.inicio", "Inicio", "Start", "Início");
        agregarSiNoExiste(lista, clavesExistentes, "generic.fin", "Fin", "End", "Fim");
        agregarSiNoExiste(lista, clavesExistentes, "generic.fecha_inicio", "Fecha Inicio", "Start Date", "Data Início");
        agregarSiNoExiste(lista, clavesExistentes, "generic.fecha_fin", "Fecha Fin", "End Date", "Data Fim");
        agregarSiNoExiste(lista, clavesExistentes, "generic.pts", "pts", "pts", "pts");
        agregarSiNoExiste(lista, clavesExistentes, "generic.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste(lista, clavesExistentes, "generic.pagado", "Pagado", "Paid", "Pago");
        agregarSiNoExiste(lista, clavesExistentes, "generic.aspirante", "Aspirante", "Applicant", "Candidato");
        agregarSiNoExiste(lista, clavesExistentes, "generic.decision", "Decisión", "Decision", "Decisão");
        agregarSiNoExiste(lista, clavesExistentes, "generic.cinturon", "Cinturón", "Belt", "Cinturão");
        agregarSiNoExiste(lista, clavesExistentes, "generic.atleta", "Atleta", "Athlete", "Atleta");
        agregarSiNoExiste(lista, clavesExistentes, "generic.catalogo", "Catálogo", "Catalog", "Catálogo");
        agregarSiNoExiste(lista, clavesExistentes, "generic.celular", "Celular", "Mobile", "Celular");
        agregarSiNoExiste(lista, clavesExistentes, "generic.club_judo", "Club Judo", "Judo Club", "Clube de Judô");
        agregarSiNoExiste(lista, clavesExistentes, "generic.configuracion", "Configuración", "Configuration", "Configuração");
        agregarSiNoExiste(lista, clavesExistentes, "generic.deportista", "Deportista (Judoka)", "Athlete (Judoka)", "Atleta (Judoca)");
        agregarSiNoExiste(lista, clavesExistentes, "generic.deportista_adulto", "Deportista Adulto", "Adult Athlete", "Atleta Adulto");
        agregarSiNoExiste(lista, clavesExistentes, "generic.descripcion_opcional", "Descripción (opcional)", "Description (optional)", "Descrição (opcional)");
        agregarSiNoExiste(lista, clavesExistentes, "generic.enlace_acceso", "Enlace de Acceso Directo", "Direct Access Link", "Link de Acesso Direto");
        agregarSiNoExiste(lista, clavesExistentes, "generic.error.nombre_obligatorio", "El nombre es obligatorio", "The name is mandatory", "O nome é obrigatório");
        agregarSiNoExiste(lista, clavesExistentes, "generic.familia", "Familia", "Family", "Família");
        agregarSiNoExiste(lista, clavesExistentes, "generic.fechas", "Fechas", "Dates", "Datas");
        agregarSiNoExiste(lista, clavesExistentes, "generic.grupos", "Grupos", "Groups", "Grupos");
        agregarSiNoExiste(lista, clavesExistentes, "generic.grupos_plural", "Grupo(s)", "Group(s)", "Grupo(s)");
        agregarSiNoExiste(lista, clavesExistentes, "generic.hola", "Hola", "Hello", "Olá");
        agregarSiNoExiste(lista, clavesExistentes, "generic.id_dojo", "ID del Dojo", "Dojo ID", "ID do Dojo");
        agregarSiNoExiste(lista, clavesExistentes, "generic.insignia", "Insignia", "Badge", "Medalha");
        agregarSiNoExiste(lista, clavesExistentes, "generic.mas_informacion", "Más información", "More information", "Mais informações");
        agregarSiNoExiste(lista, clavesExistentes, "generic.menu", "Menú", "Menu", "Menu");
        agregarSiNoExiste(lista, clavesExistentes, "generic.ninguna", "Ninguna", "None", "Nenhuma");
        agregarSiNoExiste(lista, clavesExistentes, "generic.notas_opcional", "Notas (opcional)", "Notes (optional)", "Notas (opcional)");
        agregarSiNoExiste(lista, clavesExistentes, "generic.operador", "Operador", "Operator", "Operador");
        agregarSiNoExiste(lista, clavesExistentes, "generic.orden", "Orden", "Order", "Ordem");
        agregarSiNoExiste(lista, clavesExistentes, "generic.periodo", "Periodo", "Period", "Período");
        agregarSiNoExiste(lista, clavesExistentes, "generic.profesor_rafael", "el Profesor Rafael", "Professor Rafael", "o Professor Rafael");
        agregarSiNoExiste(lista, clavesExistentes, "generic.progreso", "Progreso", "Progress", "Progresso");
        agregarSiNoExiste(lista, clavesExistentes, "generic.sensei", "Sensei", "Sensei", "Sensei");
        agregarSiNoExiste(lista, clavesExistentes, "generic.sensei_cargo", "Sensei a Cargo", "Sensei in Charge", "Sensei Responsável");
        agregarSiNoExiste(lista, clavesExistentes, "generic.sin_fechas", "Sin fechas", "No dates", "Sem datas");
        agregarSiNoExiste(lista, clavesExistentes, "generic.sin_nombre", "Sin Nombre", "No Name", "Sem Nome");
        agregarSiNoExiste(lista, clavesExistentes, "generic.tipo_evento", "Tipo de Evento", "Event Type", "Tipo de Evento");
        agregarSiNoExiste(lista, clavesExistentes, "generic.todos", "Todos", "All", "Todos");
        agregarSiNoExiste(lista, clavesExistentes, "generic.torneo", "Torneo", "Tournament", "Torneio");
        agregarSiNoExiste(lista, clavesExistentes, "generic.valores", "Valores", "Values", "Valores");
        agregarSiNoExiste(lista, clavesExistentes, "generic.vas_a_registrar", "Vas a registrar", "You are going to register", "Você vai registrar");
        agregarSiNoExiste(lista, clavesExistentes, "generic.vence", "Vence", "Expires", "Vence");
        agregarSiNoExiste(lista, clavesExistentes, "generic.ver_detalles", "Ver Detalles", "View Details", "Ver Detalhes");
        agregarSiNoExiste(lista, clavesExistentes, "generic.indice", "Índice", "Index", "Índice");

        // ================== DASHBOARD ==================
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.welcome", "Hola, {0}", "Hello, {0}", "Olá, {0}");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.titulo", "Panel de Control", "Dashboard", "Painel de Controle");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.boton.tomar_asistencia", "Tomar Asistencia", "Take Attendance", "Registrar Presença");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.btn.tareas", "Ir a Mis Tareas", "Go to My Tasks", "Ir para Minhas Tarefas");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.kpi.total_judokas", "Total Judokas", "Total Judokas", "Total de Judocas");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.kpi.grupos_activos", "Grupos Activos", "Active Groups", "Grupos Ativos");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.kpi.pruebas_hoy", "Para Hoy", "For Today", "Para Hoje");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.kpi.asistencia_promedio", "Asistencia Promedio", "Average Attendance", "Média de Presença");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.grafico.poder_combate_titulo", "Poder de Combate por Grupo", "Combat Power by Group", "Poder de Combate por Grupo");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.grafico.asistencia_30dias_titulo", "Asistencia últimos 30 días", "Last 30 days attendance", "Presença últimos 30 dias");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.grafico.promedio", "Promedio", "Average", "Média");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.grafico.asistencia_porcentaje", "Asistencia %", "Attendance %", "Presença %");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.mes1", "Mes 1", "Month 1", "Mês 1");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.mi_progreso", "Mi Progreso", "My Progress", "Meu Progresso");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.poder_oculto", "Poder Oculto", "Hidden Power", "Poder Oculto");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.camino_shodan", "Camino al Cinturón Negro (Shodan)", "Road to Black Belt (Shodan)", "Caminho para a Faixa Preta (Shodan)");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.alumnos.principal", "Alumnos Dojo Principal", "Main Dojo Students", "Alunos do Dojo Principal");
        agregarSiNoExiste(lista, clavesExistentes, "dashboard.navegando_progreso", "Navegando al progreso de", "Navigating to the progress of", "Navegando para o progresso de");

        // ================== CHART ==================
        agregarSiNoExiste(lista, clavesExistentes, "chart.radar.serie", "Nivel Actual", "Current Level", "Nível Atual");
        agregarSiNoExiste(lista, clavesExistentes, "chart.sin_datos", "Sin datos", "No data", "Sem dados");
        agregarSiNoExiste(lista, clavesExistentes, "chart.tu_progreso", "Tu Progreso", "Your Progress", "Seu Progresso");

        // ================== EMPTY ==================
        agregarSiNoExiste(lista, clavesExistentes, "empty.desc.realiza_pruebas", "Realiza pruebas para ver tu evolución.", "Take tests to see your progress.", "Realize testes para ver sua evolução.");

        // ================== TOOLTIP ==================
        agregarSiNoExiste(lista, clavesExistentes, "tooltip.trofeos", "Mis Trofeos e Insignias", "My Trophies & Badges", "Meus Troféus e Medalhas");
        agregarSiNoExiste(lista, clavesExistentes, "tooltip.palmares", "Mi Palmarés", "My Record", "Meu Histórico");
        agregarSiNoExiste(lista, clavesExistentes, "tooltip.cambiar.foto", "Cambiar foto de perfil", "Change profile photo", "Alterar foto de perfil");
        agregarSiNoExiste(lista, clavesExistentes, "tooltip.registro.permanente", "Registro permanente (No editable)", "Permanent record (Non-editable)", "Registro permanente (Não editável)");
        agregarSiNoExiste(lista, clavesExistentes, "tooltip.asignar.grupo", "Asignar a", "Assign to", "Atribuir a");
        agregarSiNoExiste(lista, clavesExistentes, "tooltip.remover.grupo", "Remover del grupo", "Remove from group", "Remover do grupo");

        // ================== PERFIL ==================
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tab.resumen", "Resumen", "Summary", "Resumo");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tab.pruebas", "Pruebas", "Tests", "Testes");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tab.tareas", "Tareas", "Tasks", "Tarefas");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tab.insignias", "Insignias", "Badges", "Medalhas");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tab.palmares", "Palmarés", "Record", "Histórico");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tab.documentos", "Documentos", "Documents", "Documentos");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.antropometria", "Antropometría histórica", "Historical anthropometry", "Antropometria histórica");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.sin_datos", "Sin datos antropométricos", "No anthropometric data", "Sem dados antropométricos");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.sin_resultados", "No hay resultados registrados", "No test results recorded", "Nenhum resultado registrado");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.sin_tareas", "No hay tareas ejecutadas", "No tasks executed", "Nenhuma tarefa executada");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.sin_palmares", "Sin participación en competiciones", "No competition record", "Sem participação em competições");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.sin_documentos", "No hay documentos subidos", "No documents uploaded", "Nenhum documento enviado");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.filtro.prueba", "Prueba", "Test", "Teste");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.filtro.desde", "Desde", "From", "De");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.filtro.hasta", "Hasta", "To", "Até");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.filtrar", "Filtrar", "Filter", "Filtrar");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.prueba", "Prueba", "Test", "Teste");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.metrica", "Métrica", "Metric", "Métrica");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.valor", "Valor", "Value", "Valor");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.clasificacion", "Clasificación", "Classification", "Classificação");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.puntos", "Puntos", "Points", "Pontos");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.tarea", "Tarea", "Task", "Tarefa");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.completada", "Completada", "Completed", "Concluída");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.evento", "Evento", "Event", "Evento");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.lugar", "Lugar", "Place", "Local");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.resultado", "Resultado", "Result", "Resultado");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.archivo", "Archivo", "File", "Arquivo");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.grid.accion", "Acción", "Action", "Ação");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.ver", "Ver", "View", "Ver");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.notas.titulo", "Bitácora de Reflexión", "Reflection Journal", "Diário de Reflexão");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.notas.placeholder", "Escribe aquí tus pensamientos...", "Write your thoughts here...", "Escreva aqui seus pensamentos...");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.msg.guardado", "Reflexión guardada", "Reflection saved", "Reflexão salva");
        agregarSiNoExiste(lista, clavesExistentes, "title.editar.reflexion", "Editar Reflexión", "Edit Reflection", "Editar Reflexão");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.estatura_cm", "Estatura (cm)", "Height (cm)", "Altura (cm)");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.imc", "IMC", "BMI", "IMC");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.tu_grado", "Tu Grado Actual", "Your Current Rank", "Sua Graduação Atual");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.anos_practica", "Años de Práctica", "Years of Practice", "Anos de Prática");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.palmares_deportivo", "Palmarés Deportivo", "Sports Record", "Histórico Esportivo");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.btn.guardar_revision", "Guardar Perfil y Enviar a Revisión", "Save Profile and Send for Review", "Salvar Perfil e Enviar para Revisão");

        // ================== VISTAS DE ADMINISTRACIÓN ==================
        agregarSiNoExiste(lista, clavesExistentes, "view.asignacion.titulo", "Asignación de Judokas a Grupos", "Judoka Group Assignment", "Atribuição de Judocas a Grupos");
        agregarSiNoExiste(lista, clavesExistentes, "view.gestion.usuarios.titulo", "Gestión de Usuarios", "User Management", "Gestão de Usuários");
        agregarSiNoExiste(lista, clavesExistentes, "view.judoka.plan.titulo", "Entrenamiento de Hoy", "Today's Training", "Treino de Hoje");

        // ================== RESULTADOS ==================
        agregarSiNoExiste(lista, clavesExistentes, "resultados.titulo", "Registro de Resultados", "Test Results Registration", "Registro de Resultados");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.selector.judoka", "Seleccionar Judoka", "Select Judoka", "Selecionar Judoca");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.grid.planes.header", "Planes de Evaluación", "Evaluation Plans", "Planos de Avaliação");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.grid.pruebas.header", "Pruebas del Plan", "Plan Tests", "Testes do Plano");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.feedback.inicio", "Resultados guardados: ", "Results saved: ", "Resultados salvos: ");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.feedback.sjft", "Índice SJFT: %.2f (%s). ", "SJFT Index: %.2f (%s). ", "Índice SJFT: %.2f (%s). ");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.feedback.prueba", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.error.guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.sin_clasificacion", "Sin clasificación", "No classification", "Sem classificação");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.sjft.error.faltan_datos", "Faltan datos para calcular el índice SJFT.", "Missing data to calculate SJFT index.", "Faltam dados para calcular o índice SJFT.");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.sjft.error.total_cero", "El total de proyecciones no puede ser cero.", "Total projections cannot be zero.", "Total de projeções não pode ser zero.");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.sjft.error.metrica_no_encontrada", "Métrica no encontrada.", "Metric not found.", "Métrica não encontrada.");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.sjft.nota_automatica", "Índice SJFT calculado automáticamente.", "SJFT index automatically calculated.", "Índice SJFT calculado automaticamente.");
        agregarSiNoExiste(lista, clavesExistentes, "resultados.btn.guardar", "Guardar Resultados", "Save Results", "Salvar Resultados");

        // ================== ASISTENCIA ==================
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.boton.cerrar_clase", "Cerrar Clase y Guardar", "Close Class and Save", "Fechar Aula e Salvar");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.boton.cerrar", "Cerrar", "Close", "Fechar");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.selector.grupo", "Selecciona el Grupo", "Select Group", "Selecione o Grupo");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.placeholder.grupo", "Ej: Infantiles Martes", "Ex: Kids Tuesday", "Ex: Infantis Terça");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.mensaje.sin_alumnos", "Este grupo no tiene alumnos asignados.", "This group has no assigned students.", "Este grupo não tem alunos atribuídos.");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.estado.ausente", "AUSENTE", "ABSENT", "AUSENTE");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.estado.presente", "PRESENTE", "PRESENT", "PRESENTE");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.notificacion.cargados", "Cargados", "Loaded", "Carregados");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.notificacion.alumnos", "alumnos", "students", "alunos");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.notificacion.registrada", "Asistencia registrada", "Attendance registered", "Presença registrada");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.notificacion.presentes", "Presentes", "Present", "Presentes");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.notificacion.error_guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.titulo", "🚨 INFORMACIÓN DE EMERGENCIA", "🚨 EMERGENCY INFORMATION", "🚨 INFORMAÇÃO DE EMERGÊNCIA");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.acudiente_movil", "Acudiente/Móvil", "Guardian/Mobile", "Responsável/Celular");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.email", "Email", "Email", "E-mail");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.eps", "EPS", "Health Insurance", "Convênio");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.nombre_acudiente", "Nombre Acudiente", "Guardian Name", "Nome do Responsável");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.llamar_ahora", "Llamar Ahora", "Call Now", "Ligar Agora");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.dialog.sos.sin_telefono", "Sin Teléfono Registrado", "No Phone Registered", "Sem Telefone Registrado");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.btn.pasar_lista_rapida", "Pasar Lista (Modo Rápido)", "Roll Call (Fast Mode)", "Fazer Chamada (Modo Rápido)");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.msg.lista_terminada", "¡Lista terminada!", "Roll call finished!", "Chamada concluída!");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.badge.asistio", "✅ Asististe", "✅ Attended", "✅ Compareceu");
        agregarSiNoExiste(lista, clavesExistentes, "asistencia.badge.no_asistio", "❌ No asististe", "❌ Did not attend", "❌ Não compareceu");
        agregarSiNoExiste(lista, clavesExistentes, "generic.badge.programada", "📅 Programada", "📅 Scheduled", "📅 Agendada");

        // ================== GAMIFICACIÓN ==================
        agregarSiNoExiste(lista, clavesExistentes, "gamificacion.titulo", "Reglas de Gamificación", "Gamification Rules", "Regras de Gamificação");
        agregarSiNoExiste(lista, clavesExistentes, "gamificacion.nueva_regla", "Nueva Regla", "New Rule", "Nova Regra");
        agregarSiNoExiste(lista, clavesExistentes, "gamificacion.msg.regla_guardada", "Regla guardada", "Rule saved", "Regra salva");
        agregarSiNoExiste(lista, clavesExistentes, "gamificacion.msg.regla_eliminada", "Regla eliminada", "Rule deleted", "Regra excluída");

        // ================== WIDGET MI DO ==================
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.titulo", "Mi Do (La Vía)", "My Do (The Way)", "Meu Do (O Caminho)");
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.shin", "SHIN (Mente)", "SHIN (Mind)", "SHIN (Mente)");
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.gi", "GI (Técnica)", "GI (Technique)", "GI (Técnica)");
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.tai", "TAI (Cuerpo)", "TAI (Body)", "TAI (Corpo)");
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.btn_catalogo", "Ver Catálogo", "View Catalog", "Ver Catálogo");
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.catalogo_titulo", "Salón de la Fama", "Hall of Fame", "Salão da Fama");
        agregarSiNoExiste(lista, clavesExistentes, "widget.mido.msg_inicio", "¡Tu camino comienza!", "Your journey begins!", "Sua jornada começa!");

        // ================== BADGES ==================
        agregarSiNoExiste(lista, clavesExistentes, "badge.estado.desbloqueada", "¡Insignia Desbloqueada!", "Badge Unlocked!", "Medalha Desbloqueada!");
        agregarSiNoExiste(lista, clavesExistentes, "badge.estado.bloqueada", "Insignia Bloqueada", "Badge Locked", "Medalha Bloqueada");
        agregarSiNoExiste(lista, clavesExistentes, "badge.estado.sin_datos", "Sin Datos", "No Data", "Sem Dados");
        agregarSiNoExiste(lista, clavesExistentes, "badge.label.obtenida", "Obtenida el", "Earned on", "Obtida em");
        agregarSiNoExiste(lista, clavesExistentes, "badge.label.pendiente", "Pendiente", "Pending", "Pendente");

        // ================== CHECK-IN ==================
        agregarSiNoExiste(lista, clavesExistentes, "checkin.titulo", "Control de Asistencia GPS", "GPS Attendance Control", "Controle de Presença GPS");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.status.ready", "Listo para verificar ubicación.", "Ready to check location.", "Pronto para verificar localização.");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.btn.marcar", "Marcar Asistencia", "Mark Attendance", "Registrar Presença");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.status.locating", "Localizando...", "Locating...", "Localizando...");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.status.requesting", "Solicitando permiso GPS...", "Requesting GPS permission...", "Solicitando permissão GPS...");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.btn.retry", "Reintentar Check-in", "Retry Check-in", "Tentar Novamente");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.error.denied", "Error GPS: Permiso denegado.", "GPS Error: Permission denied.", "Erro GPS: Permissão negada.");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.error.browser", "ERROR: Debes habilitar el GPS.", "ERROR: You must enable GPS.", "ERRO: Você deve habilitar o GPS.");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.status.validating", "Validando distancia...", "Validating distance...", "Validando distância...");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.btn.success", "¡Asistencia Marcada!", "Attendance Marked!", "Presença Registrada!");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.status.registered", "Te has registrado correctamente.", "You have registered correctly.", "Você se registrou corretamente.");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.msg.oss", "¡Asistencia registrada! Oss.", "Attendance registered! Oss.", "Presença registrada! Oss.");
        agregarSiNoExiste(lista, clavesExistentes, "checkin.pase_qr", "Pase QR", "QR Pass", "Passe QR");

        // ================== GRUPOS ==================
        agregarSiNoExiste(lista, clavesExistentes, "grupos.titulo", "Gestión de Grupos", "Group Management", "Gestão de Grupos");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.btn.nuevo", "Nuevo Grupo", "New Group", "Novo Grupo");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.btn.aprobar", "Aprobar Grupo", "Approve Group", "Aprovar Grupo");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.grid.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.grid.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.grid.miembros", "Miembros", "Members", "Membros");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.label.alumnos", "alumnos", "students", "alunos");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.tooltip.gestionar_miembros", "Gestionar Miembros", "Manage Members", "Gerenciar Membros");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.dialog.miembros.titulo", "Miembros de", "Members of", "Membros de");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.field.buscar_alumno", "Buscar alumno para agregar...", "Search student to add...", "Buscar aluno para adicionar...");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.section.agregar", "Agregar Nuevo Miembro", "Add New Member", "Adicionar Novo Membro");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.section.actuales", "Miembros Actuales", "Current Members", "Membros Atuais");
        agregarSiNoExiste(lista, clavesExistentes, "grupos.asignados", "Grupos Asignados", "Assigned Groups", "Grupos Atribuídos");

        // ================== CAMPENATOS ==================
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.titulo", "Gestión de Campeonatos", "Championship Management", "Gestão de Campeonatos");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.btn.nueva_convocatoria", "Nueva Convocatoria", "New Call-up", "Nova Convocação");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.grid.evento", "Evento", "Event", "Evento");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.grid.resultado", "Resultado", "Result", "Resultado");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.dialog.convocatoria.titulo", "Crear Convocatoria", "Create Call-up", "Criar Convocação");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.field.nombre_evento", "Nombre del Evento", "Event Name", "Nome do Evento");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.field.lugar", "Lugar", "Place", "Local");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.field.nivel", "Nivel", "Level", "Nível");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.field.seleccionar_atletas", "Seleccionar Atletas", "Select Athletes", "Selecionar Atletas");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.msg.inscritos", "atletas inscritos exitosamente.", "athletes registered successfully.", "atletas inscritos com sucesso.");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.dialog.resultado.titulo", "Resultado:", "Result:", "Resultado:");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.field.medalla", "Medalla / Puesto", "Medal / Place", "Medalha / Posição");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.field.link_video", "Link Video (YouTube)", "Video Link (YouTube)", "Link do Vídeo (YouTube)");
        agregarSiNoExiste(lista, clavesExistentes, "campeonatos.proximos", "Mis Próximos Campeonatos", "My Upcoming Championships", "Meus Próximos Campeonatos");

        // ================== CAMPOS ==================
        agregarSiNoExiste(lista, clavesExistentes, "campos.titulo", "Campos de Entrenamiento", "Training Camps", "Campos de Treinamento");
        agregarSiNoExiste(lista, clavesExistentes, "campos.btn.programar", "Programar Campo", "Schedule Camp", "Agendar Campo");
        agregarSiNoExiste(lista, clavesExistentes, "campos.grid.nombre", "Campo / Evento", "Camp / Event", "Campo / Evento");
        agregarSiNoExiste(lista, clavesExistentes, "campos.estado.en_curso", "En Curso", "In Progress", "Em Andamento");
        agregarSiNoExiste(lista, clavesExistentes, "campos.btn.certificar", "Certificar Cumplimiento", "Certify Compliance", "Certificar Cumprimento");
        agregarSiNoExiste(lista, clavesExistentes, "campos.dialog.programar.titulo", "Programar Campo", "Schedule Camp", "Agendar Campo");
        agregarSiNoExiste(lista, clavesExistentes, "campos.field.nombre", "Nombre del Campo", "Camp Name", "Nome do Campo");
        agregarSiNoExiste(lista, clavesExistentes, "campos.placeholder.ej_campamento", "Ej: Campamento de Altura", "Ex: Altitude Camp", "Ex: Acampamento de Altitude");
        agregarSiNoExiste(lista, clavesExistentes, "campos.field.lugar", "Ubicación", "Location", "Localização");
        agregarSiNoExiste(lista, clavesExistentes, "campos.field.objetivo", "Enfoque / Objetivo", "Focus / Goal", "Foco / Objetivo");
        agregarSiNoExiste(lista, clavesExistentes, "campos.placeholder.ej_tactico", "Ej: Táctico Competitivo", "Ex: Competitive Tactical", "Ex: Tático Competitivo");
        agregarSiNoExiste(lista, clavesExistentes, "campos.field.convocados", "Convocados", "Invited", "Convocados");
        agregarSiNoExiste(lista, clavesExistentes, "campos.msg.programado", "Campo programado para", "Camp scheduled for", "Campo agendado para");
        agregarSiNoExiste(lista, clavesExistentes, "campos.dialog.certificar.titulo", "Certificar:", "Certify:", "Certificar:");
        agregarSiNoExiste(lista, clavesExistentes, "campos.label.pregunta_cumplimiento", "¿El judoka completó satisfactoriamente el campo?", "Did the judoka complete the camp successfully?", "O judoca completou o campo satisfatoriamente?");
        agregarSiNoExiste(lista, clavesExistentes, "campos.field.puntos_ascenso", "Puntos de Ascenso", "Promotion Points", "Pontos de Promoção");
        agregarSiNoExiste(lista, clavesExistentes, "campos.btn.confirmar_puntos", "Certificar y Otorgar Puntos", "Certify and Award Points", "Certificar e Atribuir Pontos");

        // ================== INVENTARIO ==================
        agregarSiNoExiste(lista, clavesExistentes, "inventario.titulo", "Tienda del Dojo", "Dojo Store", "Loja do Dojo");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.btn.nuevo", "Nuevo Producto", "New Product", "Novo Produto");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.grid.articulo", "Artículo", "Item", "Artigo");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.grid.stock", "Stock", "Stock", "Estoque");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.grid.venta", "Precio Venta", "Selling Price", "Preço Venda");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.grid.costo", "Costo", "Cost", "Custo");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.status.agotado", "AGOTADO", "OUT OF STOCK", "ESGOTADO");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.tooltip.add_stock", "Agregar Stock", "Add Stock", "Adicionar Estoque");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.dialog.venta", "Registrar Venta", "Register Sale", "Registrar Venda");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.msg.venta_ok", "Venta registrada y descontada del inventario", "Sale registered and deducted from inventory", "Venda registrada e deduzida do estoque");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.dialog.stock", "Reabastecer Stock", "Restock", "Reabastecer Estoque");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.field.cantidad_ingreso", "Cantidad a Ingresar", "Quantity to Add", "Quantidade a Adicionar");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.dialog.nuevo", "Nuevo Producto", "New Product", "Novo Produto");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.dialog.editar", "Editar Producto", "Edit Product", "Editar Produto");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.field.costo", "Costo Compra ($)", "Purchase Cost ($)", "Custo Compra ($)");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.field.precio", "Precio Venta ($)", "Selling Price ($)", "Preço Venda ($)");
        agregarSiNoExiste(lista, clavesExistentes, "inventario.field.stock_inicial", "Stock Inicial", "Initial Stock", "Estoque Inicial");

        // ================== TESORERÍA ==================
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.titulo", "Gestión Financiera", "Financial Management", "Gestão Financeira");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.tab.registrar_ingreso", "Registrar Ingreso", "Register Income", "Registrar Receita");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.tab.registrar_gasto", "Registrar Gasto", "Register Expense", "Registrar Despesa");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.tab.balance_reportes", "Balance y Reportes", "Balance and Reports", "Balanço e Relatórios");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.alumno", "Alumno", "Student", "Aluno");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.concepto", "Concepto", "Concept", "Conceito");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.valor", "Valor ($)", "Amount ($)", "Valor ($)");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.valor_pagado", "Valor Pagado ($)", "Amount Paid ($)", "Valor Pago ($)");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.metodo_pago", "Método de Pago", "Payment Method", "Método de Pagamento");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.observacion", "Observación", "Observation", "Observação");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.categoria_gasto", "Categoría de Gasto", "Expense Category", "Categoria de Despesa");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.detalle_proveedor", "Detalle / Proveedor", "Detail / Supplier", "Detalhe / Fornecedor");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.foto_factura", "Foto de Factura", "Invoice Photo", "Foto da Fatura");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.soporte", "Soporte", "Support", "Suporte");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.boton.registrar_generar_recibo", "Registrar y Generar Recibo", "Register and Generate Receipt", "Registrar e Gerar Recibo");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.boton.registrar_salida", "Registrar Salida", "Register Outflow", "Registrar Saída");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.boton.guardar", "Guardar", "Save", "Salvar");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.kpi.ingresos_mes", "Ingresos Mes", "Monthly Income", "Receitas do Mês");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.kpi.egresos_mes", "Egresos Mes", "Monthly Expenses", "Despesas do Mês");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.kpi.balance", "Balance", "Balance", "Balanço");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.grid.fecha", "Fecha", "Date", "Data");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.grid.tipo", "Tipo", "Type", "Tipo");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.grid.concepto", "Concepto", "Concept", "Conceito");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.grid.monto", "Monto", "Amount", "Valor");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.grid.judoka", "Judoka", "Judoka", "Judoca");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.grid.soporte", "Soporte", "Support", "Suporte");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.dialog.nuevo_concepto.titulo", "Nuevo Concepto de", "New Concept for", "Novo Conceito de");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.dialog.nuevo_concepto.nombre", "Nombre del Concepto", "Concept Name", "Nome do Conceito");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.dialog.nuevo_concepto.valor_sugerido", "Valor Sugerido", "Suggested Value", "Valor Sugerido");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.validacion.concepto_monto", "Concepto y Monto son obligatorios", "Concept and Amount are required", "Conceito e Valor são obrigatórios");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.validacion.categoria_monto", "Categoría y Monto obligatorios", "Category and Amount are required", "Categoria e Valor são obrigatórios");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.notificacion.ingreso_exitoso", "Ingreso registrado con éxito", "Income registered successfully", "Receita registrada com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.notificacion.soporte_cargado", "Soporte cargado", "Support uploaded", "Suporte carregado");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.notificacion.error_subir", "Error al subir: ", "Error uploading: ", "Erro ao enviar: ");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.notificacion.gasto_registrado", "Gasto registrado", "Expense registered", "Despesa registrada");
        agregarSiNoExiste(lista, clavesExistentes, "tesoreria.notificacion.concepto_creado", "Concepto Creado", "Concept Created", "Conceito Criado");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.beneficiario", "Beneficiario", "Beneficiary", "Beneficiário");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.msg.nequi_cargado", "Comprobante Nequi cargado con éxito", "Nequi receipt uploaded successfully", "Comprovante Nequi enviado com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.compromisos_pendientes", "Compromisos Pendientes", "Pending Commitments", "Compromissos Pendentes");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.btn.confirmar_pago", "Confirmar Pago", "Confirm Payment", "Confirmar Pagamento");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.codigo_aprobacion_nequi", "Código de Aprobación (Solo Nequi)", "Approval Code (Nequi only)", "Código de Aprovação (Apenas Nequi)");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.msg.una_transferencia", "Realiza UNA SOLA transferencia por", "Make ONLY ONE transfer for", "Faça APENAS UMA transferência para");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.referencia_nequi", "Referencia Nequi o URL del Comprobante", "Nequi Reference or Receipt URL", "Referência Nequi ou URL do Comprovante");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.btn.enviar_soporte", "Enviar Soporte", "Send Support", "Enviar Suporte");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.estado_cuenta", "Estado de Cuenta", "Account Status", "Status da Conta");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.pago_pendiente", "Pago Pendiente", "Pending Payment", "Pagamento Pendente");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.pendiente_pago", "Pendiente de Pago", "Pending Payment", "Pendente de Pagamento");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.total_pagar", "Total a Pagar", "Total to Pay", "Total a Pagar");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.btn.subir_nequi", "Subir Pantallazo Nequi", "Upload Nequi Screenshot", "Enviar Captura de Tela Nequi");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.btn.ver_recibo_nequi", "Ver Recibo Nequi", "View Nequi Receipt", "Ver Recibo Nequi");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.msg.comprobante_exito", "¡Comprobante subido con éxito!", "Receipt uploaded successfully!", "Comprovante enviado com sucesso!");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.msg.grupo_familiar_activado", "¡Grupo familiar activado y finanzas procesadas exitosamente!", "Family group activated and finances processed successfully!", "Grupo familiar ativado e finanças processadas com sucesso!");

        // ================== ADMISIONES ==================
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.titulo", "Validación de Ingresos", "Admission Validation", "Validação de Ingressos");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.descripcion", "Revise los documentos y pagos de los aspirantes.", "Review applicants' documents and payments.", "Revise os documentos e pagamentos dos candidatos.");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.grid.registrado", "Registrado", "Registered", "Registrado");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.grid.documentos", "Documentos", "Documents", "Documentos");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.grid.pago", "Pago Matrícula", "Enrollment Payment", "Pagamento Matrícula");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.btn.marcar_pago", "Marcar Pago Manual", "Mark Manual Payment", "Marcar Pagamento Manual");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.msg.activado", "¡Judoka activado con éxito!", "Judoka activated successfully!", "Judoca ativado com sucesso!");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.msg.rechazado", "Aspirante rechazado.", "Applicant rejected.", "Candidato rejeitado.");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.btn.anadir_deportista", "Añadir otro deportista", "Add another athlete", "Adicionar outro atleta");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.en_revision_auditoria", "En Revisión (Auditoría)", "Under Review (Audit)", "Em Revisão (Auditoria)");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.rechazado_master", "Rechazado por el Master", "Rejected by Master", "Rejeitado pelo Master");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.msg.disponible_aprobacion", "Disponible cuando el Master apruebe el ingreso", "Available when the Master approves entry", "Disponível quando o Master aprovar a entrada");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.btn.subir_eps", "Subir EPS", "Upload Health Insurance", "Enviar Comprovante de Convênio");
        agregarSiNoExiste(lista, clavesExistentes, "admisiones.btn.subir_waiver", "Subir Waiver firmado", "Upload signed Waiver", "Enviar Waiver assinado");

        // ================== ERRORES ==================
        agregarSiNoExiste(lista, clavesExistentes, "error.generic", "Ha ocurrido un error", "An error occurred", "Ocorreu um erro");
        agregarSiNoExiste(lista, clavesExistentes, "error.upload", "Error al subir archivo", "Error uploading file", "Erro ao enviar arquivo");
        agregarSiNoExiste(lista, clavesExistentes, "error.campos_obligatorios", "Campos obligatorios incompletos", "Required fields missing", "Campos obrigatórios incompletos");
        agregarSiNoExiste(lista, clavesExistentes, "error.campos_incompletos", "Por favor, llene todos los campos", "Please fill all fields", "Por favor, preencha todos os campos");
        agregarSiNoExiste(lista, clavesExistentes, "error.contrasenas_no_coinciden", "Las contraseñas no coinciden", "Passwords do not match", "As senhas não coincidem");
        agregarSiNoExiste(lista, clavesExistentes, "error.usuario.existe", "Este correo ya está registrado", "This email is already registered", "Este e-mail já está registrado");
        agregarSiNoExiste(lista, clavesExistentes, "error.judoka_no_autenticado", "Judoka no autenticado", "Unauthenticated Judoka", "Judoca não autenticado");
        agregarSiNoExiste(lista, clavesExistentes, "error.judoka_no_encontrado", "Judoka no encontrado", "Judoka not found", "Judoca não encontrado");
        agregarSiNoExiste(lista, clavesExistentes, "error.no_judoka_autenticado", "No hay judoka autenticado", "No authenticated judoka", "Nenhum judoca autenticado");
        agregarSiNoExiste(lista, clavesExistentes, "error.sin_permiso_perfil", "No tienes permiso para ver este perfil", "You do not have permission to view this profile", "Você não tem permissão para ver este perfil");
        agregarSiNoExiste(lista, clavesExistentes, "error.envio", "Error en el envío", "Sending error", "Erro no envio");

        // ================== MENSAJES DE ÉXITO ==================
        agregarSiNoExiste(lista, clavesExistentes, "msg.success.saved", "Guardado exitosamente", "Saved successfully", "Salvo com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.success.updated", "Actualizado exitosamente", "Updated successfully", "Atualizado com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.success.deleted", "Eliminado exitosamente", "Deleted successfully", "Excluído com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.success.config_saved", "Configuración guardada correctamente", "Configuration saved successfully", "Configuração salva com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.foto.actualizada", "Foto actualizada correctamente", "Photo updated successfully", "Foto atualizada com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.diario.vacio", "Tu diario está vacío. Empieza hoy.", "Your diary is empty. Start today.", "Seu diário está vazio. Comece hoje.");
        agregarSiNoExiste(lista, clavesExistentes, "msg.entrada.actualizada", "Entrada actualizada.", "Entry updated.", "Entrada atualizada.");
        agregarSiNoExiste(lista, clavesExistentes, "msg.exito.archivo_subido", "Documento guardado correctamente", "Document saved successfully", "Documento salvo com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.exito.puede_continuar", "¡Excelente! Ya puedes finalizar", "Great! You can now finish", "Ótimo! Você pode finalizar agora");
        agregarSiNoExiste(lista, clavesExistentes, "msg.error.nube", "Error al conectar con la Nube", "Error connecting to the Cloud", "Erro ao conectar com a Nuvem");
        agregarSiNoExiste(lista, clavesExistentes, "msg.excelente.trabajo", "¡Excelente trabajo!", "Excellent work!", "Excelente trabalho!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.error.guardar", "Error al guardar", "Error saving", "Erro ao salvar");
        agregarSiNoExiste(lista, clavesExistentes, "msg.dia.descanso", "Hoy es día de descanso. ¡Recupérate!", "Today is a rest day. Recover!", "Hoje é dia de descanso. Recupere-se!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.entrenamiento.finalizado", "¡Entrenamiento del día finalizado!", "Daily training finished!", "Treino do dia finalizado!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.error.asignacion", "Error al asignar", "Error assigning", "Erro ao atribuir");
        agregarSiNoExiste(lista, clavesExistentes, "msg.exito.asignacion", "Judoka asignado correctamente", "Judoka assigned successfully", "Judoca atribuído com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "msg.error.remocion", "Error al remover", "Error removing", "Erro ao remover");
        agregarSiNoExiste(lista, clavesExistentes, "msg.exito.remocion", "Judoka removido del grupo", "Judoka removed from group", "Judoca removido do grupo");
        agregarSiNoExiste(lista, clavesExistentes, "msg.selecciona.categoria.para.comparar", "Selecciona una categoría arriba para ver tu evolución.", "Select a category above to see your progress.", "Selecione uma categoria acima para ver sua evolução.");
        agregarSiNoExiste(lista, clavesExistentes, "msg.enlace_copiado", "Enlace copiado al portapapeles", "Link copied to clipboard", "Link copiado para a área de transferência");
        agregarSiNoExiste(lista, clavesExistentes, "msg.configurado_exito", "¡Configurado con éxito!", "Configured successfully!", "Configurado com sucesso!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.subido_exito", "¡Subido con éxito!", "Uploaded successfully!", "Enviado com sucesso!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.bienvenido_a", "¡Bienvenido a", "Welcome to", "Bem-vindo a");
        agregarSiNoExiste(lista, clavesExistentes, "msg.bienvenido_academia", "¡Bienvenido a la Academia!", "Welcome to the Academy!", "Bem-vindo à Academia!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.bienvenido_saas", "¡Bienvenido a tu nuevo SaaS!", "Welcome to your new SaaS!", "Bem-vindo ao seu novo SaaS!");
        agregarSiNoExiste(lista, clavesExistentes, "msg.bienvenido_dojo", "¡Bienvenido al Dojo!", "Welcome to the Dojo!", "Bem-vindo ao Dojo!");
        agregarSiNoExiste(lista, clavesExistentes, "generic.hola_exclamacion", "¡Hola", "Hello", "Olá");
        agregarSiNoExiste(lista, clavesExistentes, "generic.msg.recibido", "¡Recibido!", "Received!", "Recebido!");

        // ================== REGISTRO / LOGIN ==================
        agregarSiNoExiste(lista, clavesExistentes, "registro.titulo", "Registro de Aspirante", "Applicant Registration", "Registro de Candidato");
        agregarSiNoExiste(lista, clavesExistentes, "registro.subtitulo", "Únete a nuestro Dojo", "Join our Dojo", "Junte-se ao nosso Dojo");
        agregarSiNoExiste(lista, clavesExistentes, "registro.btn.siguiente", "Siguiente", "Next", "Próximo");
        agregarSiNoExiste(lista, clavesExistentes, "registro.btn.volver", "Ya tengo cuenta", "I already have an account", "Já tenho conta");
        agregarSiNoExiste(lista, clavesExistentes, "registro.exito", "Registro Exitoso. Inicia Sesión.", "Registration successful. Log in.", "Registro bem-sucedido. Faça login.");
        agregarSiNoExiste(lista, clavesExistentes, "registro.email_usuario", "Email (Será su Usuario)", "Email (Will be your Username)", "E-mail (Será seu Usuário)");
        agregarSiNoExiste(lista, clavesExistentes, "registro.email", "Email (Usuario)", "Email (Username)", "E-mail (Usuário)");
        agregarSiNoExiste(lista, clavesExistentes, "registro.crear_dojo", "Crear mi Dojo", "Create my Dojo", "Criar meu Dojo");
        agregarSiNoExiste(lista, clavesExistentes, "registro.crear_cuenta", "Crear una Cuenta", "Create an Account", "Criar uma Conta");
        agregarSiNoExiste(lista, clavesExistentes, "registro.crear_contrasena", "Crea tu Contraseña", "Create your Password", "Crie sua Senha");
        agregarSiNoExiste(lista, clavesExistentes, "registro.profesor_nuevo_dojo", "Profesor de Judo (Nuevo Dojo)", "Judo Teacher (New Dojo)", "Professor de Judô (Novo Dojo)");
        agregarSiNoExiste(lista, clavesExistentes, "registro.tu_profesor_judo", "tu Profesor de Judo", "your Judo Teacher", "seu Professor de Judô");
        agregarSiNoExiste(lista, clavesExistentes, "login.btn.registrar", "¿No tienes cuenta? Regístrate aquí.", "Don't have an account? Register here.", "Não tem conta? Registre-se aqui.");
        agregarSiNoExiste(lista, clavesExistentes, "login.form.titulo", "Iniciar Sesión", "Sign In", "Entrar");
        agregarSiNoExiste(lista, clavesExistentes, "login.lbl.usuario", "Usuario", "Username", "Usuário");
        agregarSiNoExiste(lista, clavesExistentes, "login.lbl.password", "Contraseña", "Password", "Senha");
        agregarSiNoExiste(lista, clavesExistentes, "login.btn.ingresar", "Entrar", "Log in", "Entrar");
        agregarSiNoExiste(lista, clavesExistentes, "login.link.olvido", "¿Olvidaste tu contraseña?", "Forgot password?", "Esqueceu a senha?");
        agregarSiNoExiste(lista, clavesExistentes, "login.error.titulo", "Usuario o contraseña incorrectos", "Incorrect username or password", "Usuário ou senha incorretos");
        agregarSiNoExiste(lista, clavesExistentes, "login.error.mensaje", "Por favor, verifica tus credenciales.", "Please check your credentials.", "Por favor, verifique suas credenciais.");
        agregarSiNoExiste(lista, clavesExistentes, "login.btn.ir_inicio", "Ir al inicio de sesión", "Go to login", "Ir para o login");

        // ================== LABELS ==================
        agregarSiNoExiste(lista, clavesExistentes, "label.fecha_nacimiento", "Fecha de Nacimiento", "Birth Date", "Data de Nascimento");
        agregarSiNoExiste(lista, clavesExistentes, "label.peso_kg", "Peso (kg)", "Weight (kg)", "Peso (kg)");
        agregarSiNoExiste(lista, clavesExistentes, "label.contrasena", "Contraseña", "Password", "Senha");
        agregarSiNoExiste(lista, clavesExistentes, "label.confirmar_contrasena", "Confirmar Contraseña", "Confirm Password", "Confirmar Senha");

        // ================== BIBLIOTECA ==================
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.titulo", "Biblioteca de Tareas Diarias", "Daily Tasks Library", "Biblioteca de Tarefas Diárias");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.boton.nueva_tarea", "Añadir Nueva Tarea", "Add New Task", "Adicionar Nova Tarefa");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.nombre_tarea", "Nombre Tarea", "Task Name", "Nome da Tarefa");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.meta", "Meta", "Goal", "Meta");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.descripcion", "Descripción", "Description", "Descrição");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.video", "Video", "Video", "Vídeo");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.tooltip.tiene_video", "Tiene video", "Has video", "Tem vídeo");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.tooltip.editar_tarea", "Editar tarea", "Edit task", "Editar tarefa");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.grid.acciones", "Acciones", "Actions", "Ações");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.error.sensei_no_autenticado", "Sensei no autenticado", "Sensei not authenticated", "Sensei não autenticado");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.notificacion.tarea_guardada", "Tarea guardada: %s", "Task saved: %s", "Tarefa salva: %s");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.notificacion.error_guardar", "Error al guardar: ", "Error saving: ", "Erro ao salvar: ");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.descripcion_ejecucion", "Descripción (¿Cómo se ejecuta?)", "Description (How is it executed?)", "Descrição (Como é executado?)");
        agregarSiNoExiste(lista, clavesExistentes, "biblioteca.error.nombre_tarea", "El nombre de la tarea es obligatorio", "The task name is mandatory", "O nome da tarefa é obrigatório");

        // ================== COMUNIDAD ==================
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.tab.muro", "Muro del Dojo", "Dojo Wall", "Mural do Dojo");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.tab.chat", "Chat Grupal", "Group Chat", "Chat em Grupo");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.post.placeholder", "Comparte algo con el dojo...", "Share something with the dojo...", "Compartilhe algo com o dojo...");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.btn.subir_foto", "Subir Foto/Video", "Upload Photo/Video", "Enviar Foto/Vídeo");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.label.drop", "Arrastra archivos aquí...", "Drag files here...", "Arraste arquivos aqui...");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.btn.publicar", "Publicar", "Post", "Publicar");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.msg.publicado", "¡Publicado en el muro!", "Posted on the wall!", "Publicado no mural!");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.msg.archivo_listo", "Archivo listo", "File ready", "Arquivo pronto");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.warn.empty_post", "Escribe algo o sube una foto", "Write something or upload a photo", "Escreva algo ou envie uma foto");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.btn.comentar", "Comentar", "Comment", "Comentar");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.comment.placeholder", "Escribe una respuesta...", "Write a reply...", "Escreva uma resposta...");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.msg.comment_sent", "Comentario enviado", "Comment sent", "Comentário enviado");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.label.image_of", "Imagen de", "Image of", "Imagem de");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.chat.escribir", "Escribe un mensaje...", "Type a message...", "Digite uma mensagem...");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.chat.enviar", "Enviar", "Send", "Enviar");
        agregarSiNoExiste(lista, clavesExistentes, "comunidad.imagen_post", "Imagen Post", "Post Image", "Imagem do Post");

        // ================== AYUDA ==================
        agregarSiNoExiste(lista, clavesExistentes, "help.poder_combate.titulo", "¿Qué es el Poder de Combate?", "What is Combat Power?", "O que é Poder de Combate?");
        agregarSiNoExiste(lista, clavesExistentes, "help.poder_combate.contenido",
                "El Poder de Combate es un indicador global (1000-5000) que resume tu rendimiento físico basado en las pruebas evaluadas. Se calcula a partir de las clasificaciones (Excelente, Bueno, etc.) de cada prueba, agrupadas en los 5 bloques de Agudelo. El radar muestra tu nivel actual en cada bloque (de 1 a 5 estrellas). Las líneas de motivador en las gráficas representan el siguiente escalón a alcanzar según normas científicas (PROESP/CBJ) o el récord del dojo.",
                "Combat Power is a global indicator (1000-5000) that summarizes your physical performance based on evaluated tests. It is calculated from the classifications (Excellent, Good, etc.) of each test, grouped into Agudelo's 5 blocks. The radar shows your current level in each block (1 to 5 stars). The motivator lines in the charts represent the next step to achieve according to scientific norms (PROESP/CBJ) or the dojo record.",
                "O Poder de Combate é um indicador global (1000-5000) que resume seu desempenho físico com base nos testes avaliados. É calculado a partir das classificações (Excelente, Bom, etc.) de cada teste, agrupadas nos 5 blocos de Agudelo. O radar mostra seu nível atual em cada bloco (de 1 a 5 estrelas). As linhas motivadoras nos gráficos representam o próximo degrau a alcançar de acordo com normas científicas (PROESP/CBJ) ou o recorde do dojo.");
        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.titulo", "Sabiduría del Sensei", "Sensei's Wisdom", "Sabedoria do Sensei");

        // ================== ENUMS (YA DEBERÍAN ESTAR, PERO LOS DEJAMOS POR SI ACASO) ==================
        // Grados de cinturón
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.blanco", "Blanco", "White", "Branco");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.amarillo", "Amarillo", "Yellow", "Amarelo");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.naranja", "Naranja", "Orange", "Laranja");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.verde", "Verde", "Green", "Verde");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.azul", "Azul", "Blue", "Azul");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.marron", "Marrón", "Brown", "Marrom");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_1_dan", "Negro 1 Dan", "Black 1st Dan", "Preto 1 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_2_dan", "Negro 2 Dan", "Black 2nd Dan", "Preto 2 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_3_dan", "Negro 3 Dan", "Black 3rd Dan", "Preto 3 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_4_dan", "Negro 4 Dan", "Black 4th Dan", "Preto 4 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_5_dan", "Negro 5 Dan", "Black 5th Dan", "Preto 5 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_6_dan", "Negro 6 Dan", "Black 6th Dan", "Preto 6 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_7_dan", "Negro 7 Dan", "Black 7th Dan", "Preto 7 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_8_dan", "Negro 8 Dan", "Black 8th Dan", "Preto 8 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_9_dan", "Negro 9 Dan", "Black 9th Dan", "Preto 9 Dan");
        agregarSiNoExiste(lista, clavesExistentes, "enum.gradocinturon.negro_10_dan", "Negro 10 Dan", "Black 10th Dan", "Preto 10 Dan");

        // Estado judoka
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadojudoka.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadojudoka.en_revision", "En revisión", "Under review", "Em revisão");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadojudoka.activo", "Activo", "Active", "Ativo");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadojudoka.inactivo", "Inactivo", "Inactive", "Inativo");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadojudoka.rechazado", "Rechazado", "Rejected", "Rejeitado");

        // Tipo documento
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipodocumento.waiver", "Exoneración", "Waiver", "Termo de responsabilidade");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipodocumento.certificado_medico", "Certificado médico", "Medical certificate", "Atestado médico");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipodocumento.eps", "EPS", "EPS", "EPS");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipodocumento.documento_identidad", "Documento de identidad", "Identity document", "Documento de identidade");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipodocumento.comprobante_pago", "Comprobante de pago", "Payment receipt", "Comprovante de pagamento");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipodocumento.otro", "Otro", "Other", "Outro");

        // Tipo transacción
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipotransaccion.ingreso", "Ingreso", "Income", "Receita");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipotransaccion.egreso", "Egreso", "Expense", "Despesa");

        // Nivel organizacional
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelorganizacional.club", "Club", "Club", "Clube");
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelorganizacional.liga", "Liga", "League", "Liga");
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelorganizacional.federacion", "Federación", "Federation", "Federação");

        // Estado microciclo
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadomicrociclo.activo", "Activo", "Active", "Ativo");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadomicrociclo.completado", "Completado", "Completed", "Concluído");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadomicrociclo.cancelado", "Cancelado", "Canceled", "Cancelado");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadomicrociclo.borrador", "Borrador", "Draft", "Rascunho");

        // Categoría ejercicio
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.medicion_antropometrica", "Medición antropométrica", "Anthropometric measurement", "Medição antropométrica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.potencia", "Potencia", "Power", "Potência");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.velocidad", "Velocidad", "Speed", "Velocidade");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.resistencia_dinamica", "Resistencia dinámica", "Dynamic endurance", "Resistência dinâmica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.resistencia_muscular_localizada", "Resistencia muscular localizada", "Localized muscular endurance", "Resistência muscular localizada");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.resistencia_isometrica", "Resistencia isométrica", "Isometric endurance", "Resistência isométrica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.aptitud_anaerobica", "Aptitud anaeróbica", "Anaerobic fitness", "Aptidão anaeróbica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.aptitud_aerobica", "Aptitud aeróbica", "Aerobic fitness", "Aptidão aeróbica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.flexibilidad", "Flexibilidad", "Flexibility", "Flexibilidade");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.agilidad", "Agilidad", "Agility", "Agilidade");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.tecnica", "Técnica", "Technique", "Técnica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriaejercicio.anticipacion", "Anticipación", "Anticipation", "Antecipação");

        // Tipo microciclo
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.corriente", "Corriente", "Ordinary", "Corrente");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.choque", "Choque", "Shock", "Choque");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.aproximacion", "Aproximación", "Approach", "Aproximação");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.competitivo", "Competitivo", "Competitive", "Competitivo");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.restauracion", "Restauración", "Restoration", "Restauração");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.ajuste", "Ajuste", "Adjustment", "Ajuste");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomicrociclo.control", "Control", "Control", "Controle");

        // Nivel competencia
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelcompetencia.local", "Local", "Local", "Local");
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelcompetencia.departamental", "Departamental", "Departmental", "Departamental");
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelcompetencia.nacional", "Nacional", "National", "Nacional");
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelcompetencia.internacional", "Internacional", "International", "Internacional");
        agregarSiNoExiste(lista, clavesExistentes, "enum.nivelcompetencia.club", "Club", "Club", "Clube");

        // Tipo suscripción
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposuscripcion.mensual", "Mensual", "Monthly", "Mensal");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposuscripcion.bimensual", "Bimensual", "Bimonthly", "Bimestral");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposuscripcion.trimestral", "Trimestral", "Quarterly", "Trimestral");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposuscripcion.semestral", "Semestral", "Semiannual", "Semestral");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposuscripcion.anual", "Anual", "Annual", "Anual");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposuscripcion.pago_unico", "Pago único", "Single payment", "Pagamento único");

        // Tipo sesión
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.tecnica", "Técnica", "Technique", "Técnica");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.randori", "Randori", "Randori", "Randori");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.uchikomi", "Uchikomi", "Uchikomi", "Uchikomi");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.nagekomi", "Nagekomi", "Nagekomi", "Nagekomi");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.shiai", "Shiai", "Shiai", "Shiai");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.mondokai", "Mondokai", "Mondokai", "Mondokai");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.yakusokugeiko", "Yakusokugeiko", "Yakusokugeiko", "Yakusokugeiko");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.kakarigeiko", "Kakarigeiko", "Kakarigeiko", "Kakarigeiko");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.pesas", "Pesas", "Weights", "Pesos");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.acondicionamiento", "Acondicionamiento", "Conditioning", "Condicionamento");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tiposesion.evaluacion", "Evaluación", "Evaluation", "Avaliação");

        // Resultado competencia
        agregarSiNoExiste(lista, clavesExistentes, "enum.resultadocompetencia.participacion", "Participación", "Participation", "Participação");
        agregarSiNoExiste(lista, clavesExistentes, "enum.resultadocompetencia.oro", "Oro", "Gold", "Ouro");
        agregarSiNoExiste(lista, clavesExistentes, "enum.resultadocompetencia.plata", "Plata", "Silver", "Prata");
        agregarSiNoExiste(lista, clavesExistentes, "enum.resultadocompetencia.bronce", "Bronce", "Bronze", "Bronze");
        agregarSiNoExiste(lista, clavesExistentes, "enum.resultadocompetencia.quinto", "Quinto", "Fifth", "Quinto");
        agregarSiNoExiste(lista, clavesExistentes, "enum.resultadocompetencia.septimo", "Séptimo", "Seventh", "Sétimo");

        // Estado asistencia
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadoasistencia.presente", "Presente", "Present", "Presente");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadoasistencia.ausente", "Ausente", "Absent", "Ausente");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadoasistencia.excusado", "Excusado", "Excused", "Dispensado");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadoasistencia.llegada_tarde", "Llegada tarde", "Late arrival", "Chegada atrasada");

        // Bloque Agudelo
        agregarSiNoExiste(lista, clavesExistentes, "enum.bloqueagudelo.definitorio", "Definitorio", "Defining", "Definitório");
        agregarSiNoExiste(lista, clavesExistentes, "enum.bloqueagudelo.sustento", "Sustento", "Sustenance", "Sustento");
        agregarSiNoExiste(lista, clavesExistentes, "enum.bloqueagudelo.eficiencia", "Eficiencia", "Efficiency", "Eficiência");
        agregarSiNoExiste(lista, clavesExistentes, "enum.bloqueagudelo.proteccion", "Protección", "Protection", "Proteção");
        agregarSiNoExiste(lista, clavesExistentes, "enum.bloqueagudelo.tecnico_coordinativo", "Técnico coordinativo", "Coordinative technical", "Técnico coordenativo");

        // Estado pago
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadopago.pendiente", "Pendiente", "Pending", "Pendente");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadopago.pagado", "Pagado", "Paid", "Pago");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadopago.en_revision", "En revisión", "Under review", "Em revisão");
        agregarSiNoExiste(lista, clavesExistentes, "enum.estadopago.fallido", "Fallido", "Failed", "Falho");

        // Clasificación rendimiento
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.excelente", "Excelente", "Excellent", "Excelente");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.muy_bien", "Muy bien", "Very good", "Muito bom");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.bueno", "Bueno", "Good", "Bom");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.regular", "Regular", "Regular", "Regular");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.razonable", "Razonable", "Reasonable", "Razoável");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.debil", "Débil", "Weak", "Fraco");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.muy_debil", "Muy débil", "Very weak", "Muito fraco");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.zona_de_riesgo", "Zona de riesgo", "Risk zone", "Zona de risco");
        agregarSiNoExiste(lista, clavesExistentes, "enum.clasificacionrendimiento.zona_saludable", "Zona saludable", "Healthy zone", "Zona saudável");

        // Categoría insignia
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriainsignia.shin", "Shin", "Shin", "Shin");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriainsignia.gi", "Gi", "Gi", "Gi");
        agregarSiNoExiste(lista, clavesExistentes, "enum.categoriainsignia.tai", "Tai", "Tai", "Tai");

        // Tipo evento gamificación
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipoeventogamificacion.asistencia", "Asistencia", "Attendance", "Presença");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipoeventogamificacion.resultado_prueba", "Resultado de prueba", "Test result", "Resultado de teste");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipoeventogamificacion.grado_alcanzado", "Grado alcanzado", "Degree achieved", "Grau alcançado");

        // Método pago
        agregarSiNoExiste(lista, clavesExistentes, "enum.metodopago.efectivo", "Efectivo", "Cash", "Dinheiro");
        agregarSiNoExiste(lista, clavesExistentes, "enum.metodopago.transferencia", "Transferencia", "Transfer", "Transferência");
        agregarSiNoExiste(lista, clavesExistentes, "enum.metodopago.tarjeta", "Tarjeta", "Card", "Cartão");
        agregarSiNoExiste(lista, clavesExistentes, "enum.metodopago.nequi", "Nequi", "Nequi", "Nequi");
        agregarSiNoExiste(lista, clavesExistentes, "enum.metodopago.daviplata", "Daviplata", "Daviplata", "Daviplata");

        // Operador comparación
        agregarSiNoExiste(lista, clavesExistentes, "enum.operadorcomparacion.mayor_que", "Mayor que", "Greater than", "Maior que");
        agregarSiNoExiste(lista, clavesExistentes, "enum.operadorcomparacion.menor_que", "Menor que", "Less than", "Menor que");
        agregarSiNoExiste(lista, clavesExistentes, "enum.operadorcomparacion.igual_a", "Igual a", "Equal to", "Igual a");
        agregarSiNoExiste(lista, clavesExistentes, "enum.operadorcomparacion.mayor_o_igual", "Mayor o igual", "Greater than or equal to", "Maior ou igual");
        agregarSiNoExiste(lista, clavesExistentes, "enum.operadorcomparacion.menor_o_igual", "Menor o igual", "Less than or equal to", "Menor ou igual");

        // Sexo
        agregarSiNoExiste(lista, clavesExistentes, "enum.sexo.masculino", "Masculino", "Male", "Masculino");
        agregarSiNoExiste(lista, clavesExistentes, "enum.sexo.femenino", "Femenino", "Female", "Feminino");

        // Mesociclo ATC
        agregarSiNoExiste(lista, clavesExistentes, "enum.mesocicloatc.adquisicion", "Adquisición", "Acquisition", "Aquisição");
        agregarSiNoExiste(lista, clavesExistentes, "enum.mesocicloatc.transferencia", "Transferencia", "Transfer", "Transferência");
        agregarSiNoExiste(lista, clavesExistentes, "enum.mesocicloatc.competencia", "Competencia", "Competition", "Competição");
        agregarSiNoExiste(lista, clavesExistentes, "enum.mesocicloatc.recuperacion", "Recuperación", "Recovery", "Recuperação");
        agregarSiNoExiste(lista, clavesExistentes, "enum.mesocicloatc.refuerzo", "Refuerzo", "Reinforcement", "Reforço");
        agregarSiNoExiste(lista, clavesExistentes, "enum.mesocicloatc.nivelacion", "Nivelación", "Leveling", "Nivelamento");

        // Tipo mecenas
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomecenas.persona_natural", "Persona natural", "Natural person", "Pessoa física");
        agregarSiNoExiste(lista, clavesExistentes, "enum.tipomecenas.empresa", "Empresa", "Company", "Empresa");

        // ================== NUEVAS TRADUCCIONES DE VISTAS (DEL CSV) ==================
        agregarSiNoExiste(lista, clavesExistentes, "sala_espera.titulo", "(Sala de Espera)", "(Waiting Room)", "(Sala de Espera)");
        agregarSiNoExiste(lista, clavesExistentes, "agenda.gps.titulo", "Agenda GPS", "GPS Agenda", "Agenda GPS");
        agregarSiNoExiste(lista, clavesExistentes, "estado.al_dia", "Al día", "Up to date", "Em dia");
        agregarSiNoExiste(lista, clavesExistentes, "aspirante.sin_nombre", "Aspirante Sin Nombre", "Unnamed Applicant", "Candidato Sem Nome");
        agregarSiNoExiste(lista, clavesExistentes, "acudiente.panel", "Panel de Acudiente", "Guardian Panel", "Painel do Responsável");
        agregarSiNoExiste(lista, clavesExistentes, "mecenas.padrino_anonimo", "Padrino Anónimo", "Anonymous Sponsor", "Padrinho Anônimo");
        agregarSiNoExiste(lista, clavesExistentes, "mecenas.patrocinado", "Patrocinado", "Sponsored", "Patrocinado");
        agregarSiNoExiste(lista, clavesExistentes, "mecenas.atletas_apoyados", "Atletas Apoyados", "Supported Athletes", "Atletas Apoiados");
        agregarSiNoExiste(lista, clavesExistentes, "mecenas.tus_ahijados", "Tus Ahijados", "Your Sponsored Athletes", "Seus Afilhados");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.bitacora_fase_r", "Bitácora (Fase R)", "Logbook (Phase R)", "Diário (Fase R)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.bitacora_clases", "Bitácora de Clases", "Class Logbook", "Diário de Aulas");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.bloque", "Bloque", "Block", "Bloco");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.bloque_metodologico", "Bloque Metodológico", "Methodological Block", "Bloco Metodológico");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.descanso_segundos", "Descanso (Segundos)", "Rest (Seconds)", "Descanso (Segundos)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.dictar_observaciones", "Dicta o escribe tus observaciones (Fase R)", "Dictate or write your observations (Phase R)", "Dite ou escreva suas observações (Fase R)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.dosificacion", "Dosificación Específica", "Specific Dosage", "Dosagem Específica");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.duracion_minutos", "Duración (Minutos)", "Duration (Minutes)", "Duração (Minutos)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.ejercicio", "Ejercicio", "Exercise", "Exercício");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.ejercicios_plan", "Ejercicios del Plan", "Plan Exercises", "Exercícios do Plano");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.error.nombre_fase", "Debe ingresar el nombre y la Fase ATC", "You must enter the name and ATC Phase", "Você deve inserir o nome e a Fase ATC");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.error.bloque_obligatorio", "El bloque metodológico es obligatorio", "The methodological block is mandatory", "O bloco metodológico é obrigatório");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.fase_atc", "Fase (ATC)", "Phase (ATC)", "Fase (ATC)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.fase_modelamiento", "Fase de Modelamiento (ATC)", "Modeling Phase (ATC)", "Fase de Modelagem (ATC)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.historial", "Historial de Microciclos", "Microcycles History", "Histórico de Microciclos");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.linea_tiempo", "Línea de Tiempo (Microciclos)", "Timeline (Microcycles)", "Linha do Tempo (Microciclos)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.activo", "Microciclo Activo", "Active Microcycle", "Microciclo Ativo");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.msg.guardado", "Microciclo guardado exitosamente", "Microcycle saved successfully", "Microciclo salvo com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.plural", "Microciclos", "Microcycles", "Microciclos");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.nombre", "Nombre del Microciclo", "Microcycle Name", "Nome do Microciclo");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.repeticiones", "Repeticiones", "Repetitions", "Repetições");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.tipo", "Tipo de Microciclo", "Microcycle Type", "Tipo de Microciclo");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.trabajo_segundos", "Trabajo (Segundos)", "Work (Seconds)", "Trabalho (Segundos)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.vacio_taller", "Vacío (Agrega microciclos desde el Taller)", "Empty (Add microcycles from the Workshop)", "Vazio (Adicione microciclos da Oficina)");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.btn.guardar", "Guardar Microciclo", "Save Microcycle", "Salvar Microciclo");
        agregarSiNoExiste(lista, clavesExistentes, "microciclo.btn.nuevo", "Nuevo Microciclo", "New Microcycle", "Novo Microciclo");

        agregarSiNoExiste(lista, clavesExistentes, "macrociclo.titulo", "Macrociclo", "Macrocycle", "Macrociclo");
        agregarSiNoExiste(lista, clavesExistentes, "macrociclo.opcional", "Macrociclo (Opcional)", "Macrocycle (Optional)", "Macrociclo (Opcional)");
        agregarSiNoExiste(lista, clavesExistentes, "macrociclo.msg.guardado", "Macrociclo guardado", "Macrocycle saved", "Macrociclo salvo");
        agregarSiNoExiste(lista, clavesExistentes, "macrociclo.mis_macrociclos", "Mis Macrociclos (Temporadas)", "My Macrocycles (Seasons)", "Meus Macrociclos (Temporadas)");
        agregarSiNoExiste(lista, clavesExistentes, "macrociclo.btn.nuevo", "Nuevo Macrociclo", "New Macrocycle", "Novo Macrociclo");
        agregarSiNoExiste(lista, clavesExistentes, "macrociclo.objetivo_principal", "Objetivo Principal", "Main Objective", "Objetivo Principal");

        agregarSiNoExiste(lista, clavesExistentes, "sensei.mis_deportistas", "Mis Deportistas", "My Athletes", "Meus Atletas");
        agregarSiNoExiste(lista, clavesExistentes, "invitaciones.titulo", "Centro de Invitaciones", "Invitation Center", "Centro de Convites");
        agregarSiNoExiste(lista, clavesExistentes, "invitaciones.btn.invitar_otro", "Invitar a otro contacto", "Invite another contact", "Convidar outro contato");
        agregarSiNoExiste(lista, clavesExistentes, "invitaciones.msg.whatsapp_listo", "Mensaje listo para WhatsApp", "Message ready for WhatsApp", "Mensagem pronta para WhatsApp");
        agregarSiNoExiste(lista, clavesExistentes, "invitaciones.msg.enlace_generado", "¡Enlace Generado!", "Link Generated!", "Link Gerado!");
        agregarSiNoExiste(lista, clavesExistentes, "invitaciones.error.pase_invalido", "Pase Mágico Inválido o Expirado", "Invalid or Expired Magic Pass", "Passe Mágico Inválido ou Expirado");
        agregarSiNoExiste(lista, clavesExistentes, "invitaciones.a_quien_invitar", "¿A quién deseas invitar?", "Who do you want to invite?", "Quem você deseja convidar?");

        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.error.unidad_medida", "Debe seleccionar al menos una unidad de medida", "You must select at least one unit of measurement", "Você deve selecionar pelo menos uma unidade de medida");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.distancia_cm", "Distancia (cm)", "Distance (cm)", "Distância (cm)");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.titulo", "Evaluaciones", "Evaluations", "Avaliações");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.msg.guardada", "Evaluación guardada exitosamente", "Evaluation saved successfully", "Avaliação salva com sucesso");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.btn.evalua", "Evalúa", "Evaluate", "Avaliar");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.error.pruebas_globales", "Las pruebas globales no se pueden editar", "Global tests cannot be edited", "Testes globais não podem ser editados");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.metrica_solo_pruebas", "Métrica (solo para pruebas)", "Metric (only for tests)", "Métrica (apenas para testes)");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.nombre", "Nombre de la Evaluación", "Evaluation Name", "Nome da Avaliação");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.btn.nueva_prueba", "Nueva Prueba", "New Test", "Novo Teste");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.objetivo_mide", "Objetivo (¿Qué mide?)", "Objective (What does it measure?)", "Objetivo (O que mede?)");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.prueba_autor", "Prueba de Autor (Sensei)", "Author Test (Sensei)", "Teste de Autor (Sensei)");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.error.no_encontrada", "Prueba no encontrada", "Test not found", "Teste não encontrado");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.tiempo_s", "Tiempo (s)", "Time (s)", "Tempo (s)");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.unidades_medida", "Unidades de Medida a Evaluar", "Measurement Units to Evaluate", "Unidades de Medida a Avaliar");
        agregarSiNoExiste(lista, clavesExistentes, "evaluacion.valor_objetivo", "Valor Objetivo", "Target Value", "Valor Objetivo");

        agregarSiNoExiste(lista, clavesExistentes, "tatami.btn.entrar", "ENTRAR AL TATAMI", "ENTER THE TATAMI", "ENTRAR NO TATAME");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.btn.iniciar_cronometro", "Iniciar Cronómetro", "Start Timer", "Iniciar Cronômetro");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.btn.iniciar_personalizado", "Iniciar Personalizado", "Start Custom", "Iniciar Personalizado");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.btn.pausar", "PAUSAR", "PAUSE", "PAUSAR");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.toca_pantalla", "TOCA LA PANTALLA PARA INICIAR", "TOUCH SCREEN TO START", "TOQUE NA TELA PARA INICIAR");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.randori_oficial", "Randori Oficial (4' x 1')", "Official Randori (4' x 1')", "Randori Oficial (4' x 1')");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.uchikomi_30_10", "Uchikomi (30'' x 10'')", "Uchikomi (30'' x 10'')", "Uchikomi (30'' x 10'')");
        agregarSiNoExiste(lista, clavesExistentes, "tatami.btn.ver_combate", "Ver Combate", "Watch Match", "Ver Combate");

        agregarSiNoExiste(lista, clavesExistentes, "auth.codigo_verificacion", "Código de Verificación", "Verification Code", "Código de Verificação");
        agregarSiNoExiste(lista, clavesExistentes, "auth.codigo_enviado", "Código enviado a", "Code sent to", "Código enviado para");
        agregarSiNoExiste(lista, clavesExistentes, "auth.codigo_incorrecto", "Código incorrecto", "Incorrect code", "Código incorreto");
        agregarSiNoExiste(lista, clavesExistentes, "auth.verificar_email", "Verificar Email", "Verify Email", "Verificar E-mail");
        agregarSiNoExiste(lista, clavesExistentes, "auth.verificar_finalizar", "Verificar y Finalizar", "Verify and Finish", "Verificar e Finalizar");

        agregarSiNoExiste(lista, clavesExistentes, "master.nuevas_suscripciones", "Nuevas Suscripciones SaaS (Otros Dojos)", "New SaaS Subscriptions (Other Dojos)", "Novas Assinaturas SaaS (Outros Dojos)");

        // Nota: algunas claves como "estado.aprobado.master.sin.soporte" no se incluyeron porque no estaban en el CSV, pero si las necesitas, agrégalas aquí.
        agregarSiNoExiste(lista, clavesExistentes, "mecenas.btn.ver_perfil", "Ver Perfil", "View Profile", "Ver Perfil");
        agregarSiNoExiste(lista, clavesExistentes, "sensei.btn.ver_perfil", "Ver Perfil", "View Profile", "Ver Perfil");
        agregarSiNoExiste(lista, clavesExistentes, "grid.judoka.nombre", "Nombre", "Name", "Nome");
        agregarSiNoExiste(lista, clavesExistentes, "grid.judoka.apellido", "Apellido", "Last Name", "Sobrenome");
        agregarSiNoExiste(lista, clavesExistentes, "grid.judoka.grado", "Grado", "Rank", "Graduação");
        agregarSiNoExiste(lista, clavesExistentes, "grid.judoka.accion", "Acción", "Action", "Ação");

        agregarSiNoExiste(lista, clavesExistentes, "view.sensei.microciclo.titulo", "Microciclo", "Microcycle", "Microciclo");
        agregarSiNoExiste(lista, clavesExistentes, "view.sensei.macrociclo", "Macrociclo", "Macrocycle", "Macrociclo");

        agregarSiNoExiste(lista, clavesExistentes, "agenda.titulo", "Agenda", "Agenda", "Agenda");
        agregarSiNoExiste(lista, clavesExistentes, "finanzas.titulo", "Finanzas", "Finance", "Finanças");
        agregarSiNoExiste(lista, clavesExistentes, "admin.titulo", "Configuración del SaaS", "SaaS Configuration", "Admin");
        agregarSiNoExiste(lista, clavesExistentes, "admin.descripcion", "Parametrice su escenario", "Parameterize your setting", "Parametrize seu cenário");
        agregarSiNoExiste(lista, clavesExistentes, "menu.backup", "Respaldo BD", "Database Backup", "Backup de BD");
        agregarSiNoExiste(lista, clavesExistentes, "menu.senseis", "Senses", "Senseis", "Senses");
        // =========================================================
        // 2. BIBLIOTECA DE SABIDURÍA (CITAS DE MAESTROS)
        // =========================================================
        // --- SUN TZU ---
        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.1",
                "Los competidores victoriosos ganan primero en su tatami y luego van a la competencia; los derrotados van a la competencia primero y luego buscan cómo ganar.",
                "Victorious competitors win first on their tatami and then go to competition; the defeated go to competition first and then seek how to win.",
                "Competidores vitoriosos vencem primeiro em seu tatame e depois vão à competição; os derrotados vão à competição primeiro e depois procuram como vencer.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.2",
                "La excelencia suprema consiste en romper el equilibrio del oponente sin usar la fuerza bruta.",
                "Supreme excellence consists in breaking the opponent's balance without using brute force.",
                "A excelência suprema consiste em quebrar o equilíbrio do oponente sem usar a força bruta.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.3",
                "En medio del caos del combate, siempre puedes marcar Ippon.",
                "In the midst of combat chaos, you can always score Ippon.",
                "No meio do caos do combate, você sempre pode marcar Ippon.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.4",
                "Conoce a tu oponente y conócete a ti mismo; en cien combates, nunca perderás.",
                "Know your opponent and know yourself; in a hundred battles, you will never lose.",
                "Conheça seu oponente e conheça a si mesmo; em cem combates, você nunca perderá.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.5",
                "La invencibilidad reside en la defensa; la posibilidad de ganar, en el ataque.",
                "Invincibility lies in the defense; the possibility of victory, in the attack.",
                "A invencibilidade reside na defesa; a possibilidade de vitória, no ataque.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.6",
                "El agua determina su curso según el suelo; el Judoka consigue la victoria adaptándose a su oponente.",
                "Water determines its course according to the ground; the Judoka achieves victory by adapting to their opponent.",
                "A água determina seu curso de acordo com o solo; o Judoca alcança a vitória adaptando-se ao seu oponente.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.7",
                "Aparenta debilidad cuando seas fuerte, y fuerza cuando estés cansado.",
                "Appear weak when you are strong, and strong when you are tired.",
                "Aparente fraqueza quando você for forte, e força quando estiver cansado.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.8",
                "La rapidez es la esencia del Judo.",
                "Speed is the essence of Judo.",
                "A rapidez é a essência do Judô.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.suntzu.9",
                "La invencibilidad depende de mí, la derrota de mi oponente.",
                "Invincibility depends on me, the opponent's defeat depends on them.",
                "A invencibilidade depende de mim, a derrota do oponente depende dele.");

        // --- MIYAMOTO MUSASHI ---
        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.1",
                "No hagas ningún movimiento en el tatami que no sea de utilidad.",
                "Do not make any movement on the tatami that is not useful.",
                "Não faça nenhum movimento no tatame que não seja útil.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.2",
                "Percibe la intención de tu oponente antes de que se mueva.",
                "Perceive your opponent's intention before they move.",
                "Perceba a intenção do seu oponente antes que ele se mova.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.3",
                "Hoy es la victoria sobre tu yo de ayer; mañana será tu victoria en el campeonato.",
                "Today is victory over your self of yesterday; tomorrow is your victory in the championship.",
                "Hoje é a vitória sobre o seu eu de ontem; amanhã será a sua vitória no campeonato.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.4",
                "Debes entender que hay más de un camino para lograr el Ippon.",
                "You must understand that there is more than one way to achieve Ippon.",
                "Você deve entender que há mais de um caminho para alcançar o Ippon.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.5",
                "En el combate, mira lo distante como si estuviera cerca y lo cercano con perspectiva.",
                "In combat, look at distant things as if they were close and close things with perspective.",
                "No combate, olhe o distante como se estivesse perto e o próximo com perspectiva.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.6",
                "El ritmo existe en todo. Si no entiendes el ritmo del combate, serás proyectado.",
                "Rhythm exists in everything. If you don't understand the combat rhythm, you will be thrown.",
                "O ritmo existe em tudo. Se você não entender o ritmo do combate, será projetado.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.7",
                "Si conoces la Vía ampliamente, la verás en cada interacción.",
                "If you know the Way broadly, you will see it in every interaction.",
                "Se você conhece o Caminho amplamente, verá o em cada interação.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.musashi.8",
                "La verdadera técnica significa practicar de tal forma que sea útil aún en la calle.",
                "True technique means practicing in such a way that it is useful even on the street.",
                "A verdadeira técnica significa praticar de tal forma que seja útil até na rua.");

        // --- JIGORO KANO ---
        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.1",
                "El Judo no es solo deporte, es el principio básico de la conducta humana.",
                "Judo is not just a sport, it is the basic principle of human conduct.",
                "O Judô não é apenas esporte, é o princípio básico da conduta humana.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.2",
                "Camina por un solo camino. No te vuelvas engreído por el Oro, ni roto por la derrota.",
                "Walk a single path. Do not become conceited by Gold, nor broken by defeat.",
                "Ande por um único caminho. Não se torne convencido pelo Ouro, nem quebrado pela derrota.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.3",
                "Lo importante no es ser mejor que otros competidores, sino ser mejor que ayer.",
                "The important thing is not to be better than other competitors, but to be better than yesterday.",
                "O importante não é ser melhor que outros competidores, mas ser melhor que ontem.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.4",
                "Máxima eficiencia con el mínimo esfuerzo.",
                "Maximum efficiency with minimum effort.",
                "Máxima eficiência com mínimo esforço.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.5",
                "Prosperidad y beneficio mutuo dentro y fuera del tatami.",
                "Mutual welfare and benefit inside and outside the tatami.",
                "Prosperidade e benefício mútuo dentro e fora do tatame.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.6",
                "Ser proyectado es temporal; rendirse es lo que lo hace permanente.",
                "Being thrown is temporary; giving up is what makes it permanent.",
                "Ser projetado é temporário; desistir é o que o torna permanente.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.7",
                "Antes y después del Randori, inclínate ante tu compañero.",
                "Before and after Randori, bow to your partner.",
                "Antes e depois do Randori, curve-se diante do seu parceiro.");

        agregarSiNoExiste(lista, clavesExistentes, "sabiduria.kano.8",
                "La delicadeza controla la fuerza. Cede para vencer.",
                "Gentleness controls strength. Yield to win.",
                "A delicadeza controla a força. Ceda para vencer.");

        // =========================================================
        // 3. PERFIL (REQUERIDO POR EL MÉTODO DADO)
        // =========================================================
        agregarSiNoExiste(lista, clavesExistentes, "perfil.titulo", "Mi Santuario", "My Sanctuary", "Meu Santuário");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.notas.titulo", "Bitácora de Reflexión", "Reflection Journal", "Diário de Reflexão");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.notas.placeholder",
                "Escribe aquí tus pensamientos, metas o correcciones...",
                "Write your thoughts, goals, or corrections here...",
                "Escreva aqui seus pensamentos, metas ou correções...");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.btn.guardar", "Guardar Reflexión", "Save Reflection", "Salvar Reflexão");
        agregarSiNoExiste(lista, clavesExistentes, "perfil.msg.guardado",
                "Reflexión guardada en tu mente.",
                "Reflection saved in your mind.",
                "Reflexão salva em sua mente.");

        agregarSiNoExiste(lista, clavesExistentes, "vista.registro.titulo",
                "Completa tu registro",
                "Complete your registration",
                "Complete seu registro");
        agregarSiNoExiste(lista, clavesExistentes, "vista.registro.descripcion",
                "Completa el formulario. ¡Enhorabuena!","Complete form. Welcome !",
                "Complete o formulário. Bem-vindo !");
        agregarSiNoExiste(lista, clavesExistentes, "label.contrasena",
                "Contraseña",
                "Password","Contraseña");
        agregarSiNoExiste(lista, clavesExistentes, "label.confirmar_contrasena","Confirmar Contraseña","Confirm password","Confirmar Contraseña");
        agregarSiNoExiste(lista, clavesExistentes, "boton.activar_cuenta","Activar tu cuenta","Activate","Activar tu cuenta");
        agregarSiNoExiste(lista, clavesExistentes, "error.campos_incompletos","Error al registrar. Por favor completa todos los campos.",
                "Error please fill all the blanks",
                "Error por favor complete todos os campos");
        agregarSiNoExiste(lista, clavesExistentes, "error.activacion","Error al activar. Por favor intenta de nuevo.","Error please try again",
                "Erro por favor tente novamente");
        agregarSiNoExiste(lista, clavesExistentes, "error.titulo_ops","ups! algo falló ... trata de nuevo por fa...",
                "Ops please try again",
                "Ups algo falhou... por favor tente novamente");
        if (!lista.isEmpty()) {
            traduccionRepo.saveAll(lista);
            System.out.println(">>> TRADUCCIONES INICIALIZADAS: " + lista.size() + " registros nuevos.");
        } else {
            System.out.println(">>> No hay nuevas traducciones que agregar.");
        }
    }

    /**
     * Agrega las tres versiones de una traducción solo si la clave no existe previamente.
     */
    private void agregarSiNoExiste(List<Traduccion> nuevas, Set<String> clavesExistentes,
                                   String clave, String es, String en, String pt) {
        if (!clavesExistentes.contains(clave)) {
            nuevas.add(new Traduccion(clave, "es", es));
            nuevas.add(new Traduccion(clave, "en", en));
            nuevas.add(new Traduccion(clave, "pt", pt));
            clavesExistentes.add(clave); // Evita duplicados en la misma iteración
        }
    }
}