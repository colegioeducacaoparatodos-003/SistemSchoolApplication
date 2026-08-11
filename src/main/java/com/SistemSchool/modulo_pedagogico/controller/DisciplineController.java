package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.lazy.DisciplineLazyModel;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class DisciplineController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(DisciplineController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Discipline discipline = new Discipline();

    private DisciplineDTO editDto = new DisciplineDTO();
    private DisciplineDTO selectedDiscipline = new DisciplineDTO();
    private Long selectedId;

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalDisciplineCount;
    private long activeDisciplineCount;
    private long inactiveDisciplineCount;
    private int totalWorkloadCount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private DisciplineService disciplineService;

    private transient DisciplineLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new DisciplineLazyModel(disciplineService);
        computeStatistics();
    }

    private void computeStatistics() {
        try {
            totalDisciplineCount = disciplineService.getTotalDisciplineCount();
            activeDisciplineCount = disciplineService.getActiveDisciplineCount();
            inactiveDisciplineCount = disciplineService.getInactiveDisciplineCount();
            totalWorkloadCount = disciplineService.getTotalWorkloadCount();
        } catch (Exception e) {
            totalDisciplineCount = 0;
            activeDisciplineCount = 0;
            inactiveDisciplineCount = 0;
            totalWorkloadCount = 0;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de disciplinas", e);
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de disciplinas", e);
        }
        return "/management/pedagogico/discipline.xhtml?faces-redirect=true";
    }

    public DisciplineLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public String saveDiscipline() {
        try {
            disciplineService.save(discipline);

            discipline = new Discipline();
            init();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Disciplina", "Disciplina registada com sucesso");

            return "/management/pedagogico/discipline.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar disciplina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Disciplina", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma disciplina selecionada!", "");
            return;
        }

        DisciplineDTO dto = disciplineService.getAllDisciplines()
                .stream()
                .filter(d -> selectedId.equals(d.getPkDiscipline()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, editDto = new DisciplineDTO());
            mapDtoFields(dto, selectedDiscipline);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Disciplina não encontrada", "");
        }
    }

    public void loadSelectedDiscipline() {
        if (selectedId == null) {
            return;
        }

        DisciplineDTO dto = disciplineService.getAllDisciplines()
                .stream()
                .filter(d -> selectedId.equals(d.getPkDiscipline()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, selectedDiscipline);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Disciplina não encontrada", "");
        }
    }

    private void mapDtoFields(DisciplineDTO source, DisciplineDTO target) {
        target.setPkDiscipline(source.getPkDiscipline());
        target.setDisciplineCode(source.getDisciplineCode());
        target.setDisciplineName(source.getDisciplineName());
        target.setWorkload(source.getWorkload());
        target.setStatus(source.getStatus());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            disciplineService.update(editDto);
            init();
            editDto = new DisciplineDTO();
            selectedId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Disciplina", "Disciplina atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar disciplina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Disciplina", e.getMessage());
        }
    }

    public void delete() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma disciplina selecionada!", "");
            return;
        }
        try {
            disciplineService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Disciplina", "Disciplina eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar disciplina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Disciplina", e.getMessage());
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

    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public DisciplineDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(DisciplineDTO editDto) {
        this.editDto = editDto;
    }

    public DisciplineDTO getSelectedDiscipline() {
        return selectedDiscipline;
    }

    public void setSelectedDiscipline(DisciplineDTO selectedDiscipline) {
        this.selectedDiscipline = selectedDiscipline;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public void setLazyModel(DisciplineLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS — GETTERS
    // ─────────────────────────────────────────────────────────────

    public long getTotalDisciplineCount() {
        return totalDisciplineCount;
    }

    public long getActiveDisciplineCount() {
        return activeDisciplineCount;
    }

    public long getInactiveDisciplineCount() {
        return inactiveDisciplineCount;
    }

    public int getTotalWorkloadCount() {
        return totalWorkloadCount;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public DisciplineStatus[] getStatuses() {
        return DisciplineStatus.values();
    }

    public List<DisciplineDTO> getDisciplines() {
        return disciplineService.getAllDisciplines();
    }
}