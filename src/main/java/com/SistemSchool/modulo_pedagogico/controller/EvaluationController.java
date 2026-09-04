package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.lazy.EvaluationLazyModel;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.EvaluationService;

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
public class EvaluationController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EvaluationController.class.getName());

    // ── MODELOS ──
    private Evaluation evaluation = new Evaluation();
    private EvaluationDTO editDto = new EvaluationDTO();
    private EvaluationDTO selectedEvaluation = new EvaluationDTO();
    private Long selectedId;
    private Long selectedDisciplineId;

    // ── FILTROS AVANÇADOS ──
    private Long filterDisciplineId;
    private String filterEvaluationType;
    private String filterTrimester;
    private String filterAnoLectivo;

    // ── LISTAS ──
    private List<Discipline> disciplines = new ArrayList<>();

    // ── ESTATÍSTICAS ──
    private long totalEvaluationCount;
    private long continuaCount;
    private long provaTrimestralCount;

    // ── SERVIÇOS ──
    @Inject
    private EvaluationService evaluationService;
    @Inject
    private DisciplineService disciplineService;
    private transient EvaluationLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new EvaluationLazyModel(evaluationService);
        loadDisciplines();
        computeStatistics();
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

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterDisciplineId = null;
        filterEvaluationType = null;
        filterTrimester = null;
        filterAnoLectivo = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    public List<EvaluationDTO> getFilteredEvaluations() {
        List<EvaluationDTO> all = evaluationService.getAllEvaluations();

        return all.stream()
                .filter(e -> filterDisciplineId == null ||
                        (e.getDisciplinePk() != null && e.getDisciplinePk().equals(filterDisciplineId)))
                .filter(e -> filterEvaluationType == null || filterEvaluationType.isBlank() ||
                        (e.getEvaluationType() != null && e.getEvaluationType().toString().equalsIgnoreCase(filterEvaluationType)))
                .filter(e -> filterTrimester == null || filterTrimester.isBlank() ||
                        (e.getTrimester() != null && e.getTrimester().toString().equalsIgnoreCase(filterTrimester)))
                .filter(e -> filterAnoLectivo == null || filterAnoLectivo.isBlank() ||
                        (e.getAnoLectivo() != null && e.getAnoLectivo().contains(filterAnoLectivo)))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllActive();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
        }
    }

    private void computeStatistics() {
        try {
            List<EvaluationDTO> all = getFilteredEvaluations();
            totalEvaluationCount = all.size();
            continuaCount = all.stream().filter(e -> e.getEvaluationType() == EvaluationType.CONTINUA).count();
            provaTrimestralCount = all.stream().filter(e -> e.getEvaluationType() == EvaluationType.PROVA_TRIMESTRAL).count();
        } catch (Exception e) {
            totalEvaluationCount = 0;
            continuaCount = 0;
            provaTrimestralCount = 0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    public void prepareNewEvaluation() {
        this.evaluation = new Evaluation();
        this.selectedDisciplineId = null;
        loadDisciplines();
    }

    public String saveEvaluation() {
        try {
            // Validação completa de todos os campos obrigatórios
            if (selectedDisciplineId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", "Disciplina é obrigatória.");
                return null;
            }
            if (evaluation.getEvaluationName() == null || evaluation.getEvaluationName().isBlank()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", "Nome da avaliação é obrigatório.");
                return null;
            }
            if (evaluation.getEvaluationType() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", "Tipo de avaliação é obrigatório.");
                return null;
            }
            if (evaluation.getTrimester() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Avaliação", "Trimestre é obrigatório.");
                return null;
            }

            Discipline discipline = disciplines.stream()
                    .filter(d -> selectedDisciplineId.equals(d.getPkDiscipline()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
            evaluation.setDiscipline(discipline);

            evaluationService.save(evaluation);
            
            // Reset após sucesso
            this.evaluation = new Evaluation();
            this.selectedDisciplineId = null;
            init();
            
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Avaliação registada com sucesso");
            return "/management/pedagogico/evaluations.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar avaliação", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
            return null;
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma avaliação selecionada");
            return;
        }
        this.selectedId = id;
        EvaluationDTO dto = evaluationService.getAllEvaluations().stream()
                .filter(e -> id.equals(e.getPkEvaluation()))
                .findFirst()
                .orElse(null);
        if (dto != null) {
            this.editDto = new EvaluationDTO();
            mapDtoFields(dto, this.editDto);
            mapDtoFields(dto, this.selectedEvaluation);
            this.selectedDisciplineId = dto.getDisciplinePk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Avaliação não encontrada");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma avaliação selecionada");
            return;
        }
        this.selectedId = id;
    }

    public void deleteEvaluation() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhuma avaliação selecionada para eliminar");
            return;
        }
        try {
            evaluationService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Avaliação eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar avaliação", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            if (selectedDisciplineId != null) {
                editDto.setDisciplinePk(selectedDisciplineId);
            }
            evaluationService.update(editDto);
            init();
            editDto = new EvaluationDTO();
            selectedId = null;
            selectedDisciplineId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Avaliação atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar avaliação", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void viewEvaluationDetails(Long id) {
        if (id == null) return;
        EvaluationDTO dto = evaluationService.getAllEvaluations().stream()
                .filter(e -> id.equals(e.getPkEvaluation()))
                .findFirst()
                .orElse(null);
        if (dto != null) {
            this.selectedEvaluation = dto;
            this.selectedId = id;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

    private void mapDtoFields(EvaluationDTO source, EvaluationDTO target) {
        target.setPkEvaluation(source.getPkEvaluation());
        target.setDisciplinePk(source.getDisciplinePk());
        target.setDisciplineName(source.getDisciplineName());
        target.setEvaluationName(source.getEvaluationName());
        target.setEvaluationType(source.getEvaluationType());
        target.setTrimester(source.getTrimester());
        target.setEvaluationDate(source.getEvaluationDate());
        target.setAnoLectivo(source.getAnoLectivo());
        target.setObs(source.getObs());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }
    public EvaluationDTO getEditDto() { return editDto; }
    public void setEditDto(EvaluationDTO editDto) { this.editDto = editDto; }
    public EvaluationDTO getSelectedEvaluation() { return selectedEvaluation; }
    public void setSelectedEvaluation(EvaluationDTO selectedEvaluation) { this.selectedEvaluation = selectedEvaluation; }
    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }
    public Long getSelectedDisciplineId() { return selectedDisciplineId; }
    public void setSelectedDisciplineId(Long selectedDisciplineId) { this.selectedDisciplineId = selectedDisciplineId; }
    public EvaluationLazyModel getLazyModel() { return lazyModel; }
    public void setLazyModel(EvaluationLazyModel lazyModel) { this.lazyModel = lazyModel; }
    public Long getFilterDisciplineId() { return filterDisciplineId; }
    public void setFilterDisciplineId(Long filterDisciplineId) { this.filterDisciplineId = filterDisciplineId; }
    public String getFilterEvaluationType() { return filterEvaluationType; }
    public void setFilterEvaluationType(String filterEvaluationType) { this.filterEvaluationType = filterEvaluationType; }
    public String getFilterTrimester() { return filterTrimester; }
    public void setFilterTrimester(String filterTrimester) { this.filterTrimester = filterTrimester; }
    public String getFilterAnoLectivo() { return filterAnoLectivo; }
    public void setFilterAnoLectivo(String filterAnoLectivo) { this.filterAnoLectivo = filterAnoLectivo; }
    public long getTotalEvaluationCount() { return totalEvaluationCount; }
    public long getContinuaCount() { return continuaCount; }
    public long getProvaTrimestralCount() { return provaTrimestralCount; }
    public List<Discipline> getDisciplines() { return disciplines; }
    public EvaluationType[] getEvaluationTypes() { return EvaluationType.values(); }
    public Trimester[] getTrimesters() { return Trimester.values(); }
    public List<EvaluationDTO> getEvaluations() { return evaluationService.getAllEvaluations(); }
}