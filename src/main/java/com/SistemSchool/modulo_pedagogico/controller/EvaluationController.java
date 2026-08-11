package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.lazy.EvaluationLazyModel;
import com.SistemSchool.modulo_pedagogico.io.EvaluationStatus;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.EvaluationService;
import com.SistemSchool.modulo_pedagogico.service.ScheduleService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class EvaluationController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(EvaluationController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // UTIL - FORMATAÇÃO
    // ─────────────────────────────────────────────────────────────

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
    
    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Evaluation evaluation = new Evaluation();

    private EvaluationDTO editDto = new EvaluationDTO();
    private EvaluationDTO selectedEvaluation = new EvaluationDTO();
    private Long selectedId;

    // Ids escolhidos no dropdown do formulário
    private Long selectedDisciplineId;
    private Long selectedScheduleId;

    // Listas para a view (dropdown), carregadas uma vez
    private List<DisciplineDTO> disciplines = new java.util.ArrayList<>();
    private List<Discipline> disciplinesList = new java.util.ArrayList<>();
    private List<ScheduleDTO> schedules = new java.util.ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalEvaluationCount;
    private long newEvaluationCount; // avaliações registadas no mês corrente
    private long openEvaluationCount;
    private long closedEvaluationCount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private EvaluationService evaluationService;

    @Inject
    private DisciplineService disciplineService;

    @Inject
    private ScheduleService scheduleService;

    private transient EvaluationLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO E NAVEGAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new EvaluationLazyModel(evaluationService);
        loadDisciplines();
        loadSchedules();
        computeStatistics();
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllDisciplines();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas para o formulário de avaliação", e);
        }
    }

    private void loadSchedules() {
        try {
            schedules = scheduleService.getAllSchedules();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar horários", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar horários para o formulário de avaliação", e);
        }
    }

    private void computeStatistics() {
        try {
            List<EvaluationDTO> all = evaluationService.getAllEvaluations();

            totalEvaluationCount = all.size();

            YearMonth currentMonth = YearMonth.from(LocalDate.now());
            newEvaluationCount = all.stream()
                    .filter(e -> e.getEvaluationDate() != null)
                    .filter(e -> YearMonth.from(e.getEvaluationDate()).equals(currentMonth))
                    .count();

            openEvaluationCount = all.stream()
                    .filter(e -> e.getStatus() == EvaluationStatus.OPEN)
                    .count();

            closedEvaluationCount = all.stream()
                    .filter(e -> e.getStatus() == EvaluationStatus.CLOSED)
                    .count();

        } catch (Exception e) {
            totalEvaluationCount = 0;
            newEvaluationCount = 0;
            openEvaluationCount = 0;
            closedEvaluationCount = 0;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de avaliações", e);
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar avaliações", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de avaliações", e);
        }
        return "/management/pedagogico/evaluations.xhtml?faces-redirect=true";
    }

    public EvaluationLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public String saveEvaluation() {
        try {
            if (selectedDisciplineId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", "Selecione uma disciplina antes de gravar.");
                return null;
            }
            if (selectedScheduleId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", "Selecione um horário antes de gravar.");
                return null;
            }

            Discipline discipline = disciplineService.getById(selectedDisciplineId);
            Schedule schedule = scheduleService.findById(selectedScheduleId);
            evaluation.setDiscipline(discipline);
            evaluation.setSchedule(schedule);

            evaluationService.save(evaluation);

            evaluation = new Evaluation();
            selectedDisciplineId = null;
            selectedScheduleId = null;
            init();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Avaliação", "Avaliação registada com sucesso");

            return "/management/pedagogico/evaluations.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar avaliação", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma avaliação selecionada!", "");
            return;
        }

        EvaluationDTO dto = evaluationService.getAllEvaluations()
                .stream()
                .filter(e -> selectedId.equals(e.getPkEvaluation()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, editDto = new EvaluationDTO());
            mapDtoFields(dto, selectedEvaluation);
            selectedDisciplineId = dto.getDisciplinePk();
            selectedScheduleId = dto.getSchedulePk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Avaliação não encontrada", "");
        }
    }

    public void loadSelectedEvaluation() {
        if (selectedId == null) {
            return;
        }

        EvaluationDTO dto = evaluationService.getAllEvaluations()
                .stream()
                .filter(e -> selectedId.equals(e.getPkEvaluation()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, selectedEvaluation);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Avaliação não encontrada", "");
        }
    }

    private void mapDtoFields(EvaluationDTO source, EvaluationDTO target) {
        target.setPkEvaluation(source.getPkEvaluation());
        target.setDisciplinePk(source.getDisciplinePk());
        target.setDisciplineName(source.getDisciplineName());
        target.setSchedulePk(source.getSchedulePk());
        target.setScheduleWeekDay(source.getScheduleWeekDay());
        target.setTitle(source.getTitle());
        target.setType(source.getType());
        target.setWeight(source.getWeight());
        target.setEvaluationDate(source.getEvaluationDate());
        target.setStatus(source.getStatus());
        target.setTrimester(source.getTrimester());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (selectedDisciplineId != null) {
                editDto.setDisciplinePk(selectedDisciplineId);
            }
            if (selectedScheduleId != null) {
                editDto.setSchedulePk(selectedScheduleId);
            }
            evaluationService.update(editDto);
            init();
            editDto = new EvaluationDTO();
            selectedId = null;
            selectedDisciplineId = null;
            selectedScheduleId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Avaliação", "Avaliação atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar avaliação", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", e.getMessage());
        }
    }

    public void delete() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma avaliação selecionada!", "");
            return;
        }
        try {
            evaluationService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Avaliação", "Avaliação eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar avaliação", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", e.getMessage());
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

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public EvaluationDTO getEditDto() {
        return editDto;
    }

    public void setEditDto(EvaluationDTO editDto) {
        this.editDto = editDto;
    }

    public EvaluationDTO getSelectedEvaluation() {
        return selectedEvaluation;
    }

    public void setSelectedEvaluation(EvaluationDTO selectedEvaluation) {
        this.selectedEvaluation = selectedEvaluation;
    }

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long selectedId) {
        this.selectedId = selectedId;
    }

    public Long getSelectedDisciplineId() {
        return selectedDisciplineId;
    }

    public void setSelectedDisciplineId(Long selectedDisciplineId) {
        this.selectedDisciplineId = selectedDisciplineId;
    }

    public Long getSelectedScheduleId() {
        return selectedScheduleId;
    }

    public void setSelectedScheduleId(Long selectedScheduleId) {
        this.selectedScheduleId = selectedScheduleId;
    }

    public void setLazyModel(EvaluationLazyModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS — GETTERS
    // ─────────────────────────────────────────────────────────────

    public long getTotalEvaluationCount() {
        return totalEvaluationCount;
    }

    public long getNewEvaluationCount() {
        return newEvaluationCount;
    }

    public long getOpenEvaluationCount() {
        return openEvaluationCount;
    }

    public long getClosedEvaluationCount() {
        return closedEvaluationCount;
    }

    // ─────────────────────────────────────────────────────────────
    // ENUMS E LISTAS PARA DROPDOWNS
    // ─────────────────────────────────────────────────────────────

    public EvaluationType[] getTypes() {
        return EvaluationType.values();
    }

    public EvaluationStatus[] getStatuses() {
        return EvaluationStatus.values();
    }

    public List<DisciplineDTO> getDisciplines() {
        return disciplines;
    }

    public List<ScheduleDTO> getSchedules() {
        return schedules;
    }

    public void refreshDisciplines() {
        loadDisciplines();
    }

    public void refreshSchedules() {
        loadSchedules();
    }

    public List<EvaluationDTO> getEvaluations() {
        return evaluationService.getAllEvaluations();
    }
}