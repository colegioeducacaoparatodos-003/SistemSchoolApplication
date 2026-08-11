package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.lazy.GradeLazyModel;
import com.SistemSchool.modulo_pedagogico.io.GradeStatus;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.service.EvaluationService;
import com.SistemSchool.modulo_pedagogico.service.GradeService;
import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class GradeController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(GradeController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Grade grade = new Grade();

    private GradeDTO editDto = new GradeDTO();
    private GradeDTO selectedGrade = new GradeDTO();
    private Long selectedId;

    // Ids escolhidos no dropdown do formulário
    private Long selectedEvaluationId;
    private Long selectedEnrolmentId;
    private Long selectedStudentId;

    // Listas para a view (dropdown), carregadas uma vez
    private List<EvaluationDTO> evaluations = new java.util.ArrayList<>();
    private List<EnrolmentDTO> enrolments = new java.util.ArrayList<>();
    private List<StudentDTO> students = new java.util.ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalGradesCount;
    private long approvedCount;
    private long pendingCount;
    private double classAverage;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private GradeService gradeService;

    @Inject
    private EvaluationService evaluationService;

    @Inject
    private EnrolmentService enrolmentService;

    @Inject
    private StudentService studentService;

    private transient GradeLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new GradeLazyModel(gradeService);
        loadEvaluations();
        loadEnrolments();
        loadStudents();
        computeStatistics();
    }

    private void loadEvaluations() {
        try {
            evaluations = evaluationService.getAllEvaluations();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar avaliações", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar avaliações para o formulário de notas", e);
        }
    }

    private void loadEnrolments() {
        try {
            enrolments = enrolmentService.getAllEnrolments();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matrículas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar matrículas para o formulário de notas", e);
        }
    }

    private void loadStudents() {
        try {
            students = studentService.getAllStudents();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos para o formulário de notas", e);
        }
    }

    private void computeStatistics() {
        try {
            List<GradeDTO> all = gradeService.getAllGrades();

            totalGradesCount = all.size();

            approvedCount = all.stream()
                    .filter(g -> g.getStatus() == GradeStatus.APPROVED)
                    .count();

            pendingCount = all.stream()
                    .filter(g -> g.getStatus() == GradeStatus.PENDING)
                    .count();

            classAverage = all.stream()
                    .filter(g -> g.getValue() != null)
                    .mapToDouble(GradeDTO::getValue)
                    .average()
                    .orElse(0.0);

        } catch (Exception e) {
            totalGradesCount = 0;
            approvedCount = 0;
            pendingCount = 0;
            classAverage = 0.0;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de notas", e);
        }
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

    public GradeLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public String saveGrade() {
        try {
            if (selectedEvaluationId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Nota", "Selecione uma avaliação antes de gravar.");
                return null;
            }
            if (selectedEnrolmentId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Nota", "Selecione uma matrícula antes de gravar.");
                return null;
            }
            if (selectedStudentId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Nota", "Selecione um aluno antes de gravar.");
                return null;
            }

            Evaluation evaluation = evaluationService.findById(selectedEvaluationId);
            Enrolment enrolment = enrolmentService.findById(selectedEnrolmentId);
            Student student = studentService.findById(selectedStudentId);

            grade.setEvaluation(evaluation);
            grade.setEnrolment(enrolment);
            grade.setStudent(student);

            gradeService.save(grade);

            grade = new Grade();
            selectedEvaluationId = null;
            selectedEnrolmentId = null;
            selectedStudentId = null;
            init();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Nota", "Nota registada com sucesso");

            return "/management/pedagogico/grades.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar nota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Nota", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma nota selecionada!", "");
            return;
        }

        GradeDTO dto = gradeService.getAllGrades()
                .stream()
                .filter(g -> selectedId.equals(g.getPkGrade()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, editDto = new GradeDTO());
            mapDtoFields(dto, selectedGrade);
            selectedEvaluationId = dto.getEvaluationPk();
            selectedEnrolmentId = dto.getEnrolmentPk();
            selectedStudentId = dto.getStudentPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Nota não encontrada", "");
        }
    }

    public void loadSelectedGrade() {
        if (selectedId == null) {
            return;
        }

        GradeDTO dto = gradeService.getAllGrades()
                .stream()
                .filter(g -> selectedId.equals(g.getPkGrade()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, selectedGrade);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Nota não encontrada", "");
        }
    }

    private void mapDtoFields(GradeDTO source, GradeDTO target) {
        target.setPkGrade(source.getPkGrade());
        target.setEvaluationPk(source.getEvaluationPk());
        target.setEvaluationDescription(source.getEvaluationDescription());
        target.setEnrolmentPk(source.getEnrolmentPk());
        target.setEnrolmentNumber(source.getEnrolmentNumber());
        target.setStudentPk(source.getStudentPk());
        target.setStudentFullName(source.getStudentFullName());
        target.setValue(source.getValue());
        target.setStatus(source.getStatus());
        target.setObservation(source.getObservation());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (selectedEvaluationId != null) {
                editDto.setEvaluationPk(selectedEvaluationId);
            }
            if (selectedEnrolmentId != null) {
                editDto.setEnrolmentPk(selectedEnrolmentId);
            }
            if (selectedStudentId != null) {
                editDto.setStudentPk(selectedStudentId);
            }
            gradeService.update(editDto);
            init();
            editDto = new GradeDTO();
            selectedId = null;
            selectedEvaluationId = null;
            selectedEnrolmentId = null;
            selectedStudentId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Nota", "Nota atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar nota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Nota", e.getMessage());
        }
    }

    public void delete() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma nota selecionada!", "");
            return;
        }
        try {
            gradeService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Nota", "Nota eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar nota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Nota", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UTIL
    // ─────────────────────────────────────────────────────────────

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS E SETTERS
    // ─────────────────────────────────────────────────────────────

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public GradeDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(GradeDTO editDto) {
        this.editDto = editDto;
    }

    public GradeDTO getSelectedGrade() {
        return selectedGrade;
    }

    public void setSelectedGrade(GradeDTO selectedGrade) {
        this.selectedGrade = selectedGrade;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public Long getSelectedEvaluationId() {
        return selectedEvaluationId;
    }

    public void setSelectedEvaluationId(Long selectedEvaluationId) {
        this.selectedEvaluationId = selectedEvaluationId;
    }

    public Long getSelectedEnrolmentId() {
        return selectedEnrolmentId;
    }

    public void setSelectedEnrolmentId(Long selectedEnrolmentId) {
        this.selectedEnrolmentId = selectedEnrolmentId;
    }

    public Long getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(Long selectedStudentId) {
        this.selectedStudentId = selectedStudentId;
    }

    public void setLazyModel(GradeLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS — GETTERS (nomes usados na view grades.xhtml)
    // ─────────────────────────────────────────────────────────────

    public long getTotalGradesCount() {
        return totalGradesCount;
    }

    public long getApprovedCount() {
        return approvedCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public double getClassAverage() {
        return classAverage;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS E LISTAS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public GradeStatus[] getStatuses() {
        return GradeStatus.values();
    }

    public List<EvaluationDTO> getEvaluations() {
        return evaluations;
    }

    public List<EnrolmentDTO> getEnrolments() {
        return enrolments;
    }

    public List<StudentDTO> getStudents() {
        return students;
    }

    public void refreshEvaluations() {
        loadEvaluations();
    }

    public void refreshEnrolments() {
        loadEnrolments();
    }

    public void refreshStudents() {
        loadStudents();
    }

    public List<GradeDTO> getGrades() {
        return gradeService.getAllGrades();
    }
}