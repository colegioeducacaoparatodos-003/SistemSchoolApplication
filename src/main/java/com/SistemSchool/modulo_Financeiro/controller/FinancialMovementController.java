package com.SistemSchool.modulo_Financeiro.controller;

import com.SistemSchool.modulo_Financeiro.dto.FinancialMovementDTO;
import com.SistemSchool.modulo_Financeiro.io.MovementStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_Financeiro.lazy.FinancialMovementLazyModel;
import com.SistemSchool.modulo_Financeiro.model.CashBox;
import com.SistemSchool.modulo_Financeiro.model.FinancialMovement;
import com.SistemSchool.modulo_Financeiro.repository.CashBoxRepository;
import com.SistemSchool.modulo_Financeiro.service.FinancialMovementService;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;
import com.SistemSchool.modulo_secrtaria.repository.PagamentoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class FinancialMovementController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FinancialMovementController.class.getName());

    // ─────────────────────────────────────────────────────────────
    // MODELOS
    // ─────────────────────────────────────────────────────────────

    private FinancialMovement movement = new FinancialMovement();
    private FinancialMovementDTO editDto = new FinancialMovementDTO();
    private FinancialMovementDTO selectedMovement = new FinancialMovementDTO();
    private Long selectedId;

    private Long selectedCashBoxId;
    private Long selectedPagamentoId;

    private List<CashBox> cashBoxes = new java.util.ArrayList<>();
    private List<Pagamento> pagamentos = new java.util.ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────

    private long totalMovementCount;
    private long incomeCount;
    private long expenseCount;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    private MovementType filterType;
    private MovementStatus filterStatus;
    private String filterCategory;
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private String filterSearchText;

    // ─────────────────────────────────────────────────────────────
    // SERVIÇOS
    // ─────────────────────────────────────────────────────────────

    @Inject
    private FinancialMovementService financialMovementService;

    @Inject
    private CashBoxRepository cashBoxRepository;

    @Inject
    private PagamentoRepository pagamentoRepository;

    private transient FinancialMovementLazyModel lazyModel;

    // ─────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO
    // ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        lazyModel = new FinancialMovementLazyModel(financialMovementService);
        loadCashBoxes();
        loadPagamentos();
        computeStatistics();
    }

    private void loadCashBoxes() {
        try {
            cashBoxes = cashBoxRepository.findAll();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar caixas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar caixas para o formulário de movimento", e);
        }
    }

    private void loadPagamentos() {
        try {
            pagamentos = pagamentoRepository.findAll();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar pagamentos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar pagamentos para o formulário de movimento", e);
        }
    }

    private void computeStatistics() {
        try {
            List<FinancialMovementDTO> all = financialMovementService.getAllFinancialMovements();
            totalMovementCount = all.size();
            incomeCount = all.stream().filter(m -> m.getType() == MovementType.INCOME).count();
            expenseCount = all.stream().filter(m -> m.getType() == MovementType.EXPENSE).count();
            totalIncome = financialMovementService.getTotalIncome();
            totalExpense = financialMovementService.getTotalExpense();
            balance = totalIncome.subtract(totalExpense);
        } catch (Exception e) {
            totalMovementCount = 0;
            incomeCount = 0;
            expenseCount = 0;
            totalIncome = BigDecimal.ZERO;
            totalExpense = BigDecimal.ZERO;
            balance = BigDecimal.ZERO;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatísticas", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao calcular estatísticas de movimentos financeiros", e);
        }
    }

    public String load() {
        try {
            init();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar movimentos", e.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao carregar a listagem de movimentos financeiros", e);
        }
        return "/management/financeiro/movements.xhtml?faces-redirect=true";
    }

    public FinancialMovementLazyModel getLazyModel() {
        return lazyModel;
    }

    // ─────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────

    public void applyFilters() {
        lazyModel = new FinancialMovementLazyModel(financialMovementService);
        // Aplica filtros externos via propriedades do LazyModel
        lazyModel.setExternalFilters(buildExternalFilters());
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros", "Filtros aplicados com sucesso");
    }

    public void clearFilters() {
        filterType = null;
        filterStatus = null;
        filterCategory = null;
        filterStartDate = null;
        filterEndDate = null;
        filterSearchText = null;
        lazyModel = new FinancialMovementLazyModel(financialMovementService);
        computeStatistics();
        addMessage(FacesMessage.SEVERITY_INFO, "Filtros", "Filtros limpos com sucesso");
    }

    private java.util.Map<String, Object> buildExternalFilters() {
        java.util.Map<String, Object> filters = new java.util.HashMap<>();
        if (filterType != null) filters.put("type", filterType);
        if (filterStatus != null) filters.put("status", filterStatus);
        if (filterCategory != null && !filterCategory.isBlank()) filters.put("category", filterCategory);
        if (filterStartDate != null) filters.put("startDate", filterStartDate.atStartOfDay());
        if (filterEndDate != null) filters.put("endDate", filterEndDate.atTime(23, 59, 59));
        if (filterSearchText != null && !filterSearchText.isBlank()) filters.put("global", filterSearchText);
        return filters;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    public void prepareNewMovement() {
        movement = new FinancialMovement();
        movement.setMovementDate(LocalDateTime.now());
        movement.setStatus(MovementStatus.ACTIVE);
        selectedCashBoxId = null;
        selectedPagamentoId = null;
    }

    public String saveMovement() {
        try {
            if (selectedCashBoxId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Movimento", "Selecione um caixa antes de gravar.");
                return null;
            }

            CashBox cashBox = cashBoxes.stream()
                    .filter(cb -> selectedCashBoxId.equals(cb.getPhCashBox()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Caixa não encontrado."));
            movement.setCashBox(cashBox);

            if (selectedPagamentoId != null) {
                Pagamento pagamento = pagamentos.stream()
                        .filter(p -> selectedPagamentoId.equals(p.getPkPagamento()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));
                movement.setPagamento(pagamento);
            } else {
                movement.setPagamento(null);
            }

            financialMovementService.save(movement);

            movement = new FinancialMovement();
            selectedCashBoxId = null;
            selectedPagamentoId = null;
            init();

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            addMessage(FacesMessage.SEVERITY_INFO, "Movimento", "Movimento registado com sucesso");
            return "/management/financeiro/movements.xhtml?faces-redirect=true";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar movimento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Movimento", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VIEW / EDIT / DELETE
    // ─────────────────────────────────────────────────────────────

    public void viewMovementDetails(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum movimento selecionado!", "");
            return;
        }
        this.selectedId = id;
        FinancialMovementDTO dto = findDtoById(id);
        if (dto != null) {
            mapDtoFields(dto, selectedMovement);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Movimento não encontrado", "");
        }
    }

    public void openEditDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nenhum movimento selecionado!", "");
            return;
        }
        this.selectedId = id;
        FinancialMovementDTO dto = findDtoById(id);
        if (dto != null) {
            mapDtoFields(dto, editDto = new FinancialMovementDTO());
            mapDtoFields(dto, selectedMovement);
            selectedCashBoxId = dto.getCashBoxPk();
            selectedPagamentoId = dto.getPagamentoPk();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Movimento não encontrado", "");
        }
    }

    public void openDeleteDialog(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum movimento selecionado!", "");
            return;
        }
        this.selectedId = id;
    }

    public void loadSelectedMovement() {
        if (selectedId == null) return;
        FinancialMovementDTO dto = findDtoById(selectedId);
        if (dto != null) {
            mapDtoFields(dto, selectedMovement);
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Movimento não encontrado", "");
        }
    }

    private FinancialMovementDTO findDtoById(Long id) {
        return financialMovementService.getAllFinancialMovements()
                .stream()
                .filter(m -> id.equals(m.getPhMovement()))
                .findFirst()
                .orElse(null);
    }

    private void mapDtoFields(FinancialMovementDTO source, FinancialMovementDTO target) {
        target.setPhMovement(source.getPhMovement());
        target.setMovementNumber(source.getMovementNumber());
        target.setCashBoxPk(source.getCashBoxPk());
        target.setCashBoxNumber(source.getCashBoxNumber());
        target.setPagamentoPk(source.getPagamentoPk());
        target.setPagamentoNumeroDocumento(source.getPagamentoNumeroDocumento());
        target.setDescription(source.getDescription());
        target.setAmount(source.getAmount());
        target.setType(source.getType());
        target.setCategory(source.getCategory());
        target.setStatus(source.getStatus());
        target.setResponsible(source.getResponsible());
        target.setObservation(source.getObservation());
        target.setMovementDate(source.getMovementDate());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (selectedCashBoxId != null) {
                editDto.setCashBoxPk(selectedCashBoxId);
            }
            if (selectedPagamentoId != null) {
                editDto.setPagamentoPk(selectedPagamentoId);
            }
            financialMovementService.update(editDto);
            init();
            editDto = new FinancialMovementDTO();
            selectedId = null;
            selectedCashBoxId = null;
            selectedPagamentoId = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Movimento", "Movimento atualizado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar movimento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Movimento", e.getMessage());
        }
    }

    public void delete(Long id) {
        if (id == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Nenhum movimento selecionado!", "");
            return;
        }
        try {
            financialMovementService.delete(id);
            selectedId = null;
            init();
            addMessage(FacesMessage.SEVERITY_INFO, "Movimento", "Movimento eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar movimento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Movimento", e.getMessage());
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

    public FinancialMovement getMovement() { return movement; }
    public void setMovement(FinancialMovement movement) { this.movement = movement; }

    public FinancialMovementDTO getEditDto() { return editDto; }
    public void setEditDto(FinancialMovementDTO editDto) { this.editDto = editDto; }

    public FinancialMovementDTO getSelectedMovement() { return selectedMovement; }
    public void setSelectedMovement(FinancialMovementDTO selectedMovement) { this.selectedMovement = selectedMovement; }

    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }

    public Long getSelectedCashBoxId() { return selectedCashBoxId; }
    public void setSelectedCashBoxId(Long selectedCashBoxId) { this.selectedCashBoxId = selectedCashBoxId; }

    public Long getSelectedPagamentoId() { return selectedPagamentoId; }
    public void setSelectedPagamentoId(Long selectedPagamentoId) { this.selectedPagamentoId = selectedPagamentoId; }

    public void setLazyModel(FinancialMovementLazyModel lazyModel) { this.lazyModel = lazyModel; }

    // Filtros
    public MovementType getFilterType() { return filterType; }
    public void setFilterType(MovementType filterType) { this.filterType = filterType; }
    public MovementStatus getFilterStatus() { return filterStatus; }
    public void setFilterStatus(MovementStatus filterStatus) { this.filterStatus = filterStatus; }
    public String getFilterCategory() { return filterCategory; }
    public void setFilterCategory(String filterCategory) { this.filterCategory = filterCategory; }
    public LocalDate getFilterStartDate() { return filterStartDate; }
    public void setFilterStartDate(LocalDate filterStartDate) { this.filterStartDate = filterStartDate; }
    public LocalDate getFilterEndDate() { return filterEndDate; }
    public void setFilterEndDate(LocalDate filterEndDate) { this.filterEndDate = filterEndDate; }
    public String getFilterSearchText() { return filterSearchText; }
    public void setFilterSearchText(String filterSearchText) { this.filterSearchText = filterSearchText; }

    // Estatísticas
    public long getTotalMovementCount() { return totalMovementCount; }
    public long getIncomeCount() { return incomeCount; }
    public long getExpenseCount() { return expenseCount; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getBalance() { return balance; }

    // Enums e Listas
    public MovementType[] getTypes() { return MovementType.values(); }
    public MovementStatus[] getStatuses() { return MovementStatus.values(); }
    public List<CashBox> getCashBoxes() { return cashBoxes; }
    public List<Pagamento> getPagamentos() { return pagamentos; }
    public List<Pagamento> getPayments() { return pagamentos; }
    public List<FinancialMovementDTO> getMovements() { return financialMovementService.getAllFinancialMovements(); }

    public void refreshCashBoxes() { loadCashBoxes(); }
    public void refreshPagamentos() { loadPagamentos(); }
}