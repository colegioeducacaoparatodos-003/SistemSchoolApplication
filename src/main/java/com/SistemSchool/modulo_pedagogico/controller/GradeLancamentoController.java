package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.EvaluationRepository;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class GradeLancamentoController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GradeLancamentoController.class.getName());

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════
    private Long selectedSchoolClassId;
    private String selectedEducationLevel;
    private Long selectedDisciplineId;
    private Long selectedScheduleId;
    private Trimester selectedTrimester;
    private EvaluationType selectedEvaluationType;
    private String evaluationTitle;

    // ═══════════════════════════════════════════════════════════════
    // LISTAS
    // ═══════════════════════════════════════════════════════════════
    private List<SchoolClass> schoolClasses = new ArrayList<>();
    private List<String> educationLevels = List.of("PRIMARIO", "I_CICLO");
    private List<Discipline> disciplines = new ArrayList<>();
    private List<Schedule> schedules = new ArrayList<>();
    private final Trimester[] trimesters = Trimester.values();
    private final EvaluationType[] evaluationTypes = EvaluationType.values();

    // ═══════════════════════════════════════════════════════════════
    // ESTADO
    // ═══════════════════════════════════════════════════════════════
    private boolean showTable = false;
    private boolean saving = false;
    private boolean creatingEvaluation = true;
    private List<StudentRow> studentRows = new ArrayList<>();
    private StudentRow selectedStudentRow;
    private int maxNota = 20;
    private String escalaLabel = "0–20";

    // Evaluation ativa na sessão de lançamento
    private Evaluation currentEvaluation;

    // ═══════════════════════════════════════════════════════════════
    // SERVIÇOS
    // ═══════════════════════════════════════════════════════════════
    @Inject
    private SchoolClassRepository schoolClassRepository;
    @Inject
    private DisciplineService disciplineService;
    @Inject
    private EnrolmentRepository enrolmentRepository;
    @Inject
    private GradeRepository gradeRepository;
    @Inject
    private EvaluationRepository evaluationRepository;
    @Inject
    private ScheduleRepository scheduleRepository;
    @Inject
    private TrimesterResultService trimesterResultService;

    // ═══════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════

    @PostConstruct
    public void init() {
        loadSchoolClasses();
        loadDisciplines();
        loadSchedules();
        selectedTrimester = Trimester.PRIMEIRO;
        selectedEvaluationType = EvaluationType.CONTINUA;
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    public void loadStudents() {
        if (selectedSchoolClassId == null || selectedDisciplineId == null
                || selectedTrimester == null || selectedEvaluationType == null
                || evaluationTitle == null || evaluationTitle.isBlank()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Campos obrigatórios",
                    "Selecione a turma, disciplina, trimestre, tipo de avaliação e informe o título.");
            return;
        }

        try {
            configureScale();

            // 1. Buscar ou criar Evaluation
            currentEvaluation = findOrCreateEvaluation();
            creatingEvaluation = (currentEvaluation.getPkEvaluation() == null);

            // 2. Buscar matrículas ativas da turma
            // IMPORTANTE: usar o método com JOIN FETCH (student + schoolClass).
            // O método findBySchoolClass_PkSchoolClass (derivado, sem fetch) devolve
            // Enrolment com Student como proxy lazy; como este controller injeta o
            // repositório diretamente (sem um service @Transactional por trás), a
            // sessão do Hibernate já está fechada quando o código abaixo tenta
            // acessar en.getStudent().getSudentNumber() no sort, causando
            // LazyInitializationException: no Session.
            List<Enrolment> enrolments = enrolmentRepository
                    .findBySchoolClass_PkSchoolClassWithStudent(selectedSchoolClassId);

            if (enrolments == null || enrolments.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                        "Nenhum aluno encontrado para a turma selecionada.");
                showTable = false;
                return;
            }

            enrolments.sort(Comparator.comparing(
                    e -> e.getStudent() != null ? e.getStudent().getSudentNumber() : "",
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

            // 3. Montar rows
            studentRows = new ArrayList<>();
            int seq = 1;

            for (Enrolment en : enrolments) {
                StudentRow row = new StudentRow();
                row.setNumero(seq++);
                row.setEnrolmentPk(en.getPhEnrolment());
                row.setStudentName(en.getStudent() != null ? en.getStudent().getFullName() : "—");
                row.setEnrolmentNumber(en.getStudent() != null ? en.getStudent().getSudentNumber() : "—");

                // Buscar Grade existente para este aluno + evaluation
                Optional<Grade> existing = gradeRepository
                        .findByEnrolmentPkAndEvaluationPk(en.getPhEnrolment(), currentEvaluation.getPkEvaluation());

                if (existing.isPresent()) {
                    Grade g = existing.get();
                    row.setNota(g.getScore());
                    row.setObservation(g.getObs());
                    row.setGradePk(g.getPkGrade());
                } else {
                    row.setNota(null);
                    row.setObservation(null);
                    row.setGradePk(null);
                }

                studentRows.add(row);
            }

            showTable = true;
            addMessage(FacesMessage.SEVERITY_INFO, "Alunos carregados",
                    studentRows.size() + " aluno(s) encontrado(s).");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            showTable = false;
        }
    }

    public void clear() {
        selectedSchoolClassId = null;
        selectedEducationLevel = null;
        selectedDisciplineId = null;
        selectedScheduleId = null;
        selectedTrimester = Trimester.PRIMEIRO;
        selectedEvaluationType = EvaluationType.CONTINUA;
        evaluationTitle = null;
        studentRows = new ArrayList<>();
        showTable = false;
        creatingEvaluation = true;
        currentEvaluation = null;
        maxNota = 20;
        escalaLabel = "0–20";
    }

    // ═══════════════════════════════════════════════════════════════
    // SALVAMENTO
    // ═══════════════════════════════════════════════════════════════

    public void saveSingleStudent() {
        if (selectedStudentRow == null || selectedStudentRow.getEnrolmentPk() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nenhum aluno selecionado.");
            return;
        }

        Double nota = selectedStudentRow.getNota();
        if (nota == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Informe uma nota para salvar.");
            return;
        }

        if (nota < 0 || nota > maxNota) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    "A nota deve estar entre 0 e " + maxNota + ".");
            return;
        }

        try {
            saving = true;
            persistGrade(selectedStudentRow);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Nota lançada com sucesso!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar nota individual", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar nota", e.getMessage());
        } finally {
            saving = false;
        }
    }

    public void saveDraft() {
        saveGrades(false);
    }

    public void saveAll() {
        saveGrades(true);
    }

    private void saveGrades(boolean requireAll) {
        if (studentRows == null || studentRows.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum aluno carregado.");
            return;
        }

        long filled = studentRows.stream().filter(r -> r.getNota() != null).count();
        if (filled == 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma nota preenchida.");
            return;
        }

        if (requireAll) {
            long empty = studentRows.stream().filter(r -> r.getNota() == null).count();
            if (empty > 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso",
                        "Existem " + empty + " aluno(s) sem nota. Preencha todas antes de salvar.");
                return;
            }
        }

        try {
            saving = true;
            int count = 0;

            for (StudentRow row : studentRows) {
                if (row.getNota() != null) {
                    persistGrade(row);
                    count++;
                }
            }

            creatingEvaluation = false;
            String title = requireAll ? "Notas Salvas" : "Rascunho Salvo";
            String detail = count + " nota(s) " + (requireAll ? "salva(s)" : "guardada(s) como rascunho") + ".";
            addMessage(FacesMessage.SEVERITY_INFO, title, detail);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar notas", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", e.getMessage());
        } finally {
            saving = false;
        }
    }

    private void persistGrade(StudentRow row) {
        if (currentEvaluation == null || currentEvaluation.getPkEvaluation() == null) {
            currentEvaluation = findOrCreateEvaluation();
        }

        Grade grade;
        if (row.getGradePk() != null) {
            grade = gradeRepository.findById(row.getGradePk())
                    .orElse(new Grade());
        } else {
            grade = new Grade();
        }

        Enrolment enrolment = enrolmentRepository.findById(row.getEnrolmentPk())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada: " + row.getEnrolmentPk()));

        grade.setEvaluation(currentEvaluation);
        grade.setEnrolment(enrolment);
        grade.setScore(row.getNota());
        grade.setObs(row.getObservation());

        // O JPA dispara @PrePersist / @PreUpdate automaticamente via repository.save().
        // NÃO chamar onCreate() / onUpdate() explicitamente para evitar erro de
        // visibilidade (protected em outro pacote).
        Grade saved = gradeRepository.save(grade);
        row.setGradePk(saved.getPkGrade());

        // Recalcular resultado trimestral
        trimesterResultService.calcularTrimesterResult(
                row.getEnrolmentPk(), selectedDisciplineId, selectedTrimester);
    }

    // ═══════════════════════════════════════════════════════════════
    // EVALUATION
    // ═══════════════════════════════════════════════════════════════

    private Evaluation findOrCreateEvaluation() {
        Discipline discipline = disciplines.stream()
                .filter(d -> d.getPkDiscipline().equals(selectedDisciplineId))
                .findFirst().orElse(null);

        // A entidade Evaluation NÃO possui os campos schoolClass, schedule nem title.
        // A busca e criação usam apenas os campos existentes: discipline, trimester,
        // evaluationType e evaluationName.
        Optional<Evaluation> existing = evaluationRepository
                .findByDisciplineAndTrimesterAndEvaluationTypeAndEvaluationName(
                        discipline, selectedTrimester, selectedEvaluationType, evaluationTitle);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Cria nova Evaluation com os campos disponíveis na entidade
        Evaluation ev = new Evaluation();
        ev.setDiscipline(discipline);
        ev.setTrimester(selectedTrimester);
        ev.setEvaluationType(selectedEvaluationType);
        ev.setEvaluationName(evaluationTitle);

        // O JPA executa @PrePersist automaticamente no save() — não chamar onCreate()
        // explicitamente para evitar erro de visibilidade (protected em outro pacote).
        return evaluationRepository.save(ev);
    }

    // ═══════════════════════════════════════════════════════════════
    // MODAL
    // ═══════════════════════════════════════════════════════════════

    public void prepareLaunch(StudentRow row) {
        this.selectedStudentRow = row;
    }

    // ═══════════════════════════════════════════════════════════════
    // AUXILIARES
    // ═══════════════════════════════════════════════════════════════

    private void configureScale() {
        if ("PRIMARIO".equals(selectedEducationLevel)) {
            maxNota = 10;
            escalaLabel = "0–10 (Primário)";
        } else {
            maxNota = 20;
            escalaLabel = "0–20 (I Ciclo)";
        }
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassRepository.findAll();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
        }
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllActive();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
        }
    }

    private void loadSchedules() {
        try {
            schedules = scheduleRepository.findAll();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar horários", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar horários", e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // DTO INTERNO — LINHA DE ALUNO
    // ═══════════════════════════════════════════════════════════════

    public static class StudentRow implements Serializable {
        private static final long serialVersionUID = 1L;

        private int numero;
        private String studentName;
        private String enrolmentNumber;
        private Double nota;
        private String observation;
        private Long enrolmentPk;
        private Long gradePk;

        public int getNumero() { return numero; }
        public void setNumero(int numero) { this.numero = numero; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getEnrolmentNumber() { return enrolmentNumber; }
        public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

        public Double getNota() { return nota; }
        public void setNota(Double nota) { this.nota = nota; }

        public String getObservation() { return observation; }
        public void setObservation(String observation) { this.observation = observation; }

        public Long getEnrolmentPk() { return enrolmentPk; }
        public void setEnrolmentPk(Long enrolmentPk) { this.enrolmentPk = enrolmentPk; }

        public Long getGradePk() { return gradePk; }
        public void setGradePk(Long gradePk) { this.gradePk = gradePk; }
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public Long getSelectedSchoolClassId() { return selectedSchoolClassId; }
    public void setSelectedSchoolClassId(Long selectedSchoolClassId) { this.selectedSchoolClassId = selectedSchoolClassId; }

    public String getSelectedEducationLevel() { return selectedEducationLevel; }
    public void setSelectedEducationLevel(String selectedEducationLevel) { this.selectedEducationLevel = selectedEducationLevel; }

    public Long getSelectedDisciplineId() { return selectedDisciplineId; }
    public void setSelectedDisciplineId(Long selectedDisciplineId) { this.selectedDisciplineId = selectedDisciplineId; }

    public Long getSelectedScheduleId() { return selectedScheduleId; }
    public void setSelectedScheduleId(Long selectedScheduleId) { this.selectedScheduleId = selectedScheduleId; }

    public Trimester getSelectedTrimester() { return selectedTrimester; }
    public void setSelectedTrimester(Trimester selectedTrimester) { this.selectedTrimester = selectedTrimester; }

    public EvaluationType getSelectedEvaluationType() { return selectedEvaluationType; }
    public void setSelectedEvaluationType(EvaluationType selectedEvaluationType) { this.selectedEvaluationType = selectedEvaluationType; }

    public String getEvaluationTitle() { return evaluationTitle; }
    public void setEvaluationTitle(String evaluationTitle) { this.evaluationTitle = evaluationTitle; }

    public List<SchoolClass> getSchoolClasses() { return schoolClasses; }
    public List<String> getEducationLevels() { return educationLevels; }
    public List<Discipline> getDisciplines() { return disciplines; }
    public List<Schedule> getSchedules() { return schedules; }
    public Trimester[] getTrimesters() { return trimesters; }
    public EvaluationType[] getEvaluationTypes() { return evaluationTypes; }

    public boolean isShowTable() { return showTable; }
    public boolean isSaving() { return saving; }
    public boolean isCreatingEvaluation() { return creatingEvaluation; }

    public List<StudentRow> getStudentRows() { return studentRows; }
    public void setStudentRows(List<StudentRow> studentRows) { this.studentRows = studentRows; }

    public StudentRow getSelectedStudentRow() { return selectedStudentRow; }
    public void setSelectedStudentRow(StudentRow selectedStudentRow) { this.selectedStudentRow = selectedStudentRow; }

    public int getMaxNota() { return maxNota; }
    public String getEscalaLabel() { return escalaLabel; }

    public String getEducationLevel() { return selectedEducationLevel; }
}