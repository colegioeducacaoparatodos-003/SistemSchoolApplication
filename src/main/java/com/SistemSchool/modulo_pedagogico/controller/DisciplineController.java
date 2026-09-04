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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DisciplineController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DisciplineController.class.getName());

    // ── MODELOS ──
    private Discipline discipline = new Discipline();
    private DisciplineDTO editDto = new DisciplineDTO();
    private DisciplineDTO selectedDiscipline = new DisciplineDTO();
    private Long selectedId;

    // ── FILTROS AVANÇADOS ──
    private String filterDisciplineCode;
    private String filterDisciplineName;
    private String filterStatus;

    // ── ESTATÍSTICAS ──
    private long totalDisciplineCount;
    private long activeCount;
    private long inactiveCount;

    // ── SERVIÇOS ──
    @Inject
    private DisciplineService disciplineService;
    private transient DisciplineLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new DisciplineLazyModel(disciplineService);
        computeStatistics();
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de disciplinas", e);
        }
        return "/management/pedagogico/disciplines.xhtml?faces-redirect=true";
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterDisciplineCode = null;
        filterDisciplineName = null;
        filterStatus = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    public List<DisciplineDTO> getFilteredDisciplines() {
        List<DisciplineDTO> all = disciplineService.getAllDisciplines();

        return all.stream()
                .filter(d -> filterDisciplineCode == null || filterDisciplineCode.isBlank() ||
                        (d.getDisciplineCode() != null
                                && d.getDisciplineCode().toLowerCase().contains(filterDisciplineCode.toLowerCase())))
                .filter(d -> filterDisciplineName == null || filterDisciplineName.isBlank() ||
                        (d.getDisciplineName() != null
                                && d.getDisciplineName().toLowerCase().contains(filterDisciplineName.toLowerCase())))
                .filter(d -> filterStatus == null || filterStatus.isBlank() ||
                        (d.getStatus() != null && d.getStatus().toString().equalsIgnoreCase(filterStatus)))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // ESTATÍSTICAS
    // ═══════════════════════════════════════════════════════════════

    private void computeStatistics() {
        try {
            List<DisciplineDTO> all = getFilteredDisciplines();
            totalDisciplineCount = all.size();
            activeCount = all.stream().filter(d -> d.getStatus() == DisciplineStatus.ATIVO).count();
            inactiveCount = all.stream().filter(d -> d.getStatus() == DisciplineStatus.INATIVO).count();
        } catch (Exception e) {
            totalDisciplineCount = 0;
            activeCount = 0;
            inactiveCount = 0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    public void prepareNewDiscipline() {
        discipline = new Discipline();
    }

    public String saveDiscipline() {
        try {
            if (discipline.getDisciplineName() == null || discipline.getDisciplineName().isBlank()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Disciplina", "Preencha todos os campos obrigatórios.");
                return null;
            }
            disciplineService.save(discipline);
            discipline = new Discipline();
            init();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Disciplina registada com sucesso");
            return "/management/pedagogico/disciplines.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar disciplina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
            return null;
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma disciplina selecionada");
            return;
        }
        this.selectedId = id;
        DisciplineDTO dto = disciplineService.getAllDisciplines().stream()
                .filter(d -> id.equals(d.getPkDiscipline())).findFirst().orElse(null);
        if (dto != null) {
            mapDtoFields(dto, editDto = new DisciplineDTO());
            mapDtoFields(dto, selectedDiscipline);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Disciplina não encontrada");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma disciplina selecionada");
            return;
        }
        this.selectedId = id;
    }

    public void deleteDiscipline() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma disciplina selecionada para eliminar");
            return;
        }
        try {
            disciplineService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Disciplina eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar disciplina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            disciplineService.update(editDto);
            init();
            editDto = new DisciplineDTO();
            selectedId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Disciplina atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar disciplina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void viewDisciplineDetails(Long id) {
        if (id == null)
            return;
        DisciplineDTO dto = disciplineService.getAllDisciplines().stream()
                .filter(d -> id.equals(d.getPkDiscipline())).findFirst().orElse(null);
        if (dto != null) {
            this.selectedDiscipline = dto;
            this.selectedId = id;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

    private void mapDtoFields(DisciplineDTO source, DisciplineDTO target) {
        target.setPkDiscipline(source.getPkDiscipline());
        target.setDisciplineCode(source.getDisciplineCode());
        target.setDisciplineName(source.getDisciplineName());
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus());
        target.setObs(source.getObs());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

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

    public DisciplineLazyModel getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(DisciplineLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public String getFilterDisciplineCode() {
        return filterDisciplineCode;
    }

    public void setFilterDisciplineCode(String filterDisciplineCode) {
        this.filterDisciplineCode = filterDisciplineCode;
    }

    public String getFilterDisciplineName() {
        return filterDisciplineName;
    }

    public void setFilterDisciplineName(String filterDisciplineName) {
        this.filterDisciplineName = filterDisciplineName;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }

    public long getTotalDisciplineCount() {
        return totalDisciplineCount;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public long getInactiveCount() {
        return inactiveCount;
    }

    public DisciplineStatus[] getStatuses() {
        return DisciplineStatus.values();
    }

    public List<DisciplineDTO> getDisciplines() {
        return disciplineService.getAllDisciplines();
    }
}