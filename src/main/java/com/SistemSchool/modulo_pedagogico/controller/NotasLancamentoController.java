package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.EvaluationService;
import com.SistemSchool.modulo_pedagogico.service.GradeService;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller para lançamento de notas em grid.
 * O utilizador seleciona: Disciplina + Turma + Trimestre
 * O sistema lista todos os alunos com campos MAC, NPP, NPT.
 * Lança todas de uma vez — muito mais simples que lançar uma a uma.
 */
@Named
@ViewScoped
public class NotasLancamentoController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(NotasLancamentoController.class.getName());

    // Filtros
    private Long selectedDisciplineId;
    private Long selectedSchoolClassId;
    private Integer selectedTrimester = 1;

    // Grid de lançamento
    private List<NotaRow> rows = new ArrayList<>();

    // Dropdowns
    private List<DisciplineDTO> disciplines = new ArrayList<>();
    private List<EnrolmentDTO> enrolments = new ArrayList<>();
    private Integer[] trimesters = {1, 2, 3};

    @Inject
    private GradeService gradeService;

    @Inject
    private EvaluationService evaluationService;

    @Inject
    private DisciplineService disciplineService;

    @Inject
    private EnrolmentService enrolmentService;

    @Inject
    private StudentService studentService;

    @PostConstruct
    public void init() {
        loadDisciplines();
        loadEnrolments();
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllDisciplines();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
        }
    }

    private void loadEnrolments() {
        try {
            enrolments = enrolmentService.getAllEnrolments();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar matrículas", e);
        }
    }

    /**
     * Carrega o grid com os alunos da turma selecionada.
     * Se já existirem notas lançadas, preenche os campos.
     */
    public void loadGrid() {
        rows.clear();
        if (selectedDisciplineId == null || selectedSchoolClassId == null || selectedTrimester == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Filtros incompletos",
                    "Selecione disciplina, turma e trimestre.");
            return;
        }

        try {
            // Buscar avaliações desta disciplina + trimestre
            List<Evaluation> evaluations = evaluationService.getByDiscipline(selectedDisciplineId)
                    .stream()
                    .filter(e -> selectedTrimester.equals(e.getTrimester()))
                    .collect(Collectors.toList());

            // Mapear por tipo
            Map<EvaluationType, Evaluation> evalByType = evaluations.stream()
                    .collect(Collectors.toMap(Evaluation::getType, e -> e, (a, b) -> a));

            // Buscar alunos da turma
            List<EnrolmentDTO> classEnrolments = enrolments.stream()
                    .filter(e -> e.getSchoolClassPk() != null && e.getSchoolClassPk().equals(selectedSchoolClassId))
                    .collect(Collectors.toList());

            for (EnrolmentDTO enr : classEnrolments) {
                NotaRow row = new NotaRow();
                row.setStudentPk(enr.getStudentPk());
                row.setStudentFullName(enr.getStudentFullName());
                row.setEnrolmentPk(enr.getPhEnrolment());

                // Se já existirem notas, preencher
                for (Evaluation ev : evaluations) {
                    // Buscar grade existente
                    gradeService.getByEvaluation(ev.getPkEvaluation()).stream()
                            .filter(g -> g.getStudent().getPkStudent().equals(enr.getStudentPk()))
                            .findFirst()
                            .ifPresent(g -> {
                                if (ev.getType() == EvaluationType.CONTINUOUS_ASSESSMENT) row.setMac(g.getValue());
                                if (ev.getType() == EvaluationType.TEACHER_TEST) row.setNpp(g.getValue());
                                if (ev.getType() == EvaluationType.FINAL_TEST) row.setNpt(g.getValue());
                                row.setObservation(g.getObservation());
                            });
                }

                row.setMacEval(evalByType.get(EvaluationType.CONTINUOUS_ASSESSMENT));
                row.setNppEval(evalByType.get(EvaluationType.TEACHER_TEST));
                row.setNptEval(evalByType.get(EvaluationType.FINAL_TEST));

                rows.add(row);
            }

            rows.sort(Comparator.comparing(NotaRow::getStudentFullName));

            if (rows.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Sem alunos",
                        "Nenhum aluno encontrado para a turma selecionada.");
            } else {
                addMessage(FacesMessage.SEVERITY_INFO, "Grid carregado",
                        rows.size() + " aluno(s) pronto(s) para lançamento.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar grid", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar grid", e.getMessage());
        }
    }

    /**
     * Grava todas as notas do grid de uma só vez.
     */
    public void saveAll() {
        int saved = 0;
        try {
            for (NotaRow row : rows) {
                if (row.getMacEval() != null && row.getMac() != null) {
                    saveOrUpdateGrade(row.getMacEval(), row.getStudentPk(), row.getEnrolmentPk(), row.getMac(), row.getObservation());
                    saved++;
                }
                if (row.getNppEval() != null && row.getNpp() != null) {
                    saveOrUpdateGrade(row.getNppEval(), row.getStudentPk(), row.getEnrolmentPk(), row.getNpp(), row.getObservation());
                    saved++;
                }
                if (row.getNptEval() != null && row.getNpt() != null) {
                    saveOrUpdateGrade(row.getNptEval(), row.getStudentPk(), row.getEnrolmentPk(), row.getNpt(), row.getObservation());
                    saved++;
                }
            }
            addMessage(FacesMessage.SEVERITY_INFO, "Notas gravadas",
                    saved + " nota(s) gravada(s) com sucesso.");
            loadGrid(); // recarrega para mostrar actualizações
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar notas", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gravar", e.getMessage());
        }
    }

    private void saveOrUpdateGrade(Evaluation evaluation, Long studentPk, Long enrolmentPk, Double value, String obs) {
        // Verificar se já existe
        List<Grade> existing = gradeService.getByEvaluation(evaluation.getPkEvaluation());
        Optional<Grade> match = existing.stream()
                .filter(g -> g.getStudent().getPkStudent().equals(studentPk))
                .findFirst();

        if (match.isPresent()) {
            Grade g = match.get();
            g.setValue(value);
            g.setObservation(obs);
            gradeService.save(g); // update implícito
        } else {
            Grade g = new Grade();
            g.setEvaluation(evaluation);
            Student s = new Student(); s.setPkStudent(studentPk);
            g.setStudent(s);
            Enrolment e = new Enrolment(); e.setPhEnrolment(enrolmentPk);
            g.setEnrolment(e);
            g.setValue(value);
            g.setObservation(obs);
            gradeService.save(g);
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ─────────────────────────────────────────────────────────────
    // Classe interna para o grid
    // ─────────────────────────────────────────────────────────────
    public static class NotaRow implements Serializable {
        private Long studentPk;
        private String studentFullName;
        private Long enrolmentPk;
        private Double mac;
        private Double npp;
        private Double npt;
        private String observation;
        private transient Evaluation macEval;
        private transient Evaluation nppEval;
        private transient Evaluation nptEval;

        // Getters / Setters
        public Long getStudentPk() { return studentPk; }
        public void setStudentPk(Long studentPk) { this.studentPk = studentPk; }
        public String getStudentFullName() { return studentFullName; }
        public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }
        public Long getEnrolmentPk() { return enrolmentPk; }
        public void setEnrolmentPk(Long enrolmentPk) { this.enrolmentPk = enrolmentPk; }
        public Double getMac() { return mac; }
        public void setMac(Double mac) { this.mac = mac; }
        public Double getNpp() { return npp; }
        public void setNpp(Double npp) { this.npp = npp; }
        public Double getNpt() { return npt; }
        public void setNpt(Double npt) { this.npt = npt; }
        public String getObservation() { return observation; }
        public void setObservation(String observation) { this.observation = observation; }
        public Evaluation getMacEval() { return macEval; }
        public void setMacEval(Evaluation macEval) { this.macEval = macEval; }
        public Evaluation getNppEval() { return nppEval; }
        public void setNppEval(Evaluation nppEval) { this.nppEval = nppEval; }
        public Evaluation getNptEval() { return nptEval; }
        public void setNptEval(Evaluation nptEval) { this.nptEval = nptEval; }
    }

    // Getters / Setters do Controller
    public Long getSelectedDisciplineId() { return selectedDisciplineId; }
    public void setSelectedDisciplineId(Long selectedDisciplineId) { this.selectedDisciplineId = selectedDisciplineId; }
    public Long getSelectedSchoolClassId() { return selectedSchoolClassId; }
    public void setSelectedSchoolClassId(Long selectedSchoolClassId) { this.selectedSchoolClassId = selectedSchoolClassId; }
    public Integer getSelectedTrimester() { return selectedTrimester; }
    public void setSelectedTrimester(Integer selectedTrimester) { this.selectedTrimester = selectedTrimester; }
    public List<NotaRow> getRows() { return rows; }
    public void setRows(List<NotaRow> rows) { this.rows = rows; }
    public List<DisciplineDTO> getDisciplines() { return disciplines; }
    public List<EnrolmentDTO> getEnrolments() { return enrolments; }
    public Integer[] getTrimesters() { return trimesters; }
}
