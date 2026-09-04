package com.SistemSchool.modulo_pedagogico.controller;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.lazy.TrimesterResultLazyModel;
import com.SistemSchool.modulo_pedagogico.io.SituationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.TrimesterResult;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

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
public class TrimesterResultController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TrimesterResultController.class.getName());

    // ── MODELOS ──
    private TrimesterResult trimesterResult = new TrimesterResult();
    private TrimesterResultDTO editDto = new TrimesterResultDTO();
    private TrimesterResultDTO selectedResult = new TrimesterResultDTO();
    private Long selectedId;
    private Long selectedEnrolmentId;
    private Long selectedDisciplineId;

    // ── FILTROS AVANÇADOS ──
    private Long filterSchoolClassId;
    private Long filterDisciplineId;
    private String filterTrimester;
    private String filterSituation;
    private String filterStudentName;

    // ── LISTAS ──
    private List<SchoolClass> schoolClasses = new ArrayList<>();
    private List<Discipline> disciplines = new ArrayList<>();

    // ── ESTATÍSTICAS ──
    private long totalResultCount;
    private long approvedCount;
    private long failedCount;
    private long inProgressCount;

    // ── LANÇAMENTO DE NOTAS ──
    private Long lancamentoSchoolClassId;
    private Long lancamentoDisciplineId;
    private Trimester lancamentoTrimester;
    private boolean lancamentoRendered = false;
    private List<LancamentoRow> lancamentoRows = new ArrayList<>();

    // ── SERVIÇOS ──
    @Inject
    private TrimesterResultService trimesterResultService;
    @Inject
    private SchoolClassRepository schoolClassRepository;
    @Inject
    private DisciplineService disciplineService;
    private transient TrimesterResultLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new TrimesterResultLazyModel(trimesterResultService);
        loadSchoolClasses();
        loadDisciplines();
        computeStatistics();
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar resultados", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de resultados", e);
        }
        return "/management/pedagogico/trimesterResults.xhtml?faces-redirect=true";
    }

    // ═══════════════════════════════════════════════════════════════
    // NAVEGAÇÃO PARA MINI-PAUTA (view dedicada)
    // ═══════════════════════════════════════════════════════════════

    public String goToMiniPauta() {
        return "/management/pedagogico/mini-pauta?faces-redirect=true";
    }

    public String goToMiniPautaWithParams(Long schoolClassId, String trimesterStr) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().put("mpAutoSearch", true);
        ctx.getExternalContext().getFlash().put("mpSchoolClassId", schoolClassId);
        ctx.getExternalContext().getFlash().put("mpTrimester", trimesterStr);
        return "/management/pedagogico/mini-pauta?faces-redirect=true";
    }

    // ═══════════════════════════════════════════════════════════════
    // FILTROS
    // ═══════════════════════════════════════════════════════════════

    public void applyFilters() {
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros aplicados", "");
    }

    public void clearFilters() {
        filterSchoolClassId = null;
        filterDisciplineId = null;
        filterTrimester = null;
        filterSituation = null;
        filterStudentName = null;
        if (lazyModel != null) {
            lazyModel.clearFilters();
        }
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros limpos", "");
    }

    public List<TrimesterResultDTO> getFilteredResults() {
        List<TrimesterResultDTO> all = trimesterResultService.getAllResults();
        if (all == null) {
            return new ArrayList<>();
        }

        return all.stream()
                .filter(r -> filterSchoolClassId == null ||
                        (r.getSchoolClassName() != null && schoolClasses.stream()
                                .anyMatch(sc -> sc.getPkSchoolClass().equals(filterSchoolClassId)
                                        && sc.getClassName().equals(r.getSchoolClassName()))))
                .filter(r -> filterDisciplineId == null ||
                        (r.getDisciplinePk() != null && r.getDisciplinePk().equals(filterDisciplineId)))
                .filter(r -> filterTrimester == null || filterTrimester.isBlank() ||
                        (r.getTrimester() != null && r.getTrimester().toString().equalsIgnoreCase(filterTrimester)))
                .filter(r -> filterSituation == null || filterSituation.isBlank() ||
                        (r.getSituation() != null && r.getSituation().toString().equalsIgnoreCase(filterSituation)))
                .filter(r -> filterStudentName == null || filterStudentName.isBlank() ||
                        (r.getStudentFullName() != null && r.getStudentFullName().toLowerCase().contains(filterStudentName.toLowerCase())))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // CÁLCULO DE RESULTADOS
    // ═══════════════════════════════════════════════════════════════

    public void calcularResultados(Long enrolmentPk, Long disciplinePk, Trimester trimester) {
        try {
            trimesterResultService.calcularTrimesterResult(enrolmentPk, disciplinePk, trimester);
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Resultados calculados com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao calcular resultados", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LANÇAMENTO DE NOTAS (stubs para o XHTML)
    // ═══════════════════════════════════════════════════════════════

    public void limparLancamentoNotas() {
        lancamentoSchoolClassId = null;
        lancamentoDisciplineId = null;
        lancamentoTrimester = null;
        lancamentoRendered = false;
        lancamentoRows = new ArrayList<>();
    }

    public void carregarLancamentoNotas() {
        lancamentoRendered = true;
        lancamentoRows = new ArrayList<>();
    }

    public void salvarLancamentoNotas() {
        addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Lançamento guardado (stub).");
        limparLancamentoNotas();
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPORTAR
    // ═══════════════════════════════════════════════════════════════

    public void exportPdf() {
        addMessage(FacesMessage.SEVERITY_INFO, "Exportar", "Exportação PDF ainda não implementada.");
    }

    // ═══════════════════════════════════════════════════════════════
    // CARREGAMENTO
    // ═══════════════════════════════════════════════════════════════

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassRepository.findAll();
            if (schoolClasses == null) {
                schoolClasses = new ArrayList<>();
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas", e);
        }
    }

    private void loadDisciplines() {
        try {
            disciplines = disciplineService.getAllActive();
            if (disciplines == null) {
                disciplines = new ArrayList<>();
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar disciplinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar disciplinas", e);
        }
    }

    private void computeStatistics() {
        try {
            List<TrimesterResultDTO> all = getFilteredResults();
            totalResultCount = all.size();
            approvedCount = all.stream().filter(r -> r.getSituation() == SituationType.APROVADO).count();
            failedCount = all.stream().filter(r -> r.getSituation() == SituationType.REPROVADO).count();
            inProgressCount = all.stream().filter(r -> r.getSituation() == SituationType.EM_CURSO).count();
        } catch (Exception e) {
            totalResultCount = 0;
            approvedCount = 0;
            failedCount = 0;
            inProgressCount = 0;
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    public void prepareNewResult() {
        trimesterResult = new TrimesterResult();
        selectedEnrolmentId = null;
        selectedDisciplineId = null;
    }

    public String saveResult() {
        try {
            if (selectedEnrolmentId == null || selectedDisciplineId == null || trimesterResult.getTrimester() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Resultado", "Preencha todos os campos obrigatórios.");
                return null;
            }
            var enrolment = new com.SistemSchool.modulo_secrtaria.model.Enrolment();
            enrolment.setPhEnrolment(selectedEnrolmentId);
            trimesterResult.setEnrolment(enrolment);

            var discipline = new Discipline();
            discipline.setPkDiscipline(selectedDisciplineId);
            trimesterResult.setDiscipline(discipline);

            trimesterResultService.save(trimesterResult);
            trimesterResult = new TrimesterResult();
            selectedEnrolmentId = null;
            selectedDisciplineId = null;
            init();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Resultado registado com sucesso");
            return "/management/pedagogico/trimesterResults.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar resultado", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
            return null;
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum resultado selecionado");
            return;
        }
        this.selectedId = id;
        List<TrimesterResultDTO> all = trimesterResultService.getAllResults();
        if (all == null) all = new ArrayList<>();
        TrimesterResultDTO dto = all.stream()
                .filter(r -> id.equals(r.getPkTrimesterResult())).findFirst().orElse(null);
        if (dto != null) {
            mapDtoFields(dto, editDto = new TrimesterResultDTO());
            mapDtoFields(dto, selectedResult);
            selectedEnrolmentId = dto.getEnrolmentPk();
            selectedDisciplineId = dto.getDisciplinePk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Resultado não encontrado");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum resultado selecionado");
            return;
        }
        this.selectedId = id;
    }

    public void deleteResult() {
        if (selectedId == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum resultado selecionado para eliminar");
            return;
        }
        try {
            trimesterResultService.delete(selectedId);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Resultado eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar resultado", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void saveUpdate() {
        try {
            trimesterResultService.update(editDto);
            init();
            editDto = new TrimesterResultDTO();
            selectedId = null;
            selectedEnrolmentId = null;
            selectedDisciplineId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Resultado atualizado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar resultado", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
        }
    }

    public void viewResultDetails(Long id) {
        if (id == null) return;
        List<TrimesterResultDTO> all = trimesterResultService.getAllResults();
        if (all == null) all = new ArrayList<>();
        TrimesterResultDTO dto = all.stream()
                .filter(r -> id.equals(r.getPkTrimesterResult())).findFirst().orElse(null);
        if (dto != null) {
            this.selectedResult = dto;
            this.selectedId = id;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UTIL
    // ═══════════════════════════════════════════════════════════════

    private void mapDtoFields(TrimesterResultDTO source, TrimesterResultDTO target) {
        target.setPkTrimesterResult(source.getPkTrimesterResult());
        target.setEnrolmentPk(source.getEnrolmentPk());
        target.setStudentFullName(source.getStudentFullName());
        target.setStudentNumber(source.getStudentNumber());
        target.setSchoolClassName(source.getSchoolClassName());
        target.setDisciplinePk(source.getDisciplinePk());
        target.setDisciplineName(source.getDisciplineName());
        target.setTrimester(source.getTrimester());
        target.setMac(source.getMac());
        target.setNpt(source.getNpt());
        target.setMt(source.getMt());
        target.setSituation(source.getSituation());
        target.setObs(source.getObs());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ═══════════════════════════════════════════════════════════════
    // VIEW MODELS INTERNOS (Lançamento)
    // ═══════════════════════════════════════════════════════════════

    public static class LancamentoRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private String studentName;
        private Double mac;
        private Double npt;
        private Double mt;
        private SituationType situation;

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Double getMac() { return mac; }
        public void setMac(Double mac) { this.mac = mac; }
        public Double getNpt() { return npt; }
        public void setNpt(Double npt) { this.npt = npt; }
        public Double getMt() { return mt; }
        public void setMt(Double mt) { this.mt = mt; }
        public SituationType getSituation() { return situation; }
        public void setSituation(SituationType situation) { this.situation = situation; }
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════

    public TrimesterResult getTrimesterResult() { return trimesterResult; }
    public void setTrimesterResult(TrimesterResult trimesterResult) { this.trimesterResult = trimesterResult; }
    public TrimesterResultDTO getEditDto() { return editDto; }
    public void setEditDto(TrimesterResultDTO editDto) { this.editDto = editDto; }
    public TrimesterResultDTO getSelectedResult() { return selectedResult; }
    public void setSelectedResult(TrimesterResultDTO selectedResult) { this.selectedResult = selectedResult; }
    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }
    public Long getSelectedEnrolmentId() { return selectedEnrolmentId; }
    public void setSelectedEnrolmentId(Long selectedEnrolmentId) { this.selectedEnrolmentId = selectedEnrolmentId; }
    public Long getSelectedDisciplineId() { return selectedDisciplineId; }
    public void setSelectedDisciplineId(Long selectedDisciplineId) { this.selectedDisciplineId = selectedDisciplineId; }
    public TrimesterResultLazyModel getLazyModel() { return lazyModel; }
    public void setLazyModel(TrimesterResultLazyModel lazyModel) { this.lazyModel = lazyModel; }
    public Long getFilterSchoolClassId() { return filterSchoolClassId; }
    public void setFilterSchoolClassId(Long filterSchoolClassId) { this.filterSchoolClassId = filterSchoolClassId; }
    public Long getFilterDisciplineId() { return filterDisciplineId; }
    public void setFilterDisciplineId(Long filterDisciplineId) { this.filterDisciplineId = filterDisciplineId; }
    public String getFilterTrimester() { return filterTrimester; }
    public void setFilterTrimester(String filterTrimester) { this.filterTrimester = filterTrimester; }
    public String getFilterSituation() { return filterSituation; }
    public void setFilterSituation(String filterSituation) { this.filterSituation = filterSituation; }
    public String getFilterStudentName() { return filterStudentName; }
    public void setFilterStudentName(String filterStudentName) { this.filterStudentName = filterStudentName; }
    public long getTotalResultCount() { return totalResultCount; }
    public long getApprovedCount() { return approvedCount; }
    public long getFailedCount() { return failedCount; }
    public long getInProgressCount() { return inProgressCount; }
    public List<SchoolClass> getSchoolClasses() { return schoolClasses; }
    public List<Discipline> getDisciplines() { return disciplines; }
    public Trimester[] getTrimesters() { return Trimester.values(); }
    public SituationType[] getSituations() { return SituationType.values(); }
    public List<TrimesterResultDTO> getResults() { return trimesterResultService.getAllResults(); }

    // Lançamento getters/setters
    public Long getLancamentoSchoolClassId() { return lancamentoSchoolClassId; }
    public void setLancamentoSchoolClassId(Long lancamentoSchoolClassId) { this.lancamentoSchoolClassId = lancamentoSchoolClassId; }
    public Long getLancamentoDisciplineId() { return lancamentoDisciplineId; }
    public void setLancamentoDisciplineId(Long lancamentoDisciplineId) { this.lancamentoDisciplineId = lancamentoDisciplineId; }
    public Trimester getLancamentoTrimester() { return lancamentoTrimester; }
    public void setLancamentoTrimester(Trimester lancamentoTrimester) { this.lancamentoTrimester = lancamentoTrimester; }
    public boolean isLancamentoRendered() { return lancamentoRendered; }
    public void setLancamentoRendered(boolean lancamentoRendered) { this.lancamentoRendered = lancamentoRendered; }
    public List<LancamentoRow> getLancamentoRows() { return lancamentoRows; }
    public void setLancamentoRows(List<LancamentoRow> lancamentoRows) { this.lancamentoRows = lancamentoRows; }
}