package com.SistemSchool.modulo_Financeiro.controller;

import com.SistemSchool.modulo_Financeiro.dto.FeeDTO;
import com.SistemSchool.modulo_Financeiro.io.FeeStatus;
import com.SistemSchool.modulo_Financeiro.io.FeeType;
import com.SistemSchool.modulo_Financeiro.lazy.FeeLazyModel;
import com.SistemSchool.modulo_Financeiro.model.Fee;
import com.SistemSchool.modulo_Financeiro.service.FeeService;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named
@ViewScoped
public class FeeController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FeeController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private Fee fee = new Fee();
    private FeeDTO editDto = new FeeDTO();
    private FeeDTO selectedFee = new FeeDTO();
    private Long selectedId;
    private Long selectedSchoolClassId;
    private List<SchoolClass> schoolClasses = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // FILTROS AVANÇADOS
    // ─────────────────────────────────────────────────────────────

    private Long filterSchoolClassId;
    private FeeType filterFeeType;
    private FeeStatus filterStatus;
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private String filterSearchText;

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalFeeCount;
    private long activeFeeCount;
    private BigDecimal totalFeeAmount;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private FeeService feeService;

    @Inject
    private SchoolClassRepository schoolClassRepository;

    private transient FeeLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new FeeLazyModel(feeService);
        loadSchoolClasses();
        computeStatistics();
    }

    private void loadSchoolClasses() {
        try {
            schoolClasses = schoolClassRepository.findAll();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar turmas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar turmas para o formulário de propina", e);
        }
    }

    private void computeStatistics() {
        try {
            List<FeeDTO> all = feeService.getAllFees();
            totalFeeCount = all.size();
            activeFeeCount = all.stream()
                    .filter(f -> f.getStatus() == FeeStatus.ACTIVE)
                    .count();
            totalFeeAmount = all.stream()
                    .map(FeeDTO::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            totalFeeCount = 0;
            activeFeeCount = 0;
            totalFeeAmount = BigDecimal.ZERO;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de propinas", e);
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar propinas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de propinas", e);
        }
        return "/management/financeiro/fees.xhtml?faces-redirect=true";
    }

    public FeeLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // PREPARAÇÃO E VISUALIZAÇÃO
    // ─────────────────────────────────────────────────────────────

    public void prepareNewFee() {
        fee = new Fee();
        selectedSchoolClassId = null;
    }

    public void viewFeeDetails(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma propina selecionada", "");
            return;
        }
        selectedFee = feeService.getAllFees().stream()
                .filter(f -> id.equals(f.getPhFee()))
                .findFirst()
                .orElse(null);
        if (selectedFee == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Propina não encontrada", "");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma propina selecionada", "");
            return;
        }
        this.selectedId = id;
        this.selectedFee = feeService.getAllFees().stream()
                .filter(f -> id.equals(f.getPhFee()))
                .findFirst()
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    public void applyFilters() {
        try {
            List<FeeDTO> all = feeService.getAllFees();
            Stream<FeeDTO> stream = all.stream();

            if (filterSchoolClassId != null) {
                stream = stream.filter(f -> filterSchoolClassId.equals(f.getSchoolClassPk()));
            }
            if (filterFeeType != null) {
                stream = stream.filter(f -> filterFeeType.equals(f.getFeeType()));
            }
            if (filterStatus != null) {
                stream = stream.filter(f -> filterStatus.equals(f.getStatus()));
            }
            if (filterStartDate != null) {
                stream = stream.filter(f -> f.getStartDate() != null &&
                        !f.getStartDate().toLocalDate().isBefore(filterStartDate));
            }
            if (filterEndDate != null) {
                stream = stream.filter(f -> f.getEndDate() != null &&
                        !f.getEndDate().toLocalDate().isAfter(filterEndDate));
            }
            if (filterSearchText != null && !filterSearchText.trim().isEmpty()) {
                String search = filterSearchText.toLowerCase();
                stream = stream.filter(f ->
                    (f.getFeeCode() != null && f.getFeeCode().toLowerCase().contains(search)) ||
                    (f.getDescription() != null && f.getDescription().toLowerCase().contains(search))
                );
            }

            List<FeeDTO> filtered = stream.collect(Collectors.toList());
            lazyModel = new FeeLazyModel(feeService, filtered);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar filtros", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao filtrar propinas", e.getMessage());
        }
    }

    public void clearFilters() {
        filterSchoolClassId = null;
        filterFeeType = null;
        filterStatus = null;
        filterStartDate = null;
        filterEndDate = null;
        filterSearchText = null;
        init();
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public String saveFee() {
        try {
            if (selectedSchoolClassId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Propina", "Selecione uma turma antes de gravar.");
                return null;
            }

            SchoolClass schoolClass = schoolClasses.stream()
                    .filter(sc -> selectedSchoolClassId.equals(sc.getPkSchoolClass()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada."));
            fee.setSchoolClass(schoolClass);

            feeService.save(fee);

            fee = new Fee();
            selectedSchoolClassId = null;
            init();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Propina", "Propina registada com sucesso");
            return "/management/financeiro/fees.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar propina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Propina", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhuma propina selecionada!", "");
            return;
        }
        this.selectedId = id;
        FeeDTO dto = feeService.getAllFees()
                .stream()
                .filter(f -> id.equals(f.getPhFee()))
                .findFirst()
                .orElse(null);

        if (dto != null) {
            mapDtoFields(dto, editDto = new FeeDTO());
            mapDtoFields(dto, selectedFee);
            selectedSchoolClassId = dto.getSchoolClassPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Propina não encontrada", "");
        }
    }

    public void loadSelectedFee() {
        if (selectedId == null) {
            return;
        }
        FeeDTO dto = feeService.getAllFees()
                .stream()
                .filter(f -> selectedId.equals(f.getPhFee()))
                .findFirst()
                .orElse(null);
        if (dto != null) {
            mapDtoFields(dto, selectedFee);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Propina não encontrada", "");
        }
    }

    private void mapDtoFields(FeeDTO source, FeeDTO target) {
        target.setPhFee(source.getPhFee());
        target.setFeeCode(source.getFeeCode());
        target.setDescription(source.getDescription());
        target.setFeeType(source.getFeeType());
        target.setSchoolClassPk(source.getSchoolClassPk());
        target.setSchoolClassName(source.getSchoolClassName());
        target.setSchoolYear(source.getSchoolYear());
        target.setAmount(source.getAmount());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setStatus(source.getStatus());
        target.setObs(source.getObs());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (selectedSchoolClassId != null) {
                editDto.setSchoolClassPk(selectedSchoolClassId);
            }
            feeService.update(editDto);
            init();
            editDto = new FeeDTO();
            selectedId = null;
            selectedSchoolClassId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Propina", "Propina atualizada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar propina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Propina", e.getMessage());
        }
    }

    public void delete(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhuma propina selecionada!", "");
            return;
        }
        try {
            feeService.delete(id);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Propina", "Propina eliminada com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar propina", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Propina", e.getMessage());
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

    public Fee getFee() { return fee; }
    public void setFee(Fee fee) { this.fee = fee; }

    public FeeDTO getEditDto() { return editDto; }
    public void setEditDto(FeeDTO editDto) { this.editDto = editDto; }

    public FeeDTO getSelectedFee() { return selectedFee; }
    public void setSelectedFee(FeeDTO selectedFee) { this.selectedFee = selectedFee; }

    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }

    public Long getSelectedSchoolClassId() { return selectedSchoolClassId; }
    public void setSelectedSchoolClassId(Long selectedSchoolClassId) { this.selectedSchoolClassId = selectedSchoolClassId; }

    public void setLazyModel(FeeLazyModel lazyModel) { this.lazyModel = lazyModel; }

    // Filtros
    public Long getFilterSchoolClassId() { return filterSchoolClassId; }
    public void setFilterSchoolClassId(Long filterSchoolClassId) { this.filterSchoolClassId = filterSchoolClassId; }

    public FeeType getFilterFeeType() { return filterFeeType; }
    public void setFilterFeeType(FeeType filterFeeType) { this.filterFeeType = filterFeeType; }

    public FeeStatus getFilterStatus() { return filterStatus; }
    public void setFilterStatus(FeeStatus filterStatus) { this.filterStatus = filterStatus; }

    public LocalDate getFilterStartDate() { return filterStartDate; }
    public void setFilterStartDate(LocalDate filterStartDate) { this.filterStartDate = filterStartDate; }

    public LocalDate getFilterEndDate() { return filterEndDate; }
    public void setFilterEndDate(LocalDate filterEndDate) { this.filterEndDate = filterEndDate; }

    public String getFilterSearchText() { return filterSearchText; }
    public void setFilterSearchText(String filterSearchText) { this.filterSearchText = filterSearchText; }

    // Estatísticas
    public long getTotalFeeCount() { return totalFeeCount; }
    public long getActiveFeeCount() { return activeFeeCount; }
    public BigDecimal getTotalFeeAmount() { return totalFeeAmount; }

    // Enums e Listas
    public FeeStatus[] getStatuses() { return FeeStatus.values(); }
    public List<SchoolClass> getSchoolClasses() { return schoolClasses; }
    public void refreshSchoolClasses() { loadSchoolClasses(); }
    public List<FeeDTO> getFees() { return feeService.getAllFees(); }
    public FeeType[] getFeeTypes() { return FeeType.values(); }
}