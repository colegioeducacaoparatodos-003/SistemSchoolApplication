package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.modulo_secrtaria.dto.SchoolClassDTO;
import com.SistemSchool.modulo_secrtaria.lazy.SchoolClassLazyModel;
import com.SistemSchool.modulo_secrtaria.io.Classe;
import com.SistemSchool.modulo_secrtaria.io.SchoolClaassStatus;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.service.SchoolClassService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class SchoolClassController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(SchoolClassController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private SchoolClass schoolClass = new SchoolClass();

    private SchoolClassDTO editDto = new SchoolClassDTO();
    private SchoolClassDTO selectedSchoolClass = new SchoolClassDTO();
    private Long selectedId;

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    private Classe filterClasse;
    private ShiftType filterTurno;
    private SchoolClaassStatus filterStatus;
    private String filterAnoLectivo;
    private String filterSearchText;

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalSchoolClassCount;
    private long activeSchoolClassCount;
    private long distinctTurnosCount;
    private long totalCapacityCount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private SchoolClassService schoolClassService;

    private transient SchoolClassLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new SchoolClassLazyModel(schoolClassService);
        computeStatistics();
    }

    private void computeStatistics() {
        try {
            List<SchoolClassDTO> all = schoolClassService.getAllSchoolClasses();

            totalSchoolClassCount = all.size();
            activeSchoolClassCount = all.stream()
                    .filter(sc -> sc.getStatus() != null)
                    .filter(sc -> sc.getStatus().isACTIVE())
                    .count();

            Set<Object> turnos = new HashSet<>();
            int totalCapacity = 0;
            for (SchoolClassDTO sc : all) {
                if (sc.getTurno() != null) {
                    turnos.add(sc.getTurno());
                }
                if (sc.getCapacidade() != null) {
                    totalCapacity += sc.getCapacidade();
                }
            }
            distinctTurnosCount = turnos.size();
            totalCapacityCount = totalCapacity;

        } catch (Exception e) {
            totalSchoolClassCount = 0;
            activeSchoolClassCount = 0;
            distinctTurnosCount = 0;
            totalCapacityCount = 0;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de turmas", e);
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de turmas", e);
        }
        return "/management/secretaria/schoolClasses.xhtml?faces-redirect=true";
    }

    public SchoolClassLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    public void applyFilters() {
        lazyModel.setFilterClasse(filterClasse);
        lazyModel.setFilterTurno(filterTurno);
        lazyModel.setFilterStatus(filterStatus);
        lazyModel.setFilterAnoLectivo(filterAnoLectivo);
        lazyModel.setSearchText(filterSearchText);
    }

    public void clearFilters() {
        filterClasse = null;
        filterTurno = null;
        filterStatus = null;
        filterAnoLectivo = null;
        filterSearchText = null;
        lazyModel.clearFilters();
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public String saveSchoolClass() {
        try {
            schoolClassService.save(schoolClass);

            schoolClass = new SchoolClass();
            init();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Turma", "Turma registada com sucesso");

            return "/management/secretaria/schoolClasses.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar turma", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Turma", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma turma selecionada!", "");
            return;
        }

        SchoolClassDTO dto = schoolClassService.getAllSchoolClasses()
                .stream()
                .filter(sc -> selectedId.equals(sc.getPkSchoolClass()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, editDto = new SchoolClassDTO());
            mapDtoFields(dto, selectedSchoolClass);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Turma não encontrada", "");
        }
    }

    public void loadSelectedSchoolClass() {
        if (selectedId == null) {
            return;
        }

        SchoolClassDTO dto = schoolClassService.getAllSchoolClasses()
                .stream()
                .filter(sc -> selectedId.equals(sc.getPkSchoolClass()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, selectedSchoolClass);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Turma não encontrada", "");
        }
    }

    private void mapDtoFields(SchoolClassDTO source, SchoolClassDTO target) {
        target.setPkSchoolClass(source.getPkSchoolClass());
        target.setClassCode(source.getClassCode());
        target.setClassName(source.getClassName());
        target.setClasse(source.getClasse());
        target.setTurno(source.getTurno());
        target.setAnoLectivo(source.getAnoLectivo());
        target.setCapacidade(source.getCapacidade());
        target.setRoom(source.getRoom());
        target.setStatus(source.getStatus());
        target.setObs(source.getObs());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            schoolClassService.update(editDto);
            init();
            editDto = new SchoolClassDTO();
            selectedId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Turma", "Turma atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar turma", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Turma", e.getMessage());
        }
    }

    public void delete() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma turma selecionada!", "");
            return;
        }
        try {
            schoolClassService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Turma", "Turma eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar turma", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Turma", e.getMessage());
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

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
    }

    public SchoolClassDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(SchoolClassDTO editDto) {
        this.editDto = editDto;
    }

    public SchoolClassDTO getSelectedSchoolClass() {
        return selectedSchoolClass;
    }

    public void setSelectedSchoolClass(SchoolClassDTO selectedSchoolClass) {
        this.selectedSchoolClass = selectedSchoolClass;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public void setLazyModel(SchoolClassLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS — GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

    public Classe getFilterClasse() {
        return filterClasse;
    }

    public void setFilterClasse(Classe filterClasse) {
        this.filterClasse = filterClasse;
    }

    public ShiftType getFilterTurno() {
        return filterTurno;
    }

    public void setFilterTurno(ShiftType filterTurno) {
        this.filterTurno = filterTurno;
    }

    public SchoolClaassStatus getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(SchoolClaassStatus filterStatus) {
        this.filterStatus = filterStatus;
    }

    public String getFilterAnoLectivo() {
        return filterAnoLectivo;
    }

    public void setFilterAnoLectivo(String filterAnoLectivo) {
        this.filterAnoLectivo = filterAnoLectivo;
    }

    public String getFilterSearchText() {
        return filterSearchText;
    }

    public void setFilterSearchText(String filterSearchText) {
        this.filterSearchText = filterSearchText;
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS — GETTERS
    // ─────────────────────────────────────────────────────────────

    public long getTotalSchoolClassCount() {
        return totalSchoolClassCount;
    }

    public long getActiveSchoolClassCount() {
        return activeSchoolClassCount;
    }

    public long getDistinctTurnosCount() {
        return distinctTurnosCount;
    }

    public long getTotalCapacityCount() {
        return totalCapacityCount;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS E LISTAS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public Classe[] getClasses() {
        return Classe.values();
    }

    public ShiftType[] getTurnos() {
        return ShiftType.values();
    }

    public SchoolClaassStatus[] getStatuses() {
        return SchoolClaassStatus.values();
    }

    public List<SchoolClassDTO> getSchoolClasses() {
        return schoolClassService.getAllSchoolClasses();
    }
}