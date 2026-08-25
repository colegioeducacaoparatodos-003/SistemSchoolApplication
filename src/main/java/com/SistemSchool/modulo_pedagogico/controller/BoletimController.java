package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.BoletimDTO;
import com.SistemSchool.modulo_pedagogico.service.BoletimService;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class BoletimController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BoletimController.class.getName());

    // Seleção
    private Long selectedStudentId;
    private Long selectedEnrolmentId;
    private Integer selectedTrimester = 1;

    // Resultado
    private BoletimDTO boletim;

    // Listas dropdown
    private List<StudentDTO> students = new ArrayList<>();
    private List<EnrolmentDTO> enrolments = new ArrayList<>();
    private Integer[] trimesters = {1, 2, 3};

    @Inject
    private BoletimService boletimService;

    @Inject
    private StudentService studentService;

    @Inject
    private EnrolmentService enrolmentService;

    @PostConstruct
    public void init() {
        loadStudents();
        loadEnrolments();
    }

    private void loadStudents() {
        try {
            students = studentService.getAllStudents();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar alunos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar alunos", e);
        }
    }

    private void loadEnrolments() {
        try {
            enrolments = enrolmentService.getAllEnrolments();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matrículas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar matrículas", e);
        }
    }

    public void generateBoletim() {
        if (selectedStudentId == null || selectedEnrolmentId == null || selectedTrimester == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Seleção incompleta",
                    "Selecione o aluno, matrícula e trimestre antes de gerar o boletim.");
            return;
        }
        try {
            boletim = boletimService.generateBoletim(
                    selectedStudentId, selectedEnrolmentId, selectedTrimester);
            addMessage(FacesMessage.SEVERITY_INFO, "Boletim",
                    "Documento gerado com sucesso para " + boletim.getStudentFullName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar boletim", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar boletim", e.getMessage());
        }
    }

    public void clear() {
        boletim = null;
        selectedStudentId = null;
        selectedEnrolmentId = null;
        selectedTrimester = 1;
    }

    public void printBoletim() {
        addMessage(FacesMessage.SEVERITY_INFO, "Impressão", "Boletim enviado para impressão.");
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters / Setters
    public Long getSelectedStudentId() { return selectedStudentId; }
    public void setSelectedStudentId(Long selectedStudentId) { this.selectedStudentId = selectedStudentId; }

    public Long getSelectedEnrolmentId() { return selectedEnrolmentId; }
    public void setSelectedEnrolmentId(Long selectedEnrolmentId) { this.selectedEnrolmentId = selectedEnrolmentId; }

    public Integer getSelectedTrimester() { return selectedTrimester; }
    public void setSelectedTrimester(Integer selectedTrimester) { this.selectedTrimester = selectedTrimester; }

    public BoletimDTO getBoletim() { return boletim; }
    public void setBoletim(BoletimDTO boletim) { this.boletim = boletim; }

    public List<StudentDTO> getStudents() { return students; }
    public List<EnrolmentDTO> getEnrolments() { return enrolments; }
    public Integer[] getTrimesters() { return trimesters; }
}
