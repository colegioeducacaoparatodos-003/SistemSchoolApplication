package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.dto.MiniPautaDTO;
import com.SistemSchool.modulo_pedagogico.dto.MiniPautaStudentRowDTO;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.MiniPautaService;

import com.SistemSchool.modulo_secrtaria.dto.SchoolClassDTO;
import com.SistemSchool.modulo_secrtaria.service.SchoolClassService;

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
public class MiniPautaController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MiniPautaController.class.getName());

    // Seleção
    private Long selectedDisciplineId;
    private Long selectedSchoolClassId;
    private Integer selectedTrimester = 1;

    // Resultado
    private MiniPautaDTO miniPauta;
    private MiniPautaStudentRowDTO selectedRow;

    // Listas dropdown
    private List<DisciplineDTO> disciplines = new ArrayList<>();
    private List<SchoolClassDTO> schoolClasses = new ArrayList<>();
    private Integer[] trimesters = {1, 2, 3};

    @Inject
    private MiniPautaService miniPautaService;

    @Inject
    private DisciplineService disciplineService;

    @Inject
    private SchoolClassService schoolClassService;

    @PostConstruct
    public void init() {
        loadDisciplines();
        loadSchoolClasses();
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllDisciplines();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
        }
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassService.getAllSchoolClasses();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas", e);
        }
    }

    public void generateMiniPauta() {
        if (selectedDisciplineId == null || selectedSchoolClassId == null || selectedTrimester == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Seleção incompleta",
                    "Selecione a disciplina, turma e trimestre antes de gerar a mini-pauta.");
            return;
        }
        try {
            miniPauta = miniPautaService.generateMiniPauta(
                    selectedDisciplineId, selectedSchoolClassId, selectedTrimester);
            addMessage(FacesMessage.SEVERITY_INFO, "Mini-Pauta",
                    "Documento gerado com sucesso para " + miniPauta.getDisciplineName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar mini-pauta", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar mini-pauta", e.getMessage());
        }
    }

    public void clear() {
        miniPauta = null;
        selectedDisciplineId = null;
        selectedSchoolClassId = null;
        selectedTrimester = 1;
    }

    public void printMiniPauta() {
        // Chamar JasperReports ou similar
        addMessage(FacesMessage.SEVERITY_INFO, "Impressão", "Mini-Pauta enviada para impressão.");
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters / Setters
    public Long getSelectedDisciplineId() { return selectedDisciplineId; }
    public void setSelectedDisciplineId(Long selectedDisciplineId) { this.selectedDisciplineId = selectedDisciplineId; }

    public Long getSelectedSchoolClassId() { return selectedSchoolClassId; }
    public void setSelectedSchoolClassId(Long selectedSchoolClassId) { this.selectedSchoolClassId = selectedSchoolClassId; }

    public Integer getSelectedTrimester() { return selectedTrimester; }
    public void setSelectedTrimester(Integer selectedTrimester) { this.selectedTrimester = selectedTrimester; }

    public MiniPautaDTO getMiniPauta() { return miniPauta; }
    public void setMiniPauta(MiniPautaDTO miniPauta) { this.miniPauta = miniPauta; }

    public MiniPautaStudentRowDTO getSelectedRow() { return selectedRow; }
    public void setSelectedRow(MiniPautaStudentRowDTO selectedRow) { this.selectedRow = selectedRow; }

    public List<DisciplineDTO> getDisciplines() { return disciplines; }
    public List<SchoolClassDTO> getSchoolClasses() { return schoolClasses; }
    public Integer[] getTrimesters() { return trimesters; }
}
