package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.lazy.GradeLazyModel;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.service.EvaluationService;
import com.SistemSchool.modulo_pedagogico.service.GradeService;
import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class GradeController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GradeController.class.getName());

    // ── MODELOS ──
    private Grade grade = new Grade();
    private GradeDTO editDto = new GradeDTO();
    private GradeDTO selectedGrade = new GradeDTO();
    private Long selectedId;

    // CORRIGIDO: Long com converter="jakarta.faces.Long" no XHTML
    private Long selectedEvaluationId;
    private Long selectedEnrolmentId;

    // ── FILTROS ──
    private Long filterEvaluationId;
    private Long filterEnrolmentId;
    private String filterStudentName;
    private String filterDisciplineName;

    // ── LISTAS ──
    // CORRIGIDO: usar EvaluationDTO em vez de Evaluation para evitar LazyInitializationException
    private List<EvaluationDTO> evaluations = new ArrayList<>();
    private List<EnrolmentDTO> enrolments = new ArrayList<>();

    // ── ESTATÍSTICAS ──
    private long totalGradeCount;
    private double averageScore;

    // ── SERVIÇOS ──
    @Inject
    private GradeService gradeService;
    @Inject
    private EvaluationService evaluationService;
    @Inject
    private EnrolmentService enrolmentService;
    private transient GradeLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new GradeLazyModel(gradeService);
        loadEvaluations();
        loadEnrolments();
        computeStatistics();
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar notas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de notas", e);
        }
        return "/management/pedagogico/grades.xhtml?faces-redirect=true";
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterEvaluationId = null;
        filterEnrolmentId = null;
        filterStudentName = null;
        filterDisciplineName = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    public List<GradeDTO> getFilteredGrades() {
        List<GradeDTO> all = gradeService.getAllGrades();

        return all.stream()
                .filter(g -> filterEvaluationId == null ||
                        (g.getEvaluationPk() != null && g.getEvaluationPk().equals(filterEvaluationId)))
                .filter(g -> filterEnrolmentId == null ||
                        (g.getEnrolmentPk() != null && g.getEnrolmentPk().equals(filterEnrolmentId)))
                .filter(g -> filterStudentName == null || filterStudentName.isBlank() ||
                        (g.getStudentFullName() != null && g.getStudentFullName().toLowerCase().contains(filterStudentName.toLowerCase())))
                .filter(g -> filterDisciplineName == null || filterDisciplineName.isBlank() ||
                        (g.getDisciplineName() != null && g.getDisciplineName().toLowerCase().contains(filterDisciplineName.toLowerCase())))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    private void loadEvaluations() {
        try {
            evaluations = evaluationService.getAllEvaluations();
            if (evaluations == null) {
                evaluations = new ArrayList<>();
            }
        } catch (Exception e) {
            evaluations = new ArrayList<>();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar avaliações", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar avaliações", e);
        }
    }

    private void loadEnrolments() {
        try {
            enrolments = enrolmentService.getAllEnrolments();
            if (enrolments == null) {
                enrolments = new ArrayList<>();
            }
        } catch (Exception e) {
            enrolments = new ArrayList<>();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matrículas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar matrículas", e);
        }
    }

    private void computeStatistics() {
        try {
            List<GradeDTO> all = getFilteredGrades();
            totalGradeCount = all.size();
            averageScore = all.stream().mapToDouble(GradeDTO::getScore).average().orElse(0.0);
        } catch (Exception e) {
            totalGradeCount = 0;
            averageScore = 0.0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    public void prepareNewGrade() {
        grade = new Grade();
        selectedEvaluationId = null;
        selectedEnrolmentId = null;
        loadEvaluations();
        loadEnrolments();
    }

    public void saveGrade() {
        try {
            if (selectedEvaluationId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A avaliação é obrigatória.");
                return;
            }
            if (selectedEnrolmentId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A matrícula / aluno é obrigatória.");
                return;
            }
            if (grade.getScore() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A nota é obrigatória.");
                return;
            }

            Evaluation evaluation = evaluationService.getById(selectedEvaluationId);
            grade.setEvaluation(evaluation);

            var enrolment = enrolmentService.getById(selectedEnrolmentId);
            grade.setEnrolment(enrolment);

            gradeService.save(grade);

            grade = new Grade();
            selectedEvaluationId = null;
            selectedEnrolmentId = null;

            init();

            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Nota lançada com sucesso");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao lançar nota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma nota selecionada");
            return;
        }
        this.selectedId = id;
        loadEvaluations();
        loadEnrolments();

        GradeDTO dto = gradeService.getAllGrades().stream()
                .filter(g -> id.equals(g.getPkGrade())).findFirst().orElse(null);
        if (dto != null) {
            editDto = new GradeDTO();
            mapDtoFields(dto, editDto);
            mapDtoFields(dto, selectedGrade);
            selectedEvaluationId = dto.getEvaluationPk();
            selectedEnrolmentId = dto.getEnrolmentPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nota não encontrada");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma nota selecionada");
            return;
        }
        this.selectedId = id;
    }

    public void deleteGrade() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma nota selecionada para eliminar");
            return;
        }
        try {
            gradeService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Nota eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar nota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            if (editDto.getScore() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A nota é obrigatória.");
                return;
            }

            gradeService.update(editDto);
            init();
            editDto = new GradeDTO();
            selectedId = null;
            selectedEvaluationId = null;
            selectedEnrolmentId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Nota atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar nota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void viewGradeDetails(Long id) {
        if (id == null) return;
        GradeDTO dto = gradeService.getAllGrades().stream()
                .filter(g -> id.equals(g.getPkGrade())).findFirst().orElse(null);
        if (dto != null) {
            this.selectedGrade = dto;
            this.selectedId = id;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPORT
    // ═══════════════════════════════════════════════════════════════

    public void exportPdf() {
        addMessage(FacesMessage.SEVERITY_INFO, "Exportar", "Funcionalidade PDF em desenvolvimento.");
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

    private void mapDtoFields(GradeDTO source, GradeDTO target) {
        target.setPkGrade(source.getPkGrade());
        target.setEvaluationPk(source.getEvaluationPk());
        target.setEvaluationName(source.getEvaluationName());
        target.setEvaluationType(source.getEvaluationType());
        target.setTrimester(source.getTrimester());
        target.setDisciplineName(source.getDisciplineName());
        target.setEnrolmentPk(source.getEnrolmentPk());
        target.setStudentFullName(source.getStudentFullName());
        target.setStudentNumber(source.getStudentNumber());
        target.setSchoolClassName(source.getSchoolClassName());
        target.setScore(source.getScore());
        target.setLaunchDate(source.getLaunchDate());
        target.setObs(source.getObs());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public GradeDTO getEditDto() { return editDto; }
    public void setEditDto(GradeDTO editDto) { this.editDto = editDto; }
    public GradeDTO getSelectedGrade() { return selectedGrade; }
    public void setSelectedGrade(GradeDTO selectedGrade) { this.selectedGrade = selectedGrade; }
    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }

    public Long getSelectedEvaluationId() { return selectedEvaluationId; }
    public void setSelectedEvaluationId(Long selectedEvaluationId) { this.selectedEvaluationId = selectedEvaluationId; }
    public Long getSelectedEnrolmentId() { return selectedEnrolmentId; }
    public void setSelectedEnrolmentId(Long selectedEnrolmentId) { this.selectedEnrolmentId = selectedEnrolmentId; }

    public GradeLazyModel getLazyModel() { return lazyModel; }
    public void setLazyModel(GradeLazyModel lazyModel) { this.lazyModel = lazyModel; }
    public Long getFilterEvaluationId() { return filterEvaluationId; }
    public void setFilterEvaluationId(Long filterEvaluationId) { this.filterEvaluationId = filterEvaluationId; }
    public Long getFilterEnrolmentId() { return filterEnrolmentId; }
    public void setFilterEnrolmentId(Long filterEnrolmentId) { this.filterEnrolmentId = filterEnrolmentId; }
    public String getFilterStudentName() { return filterStudentName; }
    public void setFilterStudentName(String filterStudentName) { this.filterStudentName = filterStudentName; }
    public String getFilterDisciplineName() { return filterDisciplineName; }
    public void setFilterDisciplineName(String filterDisciplineName) { this.filterDisciplineName = filterDisciplineName; }
    public long getTotalGradeCount() { return totalGradeCount; }
    public double getAverageScore() { return averageScore; }
    public List<EvaluationDTO> getEvaluations() { return evaluations; }
    public List<EnrolmentDTO> getEnrolments() { return enrolments; }
    public EvaluationType[] getEvaluationTypes() { return EvaluationType.values(); }
    public Trimester[] getTrimesters() { return Trimester.values(); }
    public List<GradeDTO> getGrades() { return gradeService.getAllGrades(); }
}